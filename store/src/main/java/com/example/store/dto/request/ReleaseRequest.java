package com.example.store.dto.request;

import java.util.*;

import lombok.Setter;
import lombok.Getter;

@Getter @Setter
public class ReleaseRequest {
    private Integer orderId;
    private Integer productId;
    private List<Alloc> allocations;

    @Getter @Setter
    public static class Alloc {
        private Integer warehouseId;
        private Integer qty;
    }
    
}
