package com.example.store.entity;


import com.example.store.converter.IntListToStringConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name="orders", schema = "store")
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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
        user = builder.user;
        productId = builder.productId;
        quantity = builder.quantity;
        totalAmount = builder.totalAmount;
        status = builder.status;
        bankTransactionId = builder.bankTransactionId;
        warehouseIds = builder.warehouseIds;
    }

    public void setUser(User user){
        if (this.user == user) {return;}
        User previous = this.user;
        this.user = user;

        if (previous != null && previous.getOrders() != null) {
            previous.getOrders().remove(this);
        }

        if (user != null) {
            if (user.getOrders() == null) {
                user.setOrders( new ArrayList<>());
            }
            if (!user.getOrders().contains(this)) {
                user.getOrders().add(this);
            }
        }
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBankTransactionId(String bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }

    public void setWarehouseIds(List<Integer> warehouseIds) {
        this.warehouseIds = warehouseIds;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getBankTransactionId() {
        return bankTransactionId;
    }

    public List<Integer> getWarehouseIds() {
        return warehouseIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public static final class Builder {
        private User user;
        private int productId;
        private int quantity;
        private double totalAmount;
        private String status;
        private String bankTransactionId;
        private List<Integer> warehouseIds;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder user(User val) {
            user = val;
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

        public Builder status(String val) {
            status = val;
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
