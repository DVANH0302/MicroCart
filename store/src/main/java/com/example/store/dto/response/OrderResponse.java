package com.example.store.dto.response;

import com.example.store.entity.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Integer orderId;
    private String username;
    private Integer productId;
    private Integer quantity;
    private Double totalAmount;
    private String status;
    private String bankTransactionId;
    private List<Integer> warehouseIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}