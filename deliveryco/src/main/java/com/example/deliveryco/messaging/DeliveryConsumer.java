package com.example.deliveryco.messaging;


import com.example.deliveryco.config.RabbitMQConfig;
import com.example.deliveryco.dto.DeliveryRequest;
import com.example.deliveryco.service.DeliveryService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeliveryConsumer {
    private final DeliveryService deliveryService;

    public DeliveryConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_REQUEST_QUEUE)
    public void handleDeliveryRequest(
            DeliveryRequest deliveryRequest,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) {
        try {
            deliveryService.processDelivery(deliveryRequest);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
