package com.example.emailservice.service;

import com.example.emailservice.dto.request.StatusEmailDto;

public interface EmailService {
    void processingEmail(StatusEmailDto statusEmailDto);
}
