package com.example.store.controller;


import com.example.store.dto.request.DeliveryFailureAlert;
import com.example.store.entity.DeliveryStatus;
import com.example.store.service.BankService;
import com.example.store.service.EmailService;
import com.example.store.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery")
@Slf4j
public class DeliveryExternalController {


    private final OrderService orderService;
    private final BankService bankService;

    public DeliveryExternalController(OrderService orderService, BankService bankService) {
        this.orderService = orderService;
        this.bankService = bankService;
    }

    @PostMapping("/alert")
    public ResponseEntity<Void> handleAlert(@RequestBody DeliveryFailureAlert deliveryFailureAlert) {
        try{
            log.info("ALERT: DELIVERY UPDATE SEND VIA RESTAPI SINCE MESSAGE BROKER IS ERROR FOR ORDER ID: {}", deliveryFailureAlert.getOrderId());
            orderService.updateStatus(deliveryFailureAlert.getOrderId(), deliveryFailureAlert.getDeliveryStatus());
            if (deliveryFailureAlert.getDeliveryStatus() == DeliveryStatus.LOST){
                bankService.refund(deliveryFailureAlert.getOrderId());
            }
            return  ResponseEntity.ok().build();
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
