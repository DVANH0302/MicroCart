package com.example.deliveryco.service;

import com.example.deliveryco.config.RabbitMQConfig;
import com.example.deliveryco.dto.request.DeliveryFailureAlert;
import com.example.deliveryco.dto.response.DeliveryUpdate;
import com.example.deliveryco.entity.Delivery;
import com.example.deliveryco.entity.DeliveryStatus;
import com.example.deliveryco.messaging.CustomCorrelationData;
import com.example.deliveryco.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class DeliveryUpdateService {

    private final DeliveryRepository deliveryRepository;


    public DeliveryUpdateService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public void saveStatus(Integer orderId, DeliveryStatus deliveryStatus) {
        Delivery chosenDelivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery order not found"));
        chosenDelivery.setStatus(deliveryStatus.name());
        deliveryRepository.save(chosenDelivery);
    }

    @Transactional
    public void saveFailedStatus(Integer orderId, DeliveryStatus deliveryStatus) {
        Delivery chosenDelivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery order not found"));
        chosenDelivery.setStatus("FAILED_" +  deliveryStatus.name());
        deliveryRepository.save(chosenDelivery);
    }

}
