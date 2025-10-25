package com.example.store.messaging;


import lombok.Builder;
import lombok.Getter;
import org.springframework.amqp.rabbit.connection.CorrelationData;


@Getter
public class CustomCorrelationData extends CorrelationData {
    private String queueType;
    private Integer orderId;

    @Builder
    public CustomCorrelationData(String id, String queueType, Integer orderId) {
        super(id);
        this.queueType = queueType;
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "CustomCorrelationData{" +
                "queueType='" + queueType + '\'' +
                ", orderId=" + orderId +
                '}';
    }
}
