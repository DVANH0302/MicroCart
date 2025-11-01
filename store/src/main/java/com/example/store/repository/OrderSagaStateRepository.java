package com.example.store.repository;

import com.example.store.entity.OrderSagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSagaStateRepository extends JpaRepository<OrderSagaState, Long> {
}
