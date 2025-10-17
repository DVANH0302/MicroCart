package com.example.deliveryco.service;

import com.example.deliveryco.config.RabbitMQConfig;
import com.example.deliveryco.dto.request.DeliveryRequest;
import com.example.deliveryco.dto.response.DeliveryUpdate;
import com.example.deliveryco.dto.response.EventMessage;
import com.example.deliveryco.entity.Delivery;
import com.example.deliveryco.entity.DeliveryStatus;
import com.example.deliveryco.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public DeliveryService(RabbitTemplate rabbitTemplate, DeliveryRepository deliveryRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.deliveryRepository = deliveryRepository;
    }

    public void processDelivery(DeliveryRequest deliveryRequest) {
        log.info("Successfully recieved deliveryRequest: {}", deliveryRequest);
        Delivery delivery = Delivery.Builder.newBuilder()
                .orderId(deliveryRequest.getOrderId())
                .customerName(deliveryRequest.getUserFullName())
                .customerEmail(deliveryRequest.getUserEmail())
                .address(deliveryRequest.getAddress())
                .warehouseIds(deliveryRequest.getWarehouseIds())
                .status(DeliveryStatus.REQUESTED.name())
                .build();

        if (deliveryRepository.findByOrderId(deliveryRequest.getOrderId()).isPresent()) {
            String message = String.format("Order ID %d already exists", deliveryRequest.getOrderId());
            sendMessageToStore("delivery.already_exists", deliveryRequest.getOrderId(), message);
            log.error("Order ID {} already exists", deliveryRequest.getOrderId());
            return;
        }
        deliveryRepository.save(delivery);
        log.info("Successfully saved deliveryRequest: {}", deliveryRequest);
        log.info("Delivery after being saved: {}", delivery);

         new Thread(() -> {
             int orderId = deliveryRequest.getOrderId();
             try {
                 // PICKED_UP
                 log.info("orderId {} is PICKED UP", orderId);
                 Thread.sleep(5000);
                 saveAndSendUpdate(orderId, DeliveryStatus.PICKED_UP);

                 // ON_DELIVERY
                 log.info("orderId {} is  ON_DELIVERY", orderId);
                 Thread.sleep(5000);
                 saveAndSendUpdate(orderId, DeliveryStatus.ON_DELIVERY);

                 // DELIVERED OR LOST (5%)
                 Thread.sleep(5000);
                 if (random.nextInt(100) < 5) {
                     log.info("orderId {} is  LOST", orderId);
                     saveAndSendUpdate(orderId, DeliveryStatus.LOST);
                 } else{
                     log.info("orderId {} is  DELIVERED", orderId);
                     saveAndSendUpdate(orderId, DeliveryStatus.DELIVERED);
                 }

             } catch (Exception e) {
                 log.info("orderId delivery failed {}", orderId);
                 Thread.currentThread().interrupt();
             }
         }).start();
    }

    public void sendMessageToStore(String eventType, Integer orderId, String message) {
        EventMessage eventMessage = new EventMessage();
        eventMessage.setOrderId(orderId);
        eventMessage.setType(eventType);
        eventMessage.setMessage(message);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STORE_EXCHANGE,
                RabbitMQConfig.DELIVERY_REQUEST_REJECT_KEY,
                eventMessage
        );
    }

    @Transactional
    public void saveAndSendUpdate(int orderId, DeliveryStatus deliveryStatus) {
        Delivery chosenDelivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery order not found"));
        chosenDelivery.setStatus(deliveryStatus.name());
        deliveryRepository.save(chosenDelivery);

        DeliveryUpdate message =  DeliveryUpdate.builder()
                .orderId(orderId)
                .status(deliveryStatus)
                .timeStamp(LocalDateTime.now())
                .build();
        log.info("Delivery update sent to exchange: {}", RabbitMQConfig.STORE_EXCHANGE);
        rabbitTemplate.convertAndSend(RabbitMQConfig.STORE_EXCHANGE, RabbitMQConfig.DELIVERY_UPDATE_KEY, message);
    }
}
