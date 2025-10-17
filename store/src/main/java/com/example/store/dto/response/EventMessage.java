package com.example.store.dto.response;


import lombok.Data;

@Data
public class EventMessage {
    String type;
    Integer orderId;
    String message;
}
