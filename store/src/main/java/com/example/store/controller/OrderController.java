package com.example.store.controller;

import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.response.OrderResponse;
import com.example.store.entity.Order;
import com.example.store.service.OrderOrchestrator;
import com.example.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final OrderOrchestrator orderOrchestrator;


    public OrderController(OrderService orderService, OrderOrchestrator orderOrchestrator) {
        this.orderService = orderService;
        this.orderOrchestrator = orderOrchestrator;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderResponse>> getAllOrders(@PathVariable Integer userId) {
        log.info("getAllOrders");
        List<OrderResponse> orderResponses = orderService.getAllOrders(userId);
        return ResponseEntity.ok(orderResponses);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Received order creation request: {}", request);
        OrderResponse response = orderOrchestrator.executeOrderCreation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Integer orderId) {
        log.info("Fetching order details for orderId: {}", orderId);
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<OrderResponse> refundOrder(@PathVariable Integer orderId) {
        log.info("Received refund request for orderId: {}", orderId);
        orderOrchestrator.executeRefund(orderId);
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
