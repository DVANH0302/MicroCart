package com.example.store.service.impl;

import com.example.store.dto.request.*;
import com.example.store.dto.response.*;
import com.example.store.entity.*;
import com.example.store.exception.OrderException;
import com.example.store.exception.OrderSagaException;
import com.example.store.service.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class OrderOrchestratorImpl implements OrderOrchestrator {

    private final OrderSagaStateService orderSagaStateService;
    private final OrderService orderService;
    private final UserService userService;;
    private final InventoryService inventoryService;
    private final BankService bankService;
    private final DeliveryService deliveryService;
    private final EmailService emailService;


    public OrderOrchestratorImpl(
            OrderService orderService,
            OrderSagaStateService orderSagaStateService,
            UserService userService,
            InventoryService inventoryService,
            BankService bankService,
            @Lazy DeliveryService deliveryService,
            @Lazy EmailService emailService
    ) {
        this.orderSagaStateService = orderSagaStateService;
        this.orderService = orderService;
        this.userService = userService;
        this.inventoryService = inventoryService;
        this.bankService = bankService;
        this.deliveryService = deliveryService;
        this.emailService = emailService;
    }

    @Override
    public OrderResponse executeOrderCreation(OrderRequest orderRequest) {
        // 0. Create saga state
        log.info("Starting order creation saga for user: {}", orderRequest.getUsername());
        OrderSagaState saga = orderSagaStateService.initiateSaga(
                orderRequest.getProductId(),
                orderRequest.getQuantity()
        );
        try{

            // 1. Get user
            log.info("Step 1: Validating user {}", orderRequest.getUsername());
            User user = userService.findByUsername(orderRequest.getUsername())
                    .orElseThrow(() -> new OrderSagaException("User not found"));

            saga = orderSagaStateService.updateSagaToUserValidated(saga,  "USER_VALIDATED");

            // 2. Check inventory availability
            log.info("Step 2: Checking inventory availability");
            AvailabilityRequest availabilityRequest = new AvailabilityRequest();
            availabilityRequest.setProductId(orderRequest.getProductId());
            availabilityRequest.setQuantity(orderRequest.getQuantity());
            AvailabilityResponse availability = inventoryService.plan(availabilityRequest);

            if (!availability.isCanFulfill()) {
                throw new OrderException("Product not available in requested quantity");
            }

            saga = orderSagaStateService.updateSagaToInventoryAvailable(saga,  "INVENTORY_AVAILABLE");

            // 3. Reserve inventory
            log.info("Step 3: Reserving inventory");
            ReserveRequest reserveRequest = new ReserveRequest();
            reserveRequest.setProductId(orderRequest.getProductId());
            reserveRequest.setQuantity(orderRequest.getQuantity());
            ReserveResponse reservation = inventoryService.reserve(reserveRequest);

            saga = orderSagaStateService.updateSagaToInventoryReserved(saga, "INVENTORY_RESERVED");

            // 4. Create order
            log.info("Step 4: Creating order record");
            List<Integer> expandedWarehouseIds = expandWarehouseAllocations(availability);
            Order order = orderService.initiateOrder(user, orderRequest, expandedWarehouseIds);
            saga = orderSagaStateService.updateSagaToOrderCreated(saga,  "ORDER_CREATED", order.getId());

            // 5. Process payment through Bank service
            log.info("Step 5: Processing payment");
            BankPaymentResponse paymentResponse = bankService.processPayment(user, order, orderRequest);
            saga = orderSagaStateService.updateSagaToPaymentCompleted(saga, "PAYMENT_COMPLETED", paymentResponse.getTransactionId());

            // 6. Schedule delivery request
            log.info("Step 6: Scheduling delivery");
            deliveryService.scheduleDeliveryRequest(order, user, orderRequest, expandedWarehouseIds);
            saga = orderSagaStateService.updateSagaToDeliveryRequested(saga,  "DELIVERY_REQUESTED");

            // 7. Complete
            log.info("Order creation saga completed successfully for order: {}", order.getId());
            saga = orderSagaStateService.complete(saga, "COMPLETED");

            return mapToOrderResponse(order);
        } catch (Exception e){
            log.error("Order of user {} which has product id: {} failed at step {}", orderRequest.getUsername(), orderRequest.getProductId(), saga.getCurrentStep());

            compensate(saga.getId());
            throw new OrderSagaException("Order Creation Failed: " + e.getMessage());
        }


    }

    private void compensate(Long sagaId) {
        log.info("Starting compensation saga for saga id: {}", sagaId);


        // Step 0 - find saga, retrieve the step and update the status

        OrderSagaState saga = orderSagaStateService.findById(sagaId)
                .orElseThrow(() -> new OrderSagaException("Saga not found for id" + sagaId + "; required manual modification."));
        SagaStatus lastStatus  = saga.getStatus();
        saga = orderSagaStateService.updateSagaToCompensating(saga, "COMPENSATING_" +  lastStatus);
        List<String> failSteps = new ArrayList<>();
        switch (lastStatus) {
            case PAYMENT_COMPLETED:
                boolean result1 = compensatePayment(saga);
                if (!result1) {
                    failSteps.add("FAILED_PAYMENT");
                }
            case ORDER_CREATED:
                boolean result2 = compensateOrder(saga);
                if (!result2) {
                    failSteps.add("FAILED_ORDER_CREATED");
                }
            case INVENTORY_RESERVED:
                boolean result3 = compensateInventory(saga);
                if (!result3) {
                    failSteps.add("FAILED_INVENTORY_RESERVED");
                }
                break;
            case INVENTORY_AVAILABLE:
                log.info("No compensation action required for step {}", lastStatus);
                break;
            case USER_VALIDATED:
                log.info("No compensation action required for step {}", lastStatus);
                break;
            case STARTED:
                log.info("No compensation action required for step {}", lastStatus);
                break;
            default:
                log.warn("Unknown compensation action required for step {}", lastStatus);
        }

        if (failSteps.isEmpty()) {
            saga = orderSagaStateService.updateSagaToCompensated(saga, "COMPENSATED");
        } else {
            String error = "Compensation Failed: " + String.join(", ", failSteps);
            saga =  orderSagaStateService.updateSagaToFailed(saga,"COMPENSATION_FAILED", error);
        }

    }



    private boolean compensatePayment(OrderSagaState saga) {
        try {
            log.info("Compensating Payment: Refund payment");
            BankRefundResponse refundResponse =  bankService.refund(saga.getOrderId());
            if (refundResponse == null || !"SUCCESS".equals(refundResponse.getStatus())) {
                throw new OrderSagaException("Refund Failed for transaction id: " + refundResponse.getTransactionId());
            }
            log.info("Compensating Payment: Successful refund");
            return true;
        } catch (Exception e) {
            log.error("Compensating Payment Failed", e);
            return false;

        }
    }

    private boolean compensateOrder(OrderSagaState saga) {
        try {
            log.info("Compensating Order: Cancel order");
            orderService.cancelOrder(saga.getOrderId());
            log.info("Compensating Order: Successful cancelled order");
            return true;
        }catch (Exception e) {
            log.error("Compensating Order Failed", e);
            return false;
        }
    }


    private boolean compensateInventory(OrderSagaState saga) {
        try {
            log.info("Compensating Inventory: Release inventory");
            Order order = orderService.findById(saga.getOrderId())
                            .orElseThrow(() -> new OrderSagaException("Order not found for id: " + saga.getOrderId()));
            ReleaseRequest releaseRequest = ReleaseRequest.createFromOrder(order);
            inventoryService.release(releaseRequest);
            log.info("Compensating Inventory: Successful release inventory");
            return true;
        }catch (Exception e) {
            log.error("Compensating Inventory Failed", e);
            return false;
        }
    }

    @Override
    public void executeRefund(Integer orderId) {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found for id: " + orderId));
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
            log.info("Start processing refund request for order {}", orderId);
            bankService.refund(orderId);
            log.info("Refund successful, updating order status");
            orderService.cancelOrder(order.getId());

            log.info("Releasing the inventory");
            inventoryService.release(ReleaseRequest.createFromOrder(order));

            log.info("Refund process completed successfully for order: {}", orderId);
        }catch (Exception e) {
            log.error("Refund process failed for order: {}", orderId, e);
            throw new OrderException("Refund process failed: " + e.getMessage());}

        emailService.sendStatusEmail(order.getId(), DeliveryStatus.CANCELLED);
    }


    // helper
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
