package com.example.deliveryco.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeliveryRequest {
    private int orderId;
    private String userFullName;
    private String userEmail;
    private String address;
    private Integer quantity;
    private List<Integer> warehouseIds;
}
