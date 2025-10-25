package com.example.store.service.impl;


import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.request.DeliveryRequest;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.messaging.CustomCorrelationData;
import com.example.store.service.DeliveryService;
import com.example.store.service.EmailService;
import com.example.store.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {
    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;
    private final EmailService emailService;

    public DeliveryServiceImpl(RabbitTemplate rabbitTemplate, OrderService orderService,  EmailService emailService) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderService = orderService;
        this.emailService = emailService;

    }

    public void requestDelivery(DeliveryRequest deliveryRequest) {
        log.info("SENDING deliveryRequest={}", deliveryRequest);
        try {
            CustomCorrelationData correlationData = CustomCorrelationData.builder()
                    .id("order-" +  deliveryRequest.getOrderId() +"-"+ UUID.randomUUID())
                    .queueType(RabbitMQConfig.DELIVERY_REQUEST_QUEUE)
                    .orderId(deliveryRequest.getOrderId())
                    .build();

            // callback to handle future confirm
            correlationData.getFuture().whenComplete((r, e) -> {
               if (e != null) {
                    emailService.sendAlertFailedDeliveryEmail(deliveryRequest.getOrderId());
               } else if (r != null && r.isAck()){
                   return;
               } else{
                   // NACK
                   emailService.sendAlertFailedDeliveryEmail(deliveryRequest.getOrderId());
               }
            });

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STORE_EXCHANGE,
                    RabbitMQConfig.DELIVERY_REQUEST_KEY,
                    deliveryRequest,
                    correlationData);

        } catch (Exception e) {
            log.error("deliveryRequest={}", deliveryRequest, e.getMessage());
            throw new RuntimeException("Failed to deliver delivery request");
        }
    }

    @Override
    public void handleUpdate(DeliveryUpdate deliveryUpdate) {
        orderService.updateStatus(deliveryUpdate.getOrderId(), deliveryUpdate.getStatus());
    }
}
