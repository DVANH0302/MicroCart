package com.example.emailservice.dto.request;

import com.example.emailservice.entity.EmailType;
import lombok.Data;

@Data
public class StatusEmailDto {
    private String recipient;
    private String subject;
    private String body;
    private EmailType emailType;
    private Integer OrderId;
}
