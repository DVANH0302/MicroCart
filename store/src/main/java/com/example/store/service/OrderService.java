package com.example.store.service;

import com.example.store.entity.DeliveryStatus;
import com.example.store.entity.Order;

import java.util.Optional;

public interface OrderService {
    void save(Order order);
    void updateStatus(Integer orderId, DeliveryStatus status);
    void refund(Integer orderId);
    Optional<Order> findByOrderIdWithUser(Integer orderId);
}
