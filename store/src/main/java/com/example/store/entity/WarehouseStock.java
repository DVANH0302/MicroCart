package com.example.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouse_stock", schema = "store")
@IdClass(WarehouseStockId.class)
@Getter @Setter
public class WarehouseStock {
    @Id @Column(name = "warehouse_id")
    private Integer warehouseId;

    @Id @Column(name = "product_id")
    private Integer productId;

    @Column(name = "quantity")
    private Integer quantity;
}
