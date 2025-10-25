package com.example.store.repository;

import com.example.store.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Integer> {

    @Query("Select o from Order o join fetch o.user where o.id = :id")
    Optional<Order> findByIdWithUser(@Param("id") Integer orderId);
    Optional<List<Order>> findByUserId(Integer userId);
}
