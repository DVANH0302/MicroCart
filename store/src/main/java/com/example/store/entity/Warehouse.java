package com.example.store.entity;


import jakarta.persistence.*;
import lombok.*;

public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Integer warehouseId;

    @Column(name = "warehouse_name")
    private String warehouseName;

    private String address;

}
