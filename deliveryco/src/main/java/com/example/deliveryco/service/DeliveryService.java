package com.example.deliveryco.service;

import com.example.deliveryco.config.RabbitMQConfig;
import com.example.deliveryco.dto.request.DeliveryFailureAlert;
import com.example.deliveryco.dto.request.DeliveryRequest;
import com.example.deliveryco.dto.response.DeliveryUpdate;
import com.example.deliveryco.entity.Delivery;
import com.example.deliveryco.entity.DeliveryStatus;
import com.example.deliveryco.messaging.CustomCorrelationData;
import com.example.deliveryco.repository.DeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final Random random = new Random();
    private final DeliveryUpdateService deliveryUpdateService;
    private final DeliverySaveService deliverySaveService;

    @Value("${store.api.url:http://localhost:8082}")
    private String STORE_API_URL;

    @Value("${store.alert.endpoint:/api/delivery/alert}")
    private String ALERT_ENDPOINT;

    @Value("${deliveryco.simulation.pickup-delay-ms:15000}")
    private long pickupDelayMs;

    @Value("${deliveryco.simulation.status-interval-ms:5000}")
    private long statusIntervalMs;

    @Value("${deliveryco.simulation.lost-percentage:5}")
    private long lostPercentage;

    private final RestTemplate restTemplate;

    private final RabbitTemplate rabbitTemplate;

    public DeliveryService( DeliveryRepository deliveryRepository, DeliveryUpdateService deliveryUpdateService,  DeliverySaveService deliverySaveService, RabbitTemplate rabbitTemplate, RestTemplate restTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryUpdateService = deliveryUpdateService;
        this.deliverySaveService = deliverySaveService;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void processDelivery(DeliveryRequest deliveryRequest) {
//        //  FOR DLQ TESTING DEMO
//        if (true) {
//            throw new RuntimeException("TESTING DLQ");
//        }

        log.info("Successfully recieved deliveryRequest: {}", deliveryRequest);

        if (deliveryRepository.findByOrderId(deliveryRequest.getOrderId()).isPresent()) {
            log.warn("Order ID {} already exists. No need to do anything!", deliveryRequest.getOrderId());
            return;
        }

        // saving the delivery in db
        Delivery delivery  = deliverySaveService.save(deliveryRequest);

        log.info("Successfully saved deliveryRequest with LOST PERCENTAGE of {}: {}. ",lostPercentage , deliveryRequest);
        log.info("Delivery after being saved: {}", delivery);

        // simulation
        int orderId = deliveryRequest.getOrderId();
        try {
            // PICKED_UP
            log.info("orderId {} is PICKED UP", orderId);
            Thread.sleep(pickupDelayMs);
            processUpdate(orderId, DeliveryStatus.PICKED_UP);

            // ON_DELIVERY
            log.info("orderId {} is  ON_DELIVERY", orderId);
            Thread.sleep(statusIntervalMs);
            processUpdate(orderId, DeliveryStatus.ON_DELIVERY);

            // DELIVERED OR LOST (5%)
            Thread.sleep(statusIntervalMs);
            if (random.nextInt(100) < lostPercentage) {
                log.info("orderId {} is  LOST", orderId);
                processUpdate(orderId, DeliveryStatus.LOST);
            } else{
                log.info("orderId {} is  DELIVERED", orderId);
                processUpdate(orderId, DeliveryStatus.DELIVERED);
            }

        } catch (Exception e) {

            log.error("orderId delivery failed {}", orderId);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Delivery processing failed", e);
        }
    }
    public void processUpdate(int orderId, DeliveryStatus deliveryStatus) {
        deliveryUpdateService.saveStatus(orderId, deliveryStatus);
        sendUpdate(
                orderId,
                deliveryStatus,
                RabbitMQConfig.getStatusMessage(deliveryStatus));
    }

    public void sendUpdate(int orderId, DeliveryStatus deliveryStatus, String message) {

        DeliveryUpdate deliveryUpdate = DeliveryUpdate.builder()
                .orderId(orderId)
                .status(deliveryStatus)
                .timeStamp(LocalDateTime.now())
                .message(message)
                .build();

        try {
            CustomCorrelationData correlationData = CustomCorrelationData.builder()
                    .id("order-" + orderId + "-" + UUID.randomUUID().toString())
                    .orderId(orderId)
                    .build();

            // Callback for ACK/NACK after successful send
            correlationData.getFuture().whenComplete((r, e) -> {
                if (e != null) {
                    log.warn("Exception during message confirmation", e);
                    updateStoreViaRestAPI(orderId, deliveryStatus);
                } else if (r != null && r.isAck()) {
                    log.info("Message successfully delivered to queue for order {}", orderId);
                } else {
                    log.warn("NACK received - falling back to REST API");
                    updateStoreViaRestAPI(orderId, deliveryStatus);
                }
            });

            // Attempt to send via RabbitMQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STORE_EXCHANGE,
                    RabbitMQConfig.getStatusKey(deliveryStatus),
                    deliveryUpdate,
                    correlationData
            );

            log.debug("Message sent to RabbitMQ for order {}", orderId);

        } catch (AmqpException | IllegalStateException e) {
            // RabbitMQ connection failed - use REST API as fallback
            log.error("RabbitMQ unavailable for order {} - using REST API fallback", orderId, e);
            updateStoreViaRestAPI(orderId, deliveryStatus);
        }
    }

    private void updateStoreViaRestAPI(Integer orderId, DeliveryStatus deliveryStatus) {
        try{
            String alertUrl =  STORE_API_URL + ALERT_ENDPOINT;

            DeliveryFailureAlert failureAlert = DeliveryFailureAlert.builder()
                    .timestamp(LocalDateTime.now())
                    .deliveryStatus(deliveryStatus)
                    .orderId(orderId)
                    .build();

            ResponseEntity<Void> response = restTemplate.postForEntity(alertUrl, failureAlert, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully send alert to STORE via REST API: {}", failureAlert);
            }
            else {
                log.error("REST API ERROR: {}", response.getStatusCode());
                // save in db with failed + deliverystatus (eg. FAILED_PICKED_UP)
                deliveryUpdateService.saveFailedStatus(orderId, deliveryStatus);
            }
        }catch (Exception e){
            log.error("REST API FAILED for order {}", orderId, e);
            deliveryUpdateService.saveFailedStatus(orderId, deliveryStatus);
        }
    }


}
