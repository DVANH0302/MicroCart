package com.example.store.service;


import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.request.DeliveryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StoreService {
    private final RabbitTemplate rabbitTemplate;

    public StoreService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void requestDelivery(DeliveryRequest deliveryRequest) {
        log.info("deliveryRequest={}", deliveryRequest);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STORE_EXCHANGE,
                    RabbitMQConfig.DELIVERY_REQUEST_KEY,
                    deliveryRequest);
        } catch (Exception e) {
            log.error("deliveryRequest={}", deliveryRequest, e.getMessage());
            throw new RuntimeException("Failed to deliver delivery request");
        }
    }

    public void handleAreadyExist(Integer orderId) {
        log.info("TODO:  handleAreadyExist={}", orderId);
        log.info("TODO: SET ORDER STATUS FAILED");
    }
}
