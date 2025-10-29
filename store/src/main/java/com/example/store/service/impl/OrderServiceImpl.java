package com.example.store.service.impl;

import com.example.store.dto.request.AvailabilityRequest;
import com.example.store.dto.request.DeliveryRequest;
import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.request.ReserveRequest;
import com.example.store.dto.response.AvailabilityResponse;
import com.example.store.dto.response.OrderResponse;
import com.example.store.dto.response.ReserveResponse;
import com.example.store.entity.DeliveryStatus;
import com.example.store.entity.Order;
import com.example.store.entity.User;
import com.example.store.exception.OrderException;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.UserRepository;
import com.example.store.service.DeliveryService;
import com.example.store.service.InventoryService;
import com.example.store.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final DeliveryService deliveryService;
    private final RestTemplate restTemplate;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            InventoryService inventoryService,
            DeliveryService deliveryService,
            RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.deliveryService = deliveryService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    @Override
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Get user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new OrderException("User not found"));

        // 2. Check inventory availability
        AvailabilityResponse availability = inventoryService.plan(AvailabilityRequest.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build());

        if (!availability.isAvailable()) {
            throw new OrderException("Product not available in requested quantity");
        }

        // 3. Reserve inventory
        ReserveResponse reservation = inventoryService.reserve(ReserveRequest.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build());

        // 4. Create order
        Order order = Order.Builder.newBuilder()
                .user(user)
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(request.getTotalAmount())
                .status(DeliveryStatus.REQUESTED.name())
                .warehouseIds(availability.getWarehouseIds())
                .build();

        orderRepository.save(order);

        // 5. Process payment through Bank service
        try {
            String bankUrl = "http://localhost:8083/api/bank/payment";
            BankPaymentRequest paymentRequest = BankPaymentRequest.builder()
                    .fromAccountId(user.getBankAccountId())
                    .toAccountId("STORE_ACCOUNT") // Store's bank account
                    .amount(request.getTotalAmount())
                    .orderId(order.getId())
                    .build();

            BankPaymentResponse paymentResponse = restTemplate.postForObject(
                    bankUrl, 
                    paymentRequest, 
                    BankPaymentResponse.class
            );

            if (paymentResponse != null && "SUCCESS".equals(paymentResponse.getStatus())) {
                order.setBankTransactionId(paymentResponse.getTransactionId());
                order.setStatus(DeliveryStatus.PAYMENT_CONFIRMED.name());
                orderRepository.save(order);

                // 6. Send delivery request
                deliveryService.requestDelivery(DeliveryRequest.builder()
                        .orderId(order.getId())
                        .userFullName(user.getFirstName() + " " + user.getLastName())
                        .userEmail(user.getEmail())
                        .address("TODO: Add address to User entity")
                        .quantity(request.getQuantity())
                        .warehouseIds(availability.getWarehouseIds())
                        .build());

                return mapToOrderResponse(order);
            } else {
                // Payment failed - release inventory
                inventoryService.release(ReserveRequest.builder()
                        .productId(request.getProductId())
                        .quantity(request.getQuantity())
                        .build());
                throw new OrderException("Payment failed");
            }
        } catch (Exception e) {
            // In case of any error - release inventory
            inventoryService.release(ReserveRequest.builder()
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build());
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
        
        try {
            // 1. Process refund through Bank service
            String bankUrl = "http://localhost:8083/api/bank/refund";
            BankRefundRequest refundRequest = BankRefundRequest.builder()
                    .originalTransactionId(order.getBankTransactionId())
                    .orderId(order.getId())
                    .build();

            BankRefundResponse refundResponse = restTemplate.postForObject(
                    bankUrl, 
                    refundRequest, 
                    BankRefundResponse.class
            );

            if (refundResponse != null && "SUCCESS".equals(refundResponse.getStatus())) {
                // 2. Update order status
                order.setStatus(DeliveryStatus.REFUNDED.name());
                orderRepository.save(order);

                // 3. Return items to inventory if delivery was cancelled (not lost)
                if (DeliveryStatus.CANCELLED.name().equals(order.getStatus())) {
                    inventoryService.release(ReserveRequest.builder()
                            .productId(order.getProductId())
                            .quantity(order.getQuantity())
                            .build());
                }
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
