package com.example.store.service.impl;

import com.example.store.dto.request.AvailabilityRequest;
import com.example.store.dto.request.BankPaymentRequest;
import com.example.store.dto.request.BankRefundRequest;
import com.example.store.dto.request.DeliveryRequest;
import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.request.ReleaseRequest;
import com.example.store.dto.request.ReserveRequest;
import com.example.store.dto.response.AvailabilityResponse;
import com.example.store.dto.response.BankPaymentResponse;
import com.example.store.dto.response.BankRefundResponse;
import com.example.store.dto.response.OrderResponse;
import com.example.store.dto.response.ReserveResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final DeliveryService deliveryService;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    private final long deliveryRequestDelayMs;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            InventoryService inventoryService,
            @Lazy DeliveryService deliveryService,
            @Lazy EmailService emailService,
            RestTemplate restTemplate,
            @Value("${store.delivery.request-delay-ms:10000}") long deliveryRequestDelayMs) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.deliveryService = deliveryService;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
        this.deliveryRequestDelayMs = deliveryRequestDelayMs;
    }

    @Transactional
    @Override
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Get user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new OrderException("User not found"));

        // 2. Check inventory availability
        AvailabilityRequest availabilityRequest = new AvailabilityRequest();
        availabilityRequest.setProductId(request.getProductId());
        availabilityRequest.setQuantity(request.getQuantity());
        AvailabilityResponse availability = inventoryService.plan(availabilityRequest);

        if (!availability.isCanFulfill()) {
            throw new OrderException("Product not available in requested quantity");
        }

        // 3. Reserve inventory
        ReserveRequest reserveRequest = new ReserveRequest();
        reserveRequest.setProductId(request.getProductId());
        reserveRequest.setQuantity(request.getQuantity());
        ReserveResponse reservation = inventoryService.reserve(reserveRequest);

        // 4. Create order
        List<Integer> expandedWarehouseIds = expandWarehouseAllocations(availability);
        Order order = Order.Builder.newBuilder()
                .user(user)
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(request.getTotalAmount())
                .status(DeliveryStatus.RECEIVED.name())
                .warehouseIds(new ArrayList<>(expandedWarehouseIds))
                .build();

        orderRepository.save(order);

        // 5. Process payment through Bank service
        try {
            String bankUrl = "http://bank-app:8083/api/bank/payment";
            BankPaymentRequest paymentRequest = BankPaymentRequest.builder()
                    .orderId(order.getId())
                    .fromAccount(user.getBankAccountId())
                    .toAccount("STORE_MAIN") // Store's bank account
                    .amount(BigDecimal.valueOf(request.getTotalAmount()))
                    .build();

            BankPaymentResponse paymentResponse = restTemplate.postForObject(
                    bankUrl, 
                    paymentRequest, 
                    BankPaymentResponse.class
            );

            if (paymentResponse != null && "SUCCESS".equals(paymentResponse.getStatus())) {
                order.setBankTransactionId(paymentResponse.getTransactionId());
                order.setStatus(DeliveryStatus.RECEIVED.name());
                orderRepository.save(order);

                // 6. Send delivery request
                scheduleDeliveryRequest(order, user, request, expandedWarehouseIds);

                return mapToOrderResponse(order);
            } else {
                // Payment failed - release inventory
                releaseReservedStock(order.getId(), request.getProductId(), reservation);
                order.setStatus(DeliveryStatus.CANCELLED.name());
                orderRepository.save(order);
                throw new OrderException("Payment failed");
            }
        } catch (Exception e) {
            // In case of any error - release inventory
            releaseReservedStock(order.getId(), request.getProductId(), reservation);
            order.setStatus(DeliveryStatus.CANCELLED.name());
            orderRepository.save(order);
            throw new OrderException("Order creation failed: " + e.getMessage());
        }
    }

    @Override
    public OrderResponse getOrder(Integer orderId) {
        Order order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));
        return mapToOrderResponse(order);
    }

    @Transactional
    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void updateStatus(Integer orderId, DeliveryStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));
        order.setStatus(status.name());
        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void refund(Integer orderId) {
        Order order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));

        DeliveryStatus currentStatus;
        try {
            currentStatus = DeliveryStatus.valueOf(order.getStatus());
        } catch (IllegalArgumentException e) {
            throw new OrderException("Invalid current order status: " + order.getStatus());
        }

        if (currentStatus != DeliveryStatus.RECEIVED) {
            throw new OrderException("Order cannot be cancelled once picked up by DeliveryCo");
        }
        
        try {
            // 1. Process refund through Bank service
            String bankUrl = "http://bank-app:8083/api/bank/refund";
            BankRefundRequest refundRequest = BankRefundRequest.builder()
                    .orderId(order.getId())
                    .originalTransactionId(order.getBankTransactionId())
                    .amount(BigDecimal.valueOf(order.getTotalAmount()))
                    .build();

            BankRefundResponse refundResponse = restTemplate.postForObject(
                    bankUrl, 
                    refundRequest, 
                    BankRefundResponse.class
            );

            if (refundResponse != null && "SUCCESS".equals(refundResponse.getStatus())) {
                // 2. Update order status
                order.setStatus(DeliveryStatus.CANCELLED.name());
                orderRepository.save(order);

                // 3. Return items to inventory
                releaseStockFromOrderRecord(order);

                // 4. Notify customer
                emailService.sendStatusEmail(order.getId(), DeliveryStatus.CANCELLED);
            } else {
                throw new OrderException("Refund failed");
            }
        } catch (Exception e) {
            throw new OrderException("Refund processing failed: " + e.getMessage());
        }
    }

    @Override
    public Optional<Order> findByOrderIdWithUser(Integer orderId) {
        return orderRepository.findByIdWithUser(orderId);
    }

    private List<Integer> expandWarehouseAllocations(AvailabilityResponse availability) {
        if (availability == null || availability.getAllocations() == null) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>();
        for (AvailabilityResponse.WarehouseAllocation allocation : availability.getAllocations()) {
            if (allocation == null || allocation.getWarehouseId() == null || allocation.getAvailable() == null) {
                continue;
            }
            int quantity = Math.max(0, allocation.getAvailable());
            for (int i = 0; i < quantity; i++) {
                result.add(allocation.getWarehouseId());
            }
        }
        return result;
    }

    private void releaseReservedStock(Integer orderId, Integer productId, ReserveResponse reservation) {
        if (reservation == null || reservation.getAllocations() == null || reservation.getAllocations().isEmpty()) {
            return;
        }
        ReleaseRequest releaseRequest = new ReleaseRequest();
        releaseRequest.setOrderId(orderId);
        releaseRequest.setProductId(productId);
        releaseRequest.setAllocations(cloneAllocations(reservation.getAllocations()));
        inventoryService.release(releaseRequest);
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

    private List<ReleaseRequest.Alloc> cloneAllocations(List<ReleaseRequest.Alloc> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            return Collections.emptyList();
        }
        return allocations.stream()
                .map(original -> {
                    ReleaseRequest.Alloc copy = new ReleaseRequest.Alloc();
                    copy.setWarehouseId(original.getWarehouseId());
                    copy.setQty(original.getQty());
                    return copy;
                })
                .collect(Collectors.toList());
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

    private void scheduleDeliveryRequest(
            Order order,
            User user,
            OrderRequest request,
            List<Integer> expandedWarehouseIds) {

        List<Integer> distinctWarehouseIds = expandedWarehouseIds.stream()
                .distinct()
                .collect(Collectors.toList());

        DeliveryRequest deliveryRequest = new DeliveryRequest(
                order.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                "TODO: Add address to User entity",
                request.getQuantity(),
                distinctWarehouseIds
        );

        Runnable task = () -> {
            try {
                Optional<Order> latestOrder = orderRepository.findById(order.getId());
                if (latestOrder.isEmpty()) {
                    log.warn("Order {} not found when attempting to send delayed delivery request", order.getId());
                    return;
                }

                String status = latestOrder.get().getStatus();
                if (DeliveryStatus.CANCELLED.name().equals(status)) {
                    log.info("Skipping delivery request for cancelled order {}", order.getId());
                    return;
                }

                log.info("Sending delayed delivery request for order {}", order.getId());
                deliveryService.requestDelivery(deliveryRequest);
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
