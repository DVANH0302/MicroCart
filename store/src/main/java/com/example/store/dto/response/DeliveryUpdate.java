package com.example.store.dto.response;

import com.example.store.entity.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeliveryUpdate {
    private int orderId;
    private DeliveryStatus status;
    private LocalDateTime timeStamp;
    private String message;
}
