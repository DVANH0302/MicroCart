package com.example.store.service;

import com.example.store.entity.OrderSagaState;

import java.util.Optional;

public interface OrderSagaStateService {
    OrderSagaState initiateSaga(Integer productId, Integer quantity);

    OrderSagaState updateSagaToUserValidated(OrderSagaState saga, String currentStep);
    OrderSagaState updateSagaToInventoryAvailable(OrderSagaState saga, String currentStep);
    OrderSagaState updateSagaToInventoryReserved(OrderSagaState saga, String currentStep);
    OrderSagaState updateSagaToOrderCreated(OrderSagaState saga, String currentStep, Integer orderId);
    OrderSagaState updateSagaToPaymentCompleted(OrderSagaState saga, String currentStep, String transactionId);
    OrderSagaState updateSagaToDeliveryRequested(OrderSagaState saga, String currentStep);

    OrderSagaState complete(OrderSagaState saga, String currentStep);

    OrderSagaState updateSagaToCompensating(OrderSagaState saga, String currentStep);
    OrderSagaState updateSagaToCompensated(OrderSagaState saga, String currentStep);
    OrderSagaState updateSagaToFailed(OrderSagaState saga, String currentStep, String error);


    Optional<OrderSagaState> findById(Long sagaId);

}
