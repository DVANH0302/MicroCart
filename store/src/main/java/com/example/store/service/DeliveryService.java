package com.example.store.service;

import com.example.store.dto.request.DeliveryRequest;
import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.response.DeliveryUpdate;
import com.example.store.entity.Order;
import com.example.store.entity.User;

import java.util.List;

public interface DeliveryService {

    void handleUpdate(DeliveryUpdate deliveryUpdate);

    void scheduleDeliveryRequest(
            Order order,
            User user,
            OrderRequest request,
            List<Integer> expandedWarehouseIds);
}
