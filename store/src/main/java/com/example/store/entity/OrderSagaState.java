package com.example.store.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_saga_state")
public class OrderSagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Column(nullable = false)
    private String currentStep;

    private Integer productId;
    private Integer quantity;
    private String bankTransactionId;

    private String errorMessage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public OrderSagaState() {
    }

    private OrderSagaState(Builder builder) {
        orderId = builder.orderId;
        setStatus(builder.sagaStatus);
        setCurrentStep(builder.currentStep);
        productId = builder.productId;
        quantity = builder.quantity;
        bankTransactionId = builder.bankTransactionId;
        setErrorMessage(builder.errorMesage);
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public void setBankTransactionId(String bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }

    public void setErrorMessage(String errorMesage) {
        this.errorMessage = errorMesage;
    }

    public void setStatus(SagaStatus sagaStatus) {
        this.status = sagaStatus;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public Long getId() {
        return id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getBankTransactionId() {
        return bankTransactionId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static final class Builder {
        private Integer orderId;
        private SagaStatus sagaStatus;
        private String currentStep;
        private Integer productId;
        private Integer quantity;
        private String bankTransactionId;
        private String errorMesage;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder orderId(Integer val) {
            orderId = val;
            return this;
        }

        public Builder sagaStatus(SagaStatus val) {
            sagaStatus = val;
            return this;
        }

        public Builder currentStep(String val) {
            currentStep = val;
            return this;
        }

        public Builder productId(Integer val) {
            productId = val;
            return this;
        }

        public Builder quantity(Integer val) {
            quantity = val;
            return this;
        }

        public Builder bankTransactionId(String val) {
            bankTransactionId = val;
            return this;
        }

        public Builder errorMesage(String val) {
            errorMesage = val;
            return this;
        }

        public OrderSagaState build() {
            return new OrderSagaState(this);
        }
    }
}
