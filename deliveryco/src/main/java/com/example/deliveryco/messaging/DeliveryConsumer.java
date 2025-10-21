package com.example.deliveryco.messaging;


import com.example.deliveryco.config.RabbitMQConfig;
import com.example.deliveryco.dto.request.DeliveryRequest;
import com.example.deliveryco.service.DeliveryService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class DeliveryConsumer {
    private final DeliveryService deliveryService;

    public DeliveryConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_REQUEST_QUEUE)
    public void handleDeliveryRequest(
            DeliveryRequest deliveryRequest
    ) {
        try {
////            FOR DLQ TESTING DEMO
//            if (true) {
//                throw new RuntimeException("TESTING DLQ");
//            }
            log.info("PROCESSING DELIVERY REQUEST {}",  deliveryRequest);
            deliveryService.processDelivery(deliveryRequest);

        } catch (RuntimeException e) {
            log.error("Error processing delivery request order {}", deliveryRequest.getOrderId());
            throw new RuntimeException(e);
        }
    }



}
