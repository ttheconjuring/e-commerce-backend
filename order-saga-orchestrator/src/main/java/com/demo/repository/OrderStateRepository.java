package com.demo.repository;

import com.demo.model.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderStateRepository extends JpaRepository<OrderState, UUID> {
}
