package com.example.deliveryco.service;

import com.example.deliveryco.dto.DeliveryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeliveryService {

    private final RabbitTemplate rabbitTemplate;

    public DeliveryService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void processDelivery(DeliveryRequest deliveryRequest) {
        log.info("Successfully recieved deliveryRequest: {}", deliveryRequest);
    }
}
