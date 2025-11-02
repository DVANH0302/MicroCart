package com.example.store.dto.request;

import java.util.*;
import java.util.stream.Collectors;

import com.example.store.entity.Order;
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

    public static ReleaseRequest createFromOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (order.getWarehouseIds() == null || order.getWarehouseIds().isEmpty()) {
            throw new IllegalArgumentException("Order has no warehouse allocations");
        }
        Map<Integer, Long> warehouseQuantities = order.getWarehouseIds().stream()
                .collect(Collectors.groupingBy(
                        warehouseId -> warehouseId,
                        Collectors.counting()
                ));
        List<Alloc> allocations = warehouseQuantities.entrySet().stream()
                .map(entry -> {
                    Alloc alloc = new Alloc();
                    alloc.setWarehouseId(entry.getKey());
                    alloc.setQty(entry.getValue().intValue());
                    return alloc;
                })
                .collect(Collectors.toList());
        ReleaseRequest request = new ReleaseRequest();
        request.setOrderId(order.getId());
        request.setProductId(order.getProductId());
        request.setAllocations(allocations);

        return request;
    }
}
