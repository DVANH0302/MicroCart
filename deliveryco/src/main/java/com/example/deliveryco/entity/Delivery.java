package com.example.deliveryco.entity;


import com.example.deliveryco.converter.IntListToStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "deliveries", schema = "delivery")
@Getter @Setter
public class Delivery {

    @Id
    @Column(name = "delivery_id", length = 36, nullable = false, updatable = false)
    private String deliveryId;

    @Column(name = "order_id")
    private int orderId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "address")
    private String address;

    @Column(name = "warehouse_ids")
    @Convert(converter = IntListToStringConverter.class)
    private List<Integer> warehouseIds;

    @Column(name = "status")
    private String status;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Delivery() {
    }

    private Delivery(Builder builder) {
        setOrderId(builder.orderId);
        setCustomerName(builder.customerName);
        setCustomerEmail(builder.customerEmail);
        setAddress(builder.address);
        setWarehouseIds(builder.warehouseIds);
        setStatus(builder.status);
    }

    @PrePersist
    protected void onCreate() {
        if (this.deliveryId == null || this.deliveryId.isEmpty()) {
            this.deliveryId = UUID.randomUUID().toString();
        }
    }


    public static final class Builder {
        private int orderId;
        private String customerName;
        private String customerEmail;
        private String address;
        private List<Integer> warehouseIds;
        private String status;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder orderId(int val) {
            orderId = val;
            return this;
        }

        public Builder customerName(String val) {
            customerName = val;
            return this;
        }

        public Builder customerEmail(String val) {
            customerEmail = val;
            return this;
        }

        public Builder address(String val) {
            address = val;
            return this;
        }

        public Builder warehouseIds(List<Integer> val) {
            warehouseIds = val;
            return this;
        }

        public Builder status(String val) {
            status = val;
            return this;
        }

        public Delivery build() {
            return new Delivery(this);
        }
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "deliveryId='" + deliveryId + '\'' +
                ", orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", address='" + address + '\'' +
                ", warehouseIds=" + warehouseIds +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
