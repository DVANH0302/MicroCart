package com.example.store.dto.request;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
public class OrderRequest {
    @NotNull
    private String username;
    
    @NotNull
    private Integer productId;
    
    @NotNull
    @Min(1)
    private Integer quantity;
    
    @NotNull
    private Double totalAmount;
}
