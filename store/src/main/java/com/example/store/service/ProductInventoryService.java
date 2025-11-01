package com.example.store.service;

import com.example.store.dto.response.ProductStockResponse;

import java.util.List;

public interface ProductInventoryService {
    List<ProductStockResponse> getProductStockSnapshot();
}
