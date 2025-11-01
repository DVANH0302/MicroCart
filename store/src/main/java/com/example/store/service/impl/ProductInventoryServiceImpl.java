package com.example.store.service.impl;

import com.example.store.dto.response.ProductStockResponse;
import com.example.store.service.ProductInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductInventoryServiceImpl implements ProductInventoryService {

    private final JdbcTemplate jdbcTemplate;

    private static final String STOCK_QUERY = """
            SELECT
                p.product_id,
                p.product_name,
                p.price,
                w.warehouse_id,
                w.warehouse_name,
                ws.quantity
            FROM store.products p
            LEFT JOIN store.warehouse_stock ws ON ws.product_id = p.product_id
            LEFT JOIN store.warehouses w ON w.warehouse_id = ws.warehouse_id
            ORDER BY p.product_id, w.warehouse_id
            """;

    @Override
    public List<ProductStockResponse> getProductStockSnapshot() {
        Map<Integer, ProductAccumulator> aggregates = new LinkedHashMap<>();

        jdbcTemplate.query(STOCK_QUERY, rs -> {
            int productId = rs.getInt("product_id");
            ProductAccumulator accumulator = aggregates.get(productId);
            if (accumulator == null) {
                String name = rs.getString("product_name");
                BigDecimal price = rs.getBigDecimal("price");
                accumulator = new ProductAccumulator(productId, name, price);
                aggregates.put(productId, accumulator);
            }

            Integer warehouseId = (Integer) rs.getObject("warehouse_id");
            if (warehouseId != null) {
                String warehouseName = rs.getString("warehouse_name");
                int quantity = rs.getInt("quantity");
                if (rs.wasNull()) {
                    quantity = 0;
                }
                accumulator.addWarehouse(warehouseId, warehouseName, quantity);
            }
        });

        return aggregates.values().stream()
                .map(ProductAccumulator::toResponse)
                .toList();
    }

    private static final class ProductAccumulator {
        private final Integer productId;
        private final String productName;
        private final BigDecimal price;
        private final List<ProductStockResponse.WarehouseStock> warehouses = new ArrayList<>();
        private int totalQuantity;

        private ProductAccumulator(Integer productId, String productName, BigDecimal price) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.totalQuantity = 0;
        }

        private void addWarehouse(Integer warehouseId, String warehouseName, int quantity) {
            warehouses.add(new ProductStockResponse.WarehouseStock(warehouseId, warehouseName, quantity));
            totalQuantity += Math.max(quantity, 0);
        }

        private ProductStockResponse toResponse() {
            return new ProductStockResponse(
                    productId,
                    productName,
                    price,
                    totalQuantity,
                    List.copyOf(warehouses)
            );
        }
    }
}
