package com.example.store.dto.request;

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
}
