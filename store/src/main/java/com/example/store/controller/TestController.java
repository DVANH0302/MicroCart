package com.example.store.controller;


import com.example.store.dto.request.DeliveryRequest;
import com.example.store.entity.Order;
import com.example.store.repository.OrderRepository;
import com.example.store.service.DeliveryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final DeliveryService deliveryService;
    private final OrderRepository orderRepository;

    public TestController(DeliveryService deliveryService, OrderRepository orderRepository) {
        this.deliveryService = deliveryService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public String sendDeliveryRequest(){
        List<Order> orders = orderRepository.findByUserId(1).orElseThrow(
                () -> new RuntimeException("order for user id not found: " + 1)
        );
        DeliveryRequest deliveryRequest = new DeliveryRequest(
                orders.get(0).getId(),
                "Andy",
                "doanvietanh03022003@gmai.com",
                "sydney",
                10,
                List.of(1, 2, 3)
        );
        deliveryService.requestDelivery(deliveryRequest);
        return "success";
    }
}
