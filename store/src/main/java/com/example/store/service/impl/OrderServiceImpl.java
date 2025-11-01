package com.example.store.service.impl;

import com.example.store.dto.request.BankRefundRequest;
import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.request.ReleaseRequest;
import com.example.store.dto.response.BankRefundResponse;
import com.example.store.dto.response.OrderResponse;
import com.example.store.entity.DeliveryStatus;
import com.example.store.entity.Order;
import com.example.store.entity.User;
import com.example.store.exception.OrderException;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.UserRepository;
import com.example.store.service.DeliveryService;
import com.example.store.service.EmailService;
import com.example.store.service.InventoryService;
import com.example.store.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            InventoryService inventoryService,
            @Lazy EmailService emailService,
            RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    @Override
    public Order initiateOrder(User user, OrderRequest request, List<Integer> expandedWarehouseIds) {
        Order order = Order.Builder.newBuilder()
                .user(user)
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(request.getTotalAmount())
                .status(DeliveryStatus.RECEIVED.name())
                .warehouseIds(new ArrayList<>(expandedWarehouseIds))
                .build();
        return orderRepository.save(order);
    }

    @Transactional
    @Override
    public void cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderException("Order not found"));
        order.setStatus(DeliveryStatus.CANCELLED.name());
        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void updateBankTransactionId(int id, String transactionId) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderException("Order not found"));
        order.setBankTransactionId(transactionId);
        orderRepository.save(order);
    }


    @Override
    public OrderResponse getOrder(Integer orderId) {
        Order order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));
        return mapToOrderResponse(order);
    }


    @Transactional
    @Override
    public void updateStatus(Integer orderId, DeliveryStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));
        order.setStatus(status.name());
        orderRepository.save(order);
    }


    @Override
    public Optional<Order> findByIdWithUser(Integer orderId) {
        return orderRepository.findByIdWithUser(orderId);
    }

    @Override
    public Optional<Order> findById(Integer orderId){
        return orderRepository.findById(orderId);
    }


    private void releaseStockFromOrderRecord(Order order) {
        if (order == null || order.getWarehouseIds() == null || order.getWarehouseIds().isEmpty()) {
            return;
        }
        Map<Integer, Long> grouped = order.getWarehouseIds().stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
        List<ReleaseRequest.Alloc> allocations = grouped.entrySet().stream()
                .map(entry -> {
                    ReleaseRequest.Alloc alloc = new ReleaseRequest.Alloc();
                    alloc.setWarehouseId(entry.getKey());
                    alloc.setQty(entry.getValue().intValue());
                    return alloc;
                })
                .collect(Collectors.toList());
        if (allocations.isEmpty()) {
            return;
        }
        ReleaseRequest releaseRequest = new ReleaseRequest();
        releaseRequest.setOrderId(order.getId());
        releaseRequest.setProductId(order.getProductId());
        releaseRequest.setAllocations(allocations);
        inventoryService.release(releaseRequest);
    }


    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .username(order.getUser().getUsername())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .bankTransactionId(order.getBankTransactionId())
                .warehouseIds(order.getWarehouseIds())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }


}
