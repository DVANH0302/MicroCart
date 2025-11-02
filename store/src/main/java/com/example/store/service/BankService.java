package com.example.store.service;

import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.response.BankPaymentResponse;
import com.example.store.dto.response.BankRefundResponse;
import com.example.store.entity.Order;
import com.example.store.entity.OrderSagaState;
import com.example.store.entity.User;

public interface BankService {
    BankPaymentResponse processPayment(User user, Order order, OrderRequest orderRequest);

    BankRefundResponse refund(Integer orderId);
}
