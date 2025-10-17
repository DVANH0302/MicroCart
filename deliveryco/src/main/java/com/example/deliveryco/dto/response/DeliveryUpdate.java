package com.example.deliveryco.dto.response;

import com.example.deliveryco.entity.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeliveryUpdate {
    private int orderId;
    private DeliveryStatus status;
    private LocalDateTime timeStamp;
}
