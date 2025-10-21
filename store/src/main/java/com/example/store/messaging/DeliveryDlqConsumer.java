package com.example.store.messaging;

import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.service.DeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@ConditionalOnProperty(name = "delivery.dlq.consumer.enabled", havingValue = "true")
public class DeliveryDlqConsumer {
    private final DeliveryService deliveryService;
    public DeliveryDlqConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_UPDATE_DLQ)
    public void handleDlqMessage(
            DeliveryUpdate deliveryUpdate
    ) {
        try {
            log.info("PROCESSING DLQ Message from queue: {}", deliveryUpdate);
            deliveryService.handleUpdate(deliveryUpdate);
        } catch (Exception e) {
            log.error("Failed to handle DLQ message", e);
            throw new RuntimeException(e);
        }
    }


}
