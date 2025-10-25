package com.example.store.dto.request;


import com.example.store.entity.EmailType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusEmailDto {
    private String recipient;
    private String subject;
    private String body;
    private EmailType emailType;
    private Integer orderId;
}
