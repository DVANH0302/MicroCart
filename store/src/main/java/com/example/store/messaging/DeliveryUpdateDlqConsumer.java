package com.example.store.messaging;

import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.service.BankService;
import com.example.store.service.EmailService;
import com.example.store.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DeliveryUpdateDlqConsumer {

    private final EmailService emailService;
    private final BankService bankService;
    private final OrderService orderService;

    public DeliveryUpdateDlqConsumer(EmailService emailService, BankService bankService, OrderService orderService) {
        this.emailService = emailService;
        this.bankService = bankService;
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_UPDATE_DLQ)
    public void handleDlqMessage(
            DeliveryUpdate deliveryUpdate

    ) {
        try {
            log.error("DELIVERY UPDATE FAILED: PROCESSING FOR ORDER ID : " + deliveryUpdate.getOrderId());

            log.error("DELIVERY UPDATE FAILED: SENDING ALERT EMAIL TO USER FOR ORDER ID : " + deliveryUpdate.getOrderId());
            emailService.sendAlertFailedDeliveryEmail(deliveryUpdate.getOrderId());

            log.error("DELIVERY UPDATE FAILED: CANCELLING IN DB FOR ORDER ID : " + deliveryUpdate.getOrderId());
            orderService.cancelOrder(deliveryUpdate.getOrderId());

            log.error("DELIVERY UPDATE FAILED: REFUNDING FOR USER FOR ORDER ID : " + deliveryUpdate.getOrderId());
            bankService.refund(deliveryUpdate.getOrderId());

        } catch (Exception e) {
            log.error("Failed to handle DLQ message", e);
            throw new RuntimeException(e);
        }
    }


}
