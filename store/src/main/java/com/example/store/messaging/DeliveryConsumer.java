package com.example.store.messaging;

import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.service.DeliveryService;
import com.example.store.service.EmailService;
import com.example.store.service.impl.DeliveryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeliveryConsumer {
    private final DeliveryService deliveryService;
    private final EmailService emailService;

    public DeliveryConsumer(DeliveryService deliveryService,  EmailService emailService) {
        this.deliveryService = deliveryService;
        this.emailService = emailService;
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
            log.info("TODO: SEND EMAIL");
            deliveryService.handleUpdate(deliveryUpdate);
            emailService.sendStatusEmail(deliveryUpdate.getOrderId(), deliveryUpdate.getStatus());

        } catch (RuntimeException e) {
            log.error("Failed to send delivery update for order {}", deliveryUpdate.getOrderId());
            throw new RuntimeException(e);
        }
    }

}
