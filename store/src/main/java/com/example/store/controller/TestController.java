package com.example.store.controller;


import com.example.store.dto.request.DeliveryRequest;
import com.example.store.service.StoreService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final StoreService deliveryService;

    public TestController(StoreService storeService) {
        this.deliveryService = storeService;
    }

    @PostMapping
    public String sendDeliveryRequest(){
        DeliveryRequest deliveryRequest = new DeliveryRequest(
                123,
                "Andy",
                "doanvietanh03022003@gmai.com",
                "sydney",
                10,
                List.of(1, 2, 3)
        );
        deliveryService.requestDelivery(deliveryRequest);
        return "success";
    }
}
