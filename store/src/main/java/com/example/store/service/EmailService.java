package com.example.store.service;

import com.example.store.dto.request.StatusEmailDto;
import com.example.store.entity.DeliveryStatus;

public interface EmailService {
    void sendStatusEmail(Integer orderId, DeliveryStatus status);
    void sendAlertFailedDeliveryEmail(Integer orderId);
}
