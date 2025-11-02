package com.example.store.messaging;

import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.response.BankRefundResponse;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.entity.DeliveryStatus;
import com.example.store.exception.OrderException;
import com.example.store.service.BankService;
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
    private final BankService bankService;

    public DeliveryConsumer(DeliveryService deliveryService,  EmailService emailService, BankService bankService) {
        this.deliveryService = deliveryService;
        this.emailService = emailService;
        this.bankService = bankService;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_UPDATE_QUEUE)
    public void handleDeliveryUpdate(
        DeliveryUpdate deliveryUpdate

    ) {
        try {

            deliveryService.handleUpdate(deliveryUpdate);

            if (deliveryUpdate.getStatus() == DeliveryStatus.LOST) {
                log.info("DELIVERY IS LOST, REFUNDING FOR ORDER ID {}",  deliveryUpdate.getOrderId());
                BankRefundResponse refundResponse =  bankService.refund(deliveryUpdate.getOrderId());
                if (refundResponse == null || !"SUCCESS".equals(refundResponse.getStatus())) {
                    throw new OrderException("Bank refund failed for order: " + deliveryUpdate.getOrderId());
                }
            }

            emailService.sendStatusEmail(deliveryUpdate.getOrderId(), deliveryUpdate.getStatus());

        } catch (RuntimeException e) {
            log.error("Failed to send delivery update for order {}", deliveryUpdate.getOrderId());
            throw new RuntimeException(e);
        }
    }

}
