package com.example.deliveryco.messaging;


import lombok.Builder;
import lombok.Getter;
import org.springframework.amqp.rabbit.connection.CorrelationData;


@Getter
public class CustomCorrelationData extends CorrelationData {
    private Integer orderId;

    @Builder
    public CustomCorrelationData(String id, Integer orderId) {
        super(id);
        this.orderId = orderId;
    }
}
