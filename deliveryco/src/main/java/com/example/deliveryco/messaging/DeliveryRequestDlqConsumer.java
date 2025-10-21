package com.example.deliveryco.messaging;

import com.example.deliveryco.config.RabbitMQConfig;
import com.example.deliveryco.dto.request.DeliveryRequest;
import com.example.deliveryco.service.DeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "delivery.request.dlq.enabled", havingValue = "true")
public class DeliveryRequestDlqConsumer {
    private final DeliveryService deliveryService;

    public DeliveryRequestDlqConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_REQUEST_DLQ)
    public void handleDeliveryRequestDlq(DeliveryRequest deliveryRequest) {
        try{
            log.info("PROCESSING DELIVERY REQUEST {}",  deliveryRequest);
            deliveryService.processDelivery(deliveryRequest);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
