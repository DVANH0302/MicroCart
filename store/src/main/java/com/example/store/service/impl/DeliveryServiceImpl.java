package com.example.store.service.impl;


import com.example.store.config.RabbitMQConfig;
import com.example.store.dto.request.DeliveryRequest;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.service.DeliveryService;
import com.example.store.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {
    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;

    public DeliveryServiceImpl(RabbitTemplate rabbitTemplate, OrderService orderService) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderService = orderService;

    }

    public void requestDelivery(DeliveryRequest deliveryRequest) {
        log.info("SENDING deliveryRequest={}", deliveryRequest);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STORE_EXCHANGE,
                    RabbitMQConfig.DELIVERY_REQUEST_KEY,
                    deliveryRequest);
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
