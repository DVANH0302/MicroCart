package com.example.store.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductStockResponse(
        Integer productId,
        String productName,
        BigDecimal price,
        Integer totalQuantity,
        List<WarehouseStock> warehouses
) {
    public record WarehouseStock(
            Integer warehouseId,
            String warehouseName,
            Integer quantity
    ) {
    }
}
