package com.example.store.service.impl;

import com.example.store.dto.request.BankPaymentRequest;
import com.example.store.dto.request.BankRefundRequest;
import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.response.BankPaymentResponse;
import com.example.store.dto.response.BankRefundResponse;
import com.example.store.entity.Order;
import com.example.store.entity.User;
import com.example.store.exception.OrderException;
import com.example.store.service.BankService;
import com.example.store.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;


@Service
public class BankServiceImpl implements BankService {
    private final RestTemplate restTemplate;
    private final OrderService orderService;

    public BankServiceImpl(RestTemplate restTemplate, OrderService orderService) {
        this.restTemplate = restTemplate;
        this.orderService = orderService;
    }

    @Override
    public BankPaymentResponse processPayment(User user, Order order, OrderRequest request) {
//        String bankUrl = "http://bank-app:8083/api/bank/payment";
        String bankUrl = "http://localhost:8083/api/bank/payment";
        BankPaymentRequest paymentRequest = BankPaymentRequest.builder()
                .orderId(order.getId())
                .fromAccount(user.getBankAccountId())
                .toAccount("STORE_MAIN")
                .amount(BigDecimal.valueOf(request.getTotalAmount()))
                .build();

        BankPaymentResponse paymentResponse = restTemplate.postForObject(
                bankUrl, paymentRequest, BankPaymentResponse.class);

        if (paymentResponse == null || !"SUCCESS".equals(paymentResponse.getStatus())) {
            throw new OrderException("Payment failed");
        }
        orderService.updateBankTransactionId(order.getId(), paymentResponse.getTransactionId());
        return paymentResponse;
    }

    @Override
    public BankRefundResponse refund(Integer orderId) {
        Order order = orderService.findByIdWithUser(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));
//        String bankUrl = "http://bank-app:8083/api/bank/refund";
        String bankUrl = "http://localhost:8083/api/bank/refund";
        BankRefundRequest refundRequest = BankRefundRequest.builder()
                .orderId(order.getId())
                .originalTransactionId(order.getBankTransactionId())
                .amount(BigDecimal.valueOf(order.getTotalAmount()))
                .build();

        BankRefundResponse refundResponse = restTemplate.postForObject(
                bankUrl,
                refundRequest,
                BankRefundResponse.class
        );
        return refundResponse;
    }

}
