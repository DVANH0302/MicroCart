package com.example.store.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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