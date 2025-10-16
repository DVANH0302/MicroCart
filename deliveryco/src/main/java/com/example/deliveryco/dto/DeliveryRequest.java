package com.example.deliveryco.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeliveryRequest {
    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private List<String> warehouseIds;
}
