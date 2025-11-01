package com.example.store.controller;

import com.example.store.dto.response.ProductStockResponse;
import com.example.store.service.ProductInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductInventoryController {

    private final ProductInventoryService productInventoryService;

    @GetMapping("/stock")
    public List<ProductStockResponse> getProductStock() {
        return productInventoryService.getProductStockSnapshot();
    }
}
