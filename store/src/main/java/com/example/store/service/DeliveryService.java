package com.example.store.service;

import com.example.store.dto.request.DeliveryRequest;
import com.example.store.dto.response.DeliveryUpdate;

public interface DeliveryService {
    void requestDelivery(DeliveryRequest deliveryRequest);

    void handleUpdate(DeliveryUpdate deliveryUpdate);
}
