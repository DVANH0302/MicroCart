package com.example.store.dto.request;

import com.example.store.entity.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeliveryFailureAlert {
    private final Integer orderId;
    private final DeliveryStatus deliveryStatus;
    private final LocalDateTime timestamp;
}
