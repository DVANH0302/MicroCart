package com.example.store.messaging;

import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DeliveryUpdateDlqConsumer {

    private final EmailService emailService;

    public DeliveryUpdateDlqConsumer(EmailService emailService) {
        this.emailService = emailService;
    }


    @RabbitListener(queues = RabbitMQConfig.DELIVERY_UPDATE_DLQ)
    public void handleDlqMessage(
            DeliveryUpdate deliveryUpdate
    ) {
        try {
            emailService.sendAlertFailedDeliveryEmail(deliveryUpdate.getOrderId());
        } catch (Exception e) {
            log.error("Failed to handle DLQ message", e);
            throw new RuntimeException(e);
        }
    }


}
