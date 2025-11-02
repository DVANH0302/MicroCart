package com.example.store.service;

import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.response.OrderResponse;
import com.example.store.entity.DeliveryStatus;
import com.example.store.entity.Order;
import com.example.store.entity.User;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    OrderResponse getOrder(Integer orderId);
    List<OrderResponse> getAllOrders(Integer userId);
    void updateStatus(Integer orderId, DeliveryStatus status);
    Optional<Order> findByIdWithUser(Integer orderId);
    Optional<Order> findById(Integer orderId);
    Optional<List<Order>> findByUserId(Integer userId);
    Order initiateOrder(User user, OrderRequest request, List<Integer> expandedWarehouseIds);
    void cancelOrder(Integer orderId);
    void updateBankTransactionId(int id, String transactionId);

}
