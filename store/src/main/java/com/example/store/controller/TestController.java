package com.example.store.controller;


import com.example.store.dto.request.DeliveryRequest;
import com.example.store.service.DeliveryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final DeliveryService deliveryService;

    public TestController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public String sendDeliveryRequest(){
        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "ABC123",
                "123",
                "345",
                3,
                List.of("1", "2", "3")
        );
        deliveryService.requestDelivery(deliveryRequest);
        return "success";
    }
}
