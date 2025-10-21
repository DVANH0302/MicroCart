package com.example.store.service.impl;

import com.example.store.entity.DeliveryStatus;
import com.example.store.entity.Order;
import com.example.store.repository.OrderRepository;
import com.example.store.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void updateStatus(Integer orderId, DeliveryStatus status) {
        Order chosenOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("order id not found!"));
        chosenOrder.setStatus(status.name());
        orderRepository.save(chosenOrder);
    }

    @Transactional
    @Override
    public void refund(Integer orderId) {
        log.info("TODO: REFUND FLOW: SET ORDER TO LOST/CANCELLED, refund money, if cancelled then restock");
    }
}
