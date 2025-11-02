package com.example.store.service.impl;

import com.example.store.entity.OrderSagaState;
import com.example.store.entity.SagaStatus;
import com.example.store.exception.OrderSagaException;
import com.example.store.repository.OrderSagaStateRepository;
import com.example.store.service.OrderSagaStateService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderSagaStateServiceImpl implements OrderSagaStateService {


    private final OrderSagaStateRepository orderSagaStateRepository;

    public OrderSagaStateServiceImpl(OrderSagaStateRepository orderSagaStateRepository) {
        this.orderSagaStateRepository = orderSagaStateRepository;
    }

    @Transactional
    @Override
    public OrderSagaState initiateSaga(Integer productId, Integer quantity) {
        try {
            OrderSagaState saga = OrderSagaState.Builder.newBuilder()
                    .sagaStatus(SagaStatus.STARTED)
                    .productId(productId)
                    .quantity(quantity)
                    .currentStep("STARTED")
                    .build();
            return orderSagaStateRepository.save(saga);
        }catch (Exception e){
            throw new OrderSagaException("Saga initiated error");
        }
    }

    @Override
    @Transactional
    public OrderSagaState complete(OrderSagaState saga, String currentStep) {
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);
    }


    @Override
    @Transactional
    public OrderSagaState updateSagaToCompensating(OrderSagaState saga, String currentStep) {
        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);    }

    @Override
    public OrderSagaState updateSagaToCompensated(OrderSagaState saga, String currentStep) {
        saga.setStatus(SagaStatus.COMPENSATED);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);
    }

    @Override
    public OrderSagaState updateSagaToFailed(OrderSagaState saga, String compensationFailed, String error) {
        saga.setStatus(SagaStatus.FAILED);
        saga.setCurrentStep(compensationFailed);
        saga.setErrorMessage(error);
        return orderSagaStateRepository.save(saga);
    }


    @Override
    @Transactional
    public OrderSagaState updateSagaToDeliveryRequested(OrderSagaState saga, String currentStep) {
        saga.setStatus(SagaStatus.DELIVERY_REQUESTED);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);
    }


    @Override
    @Transactional
    public OrderSagaState updateSagaToPaymentCompleted(OrderSagaState saga, String currentStep, String transactionId) {
        saga.setBankTransactionId(transactionId);
        saga.setStatus(SagaStatus.PAYMENT_COMPLETED);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);
    }

    @Override
    @Transactional
    public OrderSagaState updateSagaToOrderCreated(OrderSagaState saga, String currentStep, Integer orderId) {
        saga.setOrderId(orderId);
        saga.setStatus(SagaStatus.ORDER_CREATED);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);
    }

    @Override
    @Transactional
    public OrderSagaState updateSagaToInventoryReserved(OrderSagaState saga, String currentStep) {
        saga.setStatus(SagaStatus.INVENTORY_RESERVED);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);
    }

    @Override
    @Transactional
    public OrderSagaState updateSagaToInventoryAvailable(OrderSagaState saga, String currentStep) {
        saga.setStatus(SagaStatus.INVENTORY_AVAILABLE);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);
    }

    @Override
    @Transactional
    public OrderSagaState updateSagaToUserValidated(OrderSagaState saga, String currentStep) {
        saga.setStatus(SagaStatus.USER_VALIDATED);
        saga.setCurrentStep(currentStep);
        return orderSagaStateRepository.save(saga);
    }


    @Override
    public Optional<OrderSagaState> findById(Long sagaId) {
        return orderSagaStateRepository.findById(sagaId);
    }


}
