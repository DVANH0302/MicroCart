package com.example.store.controller;


import com.example.store.dto.request.DeliveryFailureAlert;
import com.example.store.service.EmailService;
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

    private final EmailService emailService;

    public  DeliveryExternalController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/alert")
    public ResponseEntity<Void> handleAlert(@RequestBody DeliveryFailureAlert deliveryFailureAlert) {
        try{
            log.info("ALERT: DELIVERY sent system error when {} for order id: {} at {}", deliveryFailureAlert.getDeliveryStatus(),deliveryFailureAlert.getOrderId(), deliveryFailureAlert.getTimestamp());
            log.info("ALERT: Sending alert to customer");
            emailService.sendAlertFailedDeliveryEmail(deliveryFailureAlert.getOrderId());
            return  ResponseEntity.ok().build();
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
