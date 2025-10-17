package com.example.store.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class WarehouseStockId implements Serializable {
    private Integer warehouseId;
    private Integer productId;
}
