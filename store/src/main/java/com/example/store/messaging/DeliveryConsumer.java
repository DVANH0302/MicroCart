package com.example.store.messaging;

import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.request.DeliveryUpdate;
import com.example.store.dto.response.EventMessage;
import com.example.store.service.StoreService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeliveryConsumer {
    private final StoreService storeService;

    public DeliveryConsumer(StoreService storeService) {
        this.storeService = storeService;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_UPDATE_QUEUE)
    public void handleDeliveryUpdate(
        DeliveryUpdate deliveryUpdate,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) {
        try {
            log.info("TODO: UPDATE ORDER STATUS IN DB");
            log.info("TODO: SEND EMAIL");
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.STORE_DELIVERY_EVENT_QUEUE)
    public void handleDeliveryEvent(
        EventMessage event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) {
        try {
            log.info("Received Delivery Event: {}", event);
            switch(event.getType()) {
                case "delivery.already_exists" -> storeService.handleAreadyExist(event.getOrderId());
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
