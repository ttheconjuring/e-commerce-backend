package com.demo.repository;

import com.demo.model.ConsumedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsumedMessageRepository extends JpaRepository<ConsumedMessage, UUID> {
}
