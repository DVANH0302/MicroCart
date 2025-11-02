package com.example.store.service.impl;


import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.request.DeliveryRequest;
import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.entity.DeliveryStatus;
import com.example.store.entity.Order;
import com.example.store.entity.User;
import com.example.store.exception.OrderSagaException;
import com.example.store.messaging.CustomCorrelationData;
import com.example.store.service.DeliveryService;
import com.example.store.service.EmailService;
import com.example.store.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {
    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;
    private final EmailService emailService;
    private final long deliveryRequestDelayMs;


    public DeliveryServiceImpl(RabbitTemplate rabbitTemplate,
                               @Lazy OrderService orderService,
                               @Lazy EmailService emailService,
                               @Value("${store.delivery.request-delay-ms:10000}") long deliveryRequestDelayMs) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderService = orderService;
        this.emailService = emailService;
        this.deliveryRequestDelayMs = deliveryRequestDelayMs;

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

    @Override
    public void scheduleDeliveryRequest(
            Order order,
            User user,
            OrderRequest request,
            List<Integer> expandedWarehouseIds) {

        try {
            Connection connection = rabbitTemplate.getConnectionFactory().createConnection();
            connection.close();
            log.info("CONNECTION: Message queue connection validated");
        } catch (Exception e) {
            log.error("CONNECTION FAILED: Message Queue unreachable", e);
            throw new OrderSagaException("Message queue unavailable " + e.getMessage());
        }


        List<Integer> distinctWarehouseIds = expandedWarehouseIds.stream()
                .distinct()
                .collect(Collectors.toList());

        DeliveryRequest deliveryRequest = new DeliveryRequest(
                order.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                "Sydney",
                request.getQuantity(),
                distinctWarehouseIds
        );

        Runnable task = () -> {
            try {
                Optional<Order> latestOrder = orderService.findById(order.getId());
                if (latestOrder.isEmpty()) {
                    log.warn("Order {} not found when attempting to send delayed delivery request", order.getId());
                    return;
                }

                String status = latestOrder.get().getStatus();
                if (DeliveryStatus.CANCELLED.name().equals(status)) {
                    log.info("DELIVERY REQUEST CANCELLED FOR ORDER {} BEFORE SNEDING DELIVERY", order.getId());
                    return;
                }

                log.info("Sending delayed delivery request for order {}", order.getId());
                requestDelivery(deliveryRequest);
            } catch (Exception ex) {
                log.error("Failed to send delayed delivery request for order {}", order.getId(), ex);
            }
        };

        CompletableFuture.runAsync(
                task,
                CompletableFuture.delayedExecutor(deliveryRequestDelayMs, TimeUnit.MILLISECONDS)
        );
    }
}
