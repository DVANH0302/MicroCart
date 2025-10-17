package com.example.deliveryco.dto.response;


import lombok.Data;

@Data
public class EventMessage {
    String type;
    Integer orderId;
    String message;
}
