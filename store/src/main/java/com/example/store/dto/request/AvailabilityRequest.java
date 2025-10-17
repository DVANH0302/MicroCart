package com.example.store.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AvailabilityRequest {
    private Integer productId;
    private Integer quantity;
}
