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

        log.info("Successfully saved deliveryRequest: {}", deliveryRequest);
        log.info("Delivery after being saved: {}", delivery);

        // simulation
         int orderId = deliveryRequest.getOrderId();
         try {
             // PICKED_UP
             log.info("orderId {} is PICKED UP", orderId);
             Thread.sleep(5000);
             processUpdate(orderId, DeliveryStatus.PICKED_UP);

             // ON_DELIVERY
             log.info("orderId {} is  ON_DELIVERY", orderId);
             Thread.sleep(5000);
             processUpdate(orderId, DeliveryStatus.ON_DELIVERY);

             // DELIVERED OR LOST (5%)
             Thread.sleep(5000);
             if (random.nextInt(100) < 5) {
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

    public void sendUpdate(int orderId,  DeliveryStatus deliveryStatus, String message) {


        CustomCorrelationData correlationData = CustomCorrelationData.builder()
                .id("order-" + orderId + "-" + UUID.randomUUID().toString())
                .orderId(orderId)
                .build();
        correlationData.getFuture().whenComplete((r, e) -> {
            if (e != null) {
                alertStoreSystemFailure(orderId, deliveryStatus);
            } else if (r != null || r.isAck()) {
                // for demo purpose of what happenning when ack
//                log.info("Alert failure demo in ack");
//                alertStoreSystemFailure(orderId, deliveryStatus);
//                deliveryUpdateService.saveFailedStatus(orderId, deliveryStatus);
                return;

            } else{
                //nack
                alertStoreSystemFailure(orderId, deliveryStatus);
                deliveryUpdateService.saveFailedStatus(orderId, deliveryStatus);
            }
        });

        DeliveryUpdate deliveryUpdate = DeliveryUpdate.builder()
                .orderId(orderId)
                .status(deliveryStatus)
                .timeStamp(LocalDateTime.now())
                .message(message)
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STORE_EXCHANGE,
                RabbitMQConfig.getStatusKey(deliveryStatus),
                deliveryUpdate,
                correlationData
        );
    }


    private void alertStoreSystemFailure(Integer orderId, DeliveryStatus deliveryStatus) {
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
