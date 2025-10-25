package com.example.deliveryco.dto.request;

import com.example.deliveryco.entity.DeliveryStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.convert.DataSizeUnit;

import java.time.LocalDateTime;

@Data
@Builder
public class DeliveryFailureAlert {
    private final Integer orderId;
    private final DeliveryStatus deliveryStatus;
    private final LocalDateTime timestamp;
}
