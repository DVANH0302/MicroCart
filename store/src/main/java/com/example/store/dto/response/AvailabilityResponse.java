package com.example.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.*;

@Getter @Setter
public class AvailabilityResponse {
    private boolean canFulfill;
    private List<WarehouseAllocation> allocations;

    @Getter @Setter @AllArgsConstructor
    public static class WarehouseAllocation{
        private Integer warehouseId;
        private Integer available;
    }
}
