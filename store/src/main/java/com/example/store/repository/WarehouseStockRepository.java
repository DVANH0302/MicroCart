package com.example.store.repository;


import com.example.store.entity.WarehouseStock;
import com.example.store.entity.WarehouseStockId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.*;

public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, WarehouseStockId> {
    
    
    @Query("""
        select ws from WarehouseStock ws
        where ws.productId = :pid
        order by ws.quantity desc
    """)
    List<WarehouseStock> findAllForProduct(@Param("pid") Integer productId);
    
    // Lock all rows for this product to serialize reservations and avoid oversell
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ws from WarehouseStock ws
        where ws.productId = :pid
        order by ws.quantity desc
    """)
    List<WarehouseStock> findAllForProductForUpdate(@Param("pid") Integer productId);

    Optional<WarehouseStock> findByWarehouseIdAndProductId(Integer warehouseId, Integer productId);
}