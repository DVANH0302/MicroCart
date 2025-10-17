package com.example.store.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReserveRequest {
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
}
