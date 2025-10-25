package com.example.store.service.impl;


import com.example.store.config.EmailConfig;
import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.request.StatusEmailDto;
import com.example.store.entity.DeliveryStatus;
import com.example.store.entity.EmailType;
import com.example.store.entity.Order;
import com.example.store.entity.User;
import com.example.store.messaging.CustomCorrelationData;
import com.example.store.service.EmailService;
import com.example.store.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;

    public EmailServiceImpl(RabbitTemplate rabbitTemplate, OrderService orderService) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderService = orderService;
    }


    @Override
    @Async
    public void sendStatusEmail(Integer orderId,  DeliveryStatus status) {
        try {
            Order order = orderService.findByOrderIdWithUser(orderId).orElseThrow(
                    () -> new RuntimeException(String.format("The order id %d is not found!", orderId))
            );

            User user = order.getUser();


            EmailType emailType;
            try {
                if (status == null) throw new IllegalArgumentException("status is null");
                emailType = EmailType.valueOf(status.name());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("No matching EmailType for DeliveryStatus: " + status, e);
            }

            StatusEmailDto statusEmailDto = StatusEmailDto.builder()
                    .recipient(user.getEmail())
                    .subject(EmailConfig.getSubject(emailType, orderId))
                    .body(EmailConfig.getBody(emailType, orderId))
                    .emailType(emailType)
                    .orderId(orderId)
                    .build();

            CustomCorrelationData correlationData = CustomCorrelationData.builder()
                    .id("order-" +  orderId +"-"+ UUID.randomUUID())
                    .queueType(RabbitMQConfig.EMAIL_QUEUE)
                    .orderId(orderId)
                    .build();


            log.info("EMAIL SENDING FOR ORDERID {}  WITH EMAIL TYPE: {}", statusEmailDto.getOrderId(), statusEmailDto.getEmailType());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STORE_EXCHANGE,
                    RabbitMQConfig.EMAIL_KEY,
                    statusEmailDto,
                    correlationData
            );
        } catch (Exception e) {
            log.info("EMAIL SENDING FAILED of order {}: {} ", orderId, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendAlertFailedDeliveryEmail(Integer orderId) {
        try{
            Order order = orderService.findByOrderIdWithUser(orderId).orElseThrow(
                    () -> new RuntimeException(String.format("The order id %d is not found!", orderId))
            );

            User user = order.getUser();
            EmailType emailType = EmailType.FAILED_PROCESSING;


            CustomCorrelationData correlationData = CustomCorrelationData.builder()
                    .id("order-" +  orderId +"-"+ UUID.randomUUID())
                    .queueType(RabbitMQConfig.EMAIL_QUEUE)
                    .orderId(orderId)
                    .build();

            StatusEmailDto statusEmailDto = StatusEmailDto.builder()
                    .recipient(user.getEmail())
                    .body(EmailConfig.getBody(emailType, orderId))
                    .subject(EmailConfig.getSubject(emailType, orderId))
                    .orderId(orderId)
                    .emailType(emailType)
                    .build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STORE_EXCHANGE,
                    RabbitMQConfig.EMAIL_KEY,
                    statusEmailDto,
                    correlationData
            );
        }catch (Exception e){
            log.info("ALERT EMAIL SENDING FAILED of order {}: {} ", orderId, e.getMessage(), e);
        }
    }

}
