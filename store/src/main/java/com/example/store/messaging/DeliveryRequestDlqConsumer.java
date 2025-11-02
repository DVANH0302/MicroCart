package com.example.store.messaging;

import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.request.DeliveryRequest;
import com.example.store.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeliveryRequestDlqConsumer {
    private final EmailService emailService;
    public DeliveryRequestDlqConsumer(EmailService emailService) {
        this.emailService = emailService;
    }
    @RabbitListener(queues = RabbitMQConfig.DELIVERY_REQUEST_DLQ)
    public void handleDeliveryRequestDlq(DeliveryRequest deliveryRequest) {
        try{
            log.error("ALERT: DELIVERY REEQUEST FAILED FOR ORDER ID: " + deliveryRequest.getOrderId());
            emailService.sendAlertFailedDeliveryEmail(deliveryRequest.getOrderId());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
