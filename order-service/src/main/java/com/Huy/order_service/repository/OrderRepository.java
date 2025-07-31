package com.Huy.order_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Huy.order_service.model.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<List<Order>> findOrderByUserId(String userId);
}
