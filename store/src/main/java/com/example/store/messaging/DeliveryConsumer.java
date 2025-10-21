package com.example.store.messaging;

import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.service.DeliveryService;
import com.example.store.service.impl.DeliveryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeliveryConsumer {
    private final DeliveryService deliveryService;

    public DeliveryConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_UPDATE_QUEUE)
    public void handleDeliveryUpdate(
        DeliveryUpdate deliveryUpdate

    ) {
        try {
//            // TESTING DLQ PURPOSE
//            if (true){
//                throw new RuntimeException("TEST FOR DLQ");
//            }
            log.info("TODO: UPDATE ORDER STATUS IN DB");
            log.info("TODO: SEND EMAIL");
            deliveryService.handleUpdate(deliveryUpdate);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
