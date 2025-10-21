package com.example.store.entity;


import com.example.store.converter.IntListToStringConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Table(name="orders", schema = "store")
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "product_id")
    private int productId;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name ="status")
    private String status;

    @Column(name = "bank_transaction_id")
    private String bankTransactionId;

//    @Column(name = "delivery_id")
//    private String deliveryId;

    @Column(name = "warehouse_ids")
    @Convert(converter = IntListToStringConverter.class)
    private List<Integer> warehouseIds;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private  LocalDateTime updatedAt;

    public Order() {

    }

    private Order(Builder builder) {
        setUserId(builder.userId);
        productId = builder.productId;
        setQuantity(builder.quantity);
        setTotalAmount(builder.totalAmount);
        setStatus(builder.deliveryStatus);
        setBankTransactionId(builder.bankTransactionId);
        setWarehouseIds(builder.warehouseIds);
    }

    public int getProductId() {
        return productId;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String deliveryStatus) {
        this.status = deliveryStatus;
    }

    public String getBankTransactionId() {
        return bankTransactionId;
    }

    public void setBankTransactionId(String bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }

    public List<Integer> getWarehouseIds() {
        return warehouseIds;
    }

    public void setWarehouseIds(List<Integer> warehouseIds) {
        this.warehouseIds = warehouseIds;
    }


    public static final class Builder {
        private int userId;
        private int productId;
        private int quantity;
        private double totalAmount;
        private String deliveryStatus;
        private String bankTransactionId;
        private List<Integer> warehouseIds;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder userId(int val) {
            userId = val;
            return this;
        }

        public Builder productId(int val) {
            productId = val;
            return this;
        }

        public Builder quantity(int val) {
            quantity = val;
            return this;
        }

        public Builder totalAmount(double val) {
            totalAmount = val;
            return this;
        }

        public Builder deliveryStatus(String val) {
            deliveryStatus = val;
            return this;
        }

        public Builder bankTransactionId(String val) {
            bankTransactionId = val;
            return this;
        }

        public Builder warehouseIds(List<Integer> val) {
            warehouseIds = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
