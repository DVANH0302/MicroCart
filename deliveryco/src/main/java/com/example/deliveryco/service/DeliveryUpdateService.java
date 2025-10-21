package com.example.deliveryco.service;

import com.example.deliveryco.config.RabbitMQConfig;
import com.example.deliveryco.dto.response.DeliveryUpdate;
import com.example.deliveryco.entity.Delivery;
import com.example.deliveryco.entity.DeliveryStatus;
import com.example.deliveryco.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliveryUpdateService {

    private final DeliveryRepository deliveryRepository;
    private final RabbitTemplate rabbitTemplate;

    public DeliveryUpdateService(DeliveryRepository deliveryRepository,  RabbitTemplate rabbitTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void saveStatus(Integer orderId, DeliveryStatus deliveryStatus) {
        Delivery chosenDelivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery order not found"));
        chosenDelivery.setStatus(deliveryStatus.name());
        deliveryRepository.save(chosenDelivery);
    }

    public void sendUpdate(int orderId,  DeliveryStatus deliveryStatus, String message) {
        DeliveryUpdate deliveryUpdate = DeliveryUpdate.builder()
                .orderId(orderId)
                .status(deliveryStatus)
                .timeStamp(LocalDateTime.now())
                .message(message)
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STORE_EXCHANGE,
                RabbitMQConfig.getStatusKey(deliveryStatus),
                deliveryUpdate
        );
    }
}
