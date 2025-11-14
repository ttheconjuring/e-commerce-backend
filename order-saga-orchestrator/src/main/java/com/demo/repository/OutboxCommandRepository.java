package com.demo.repository;

import com.demo.model.OutboxCommand;
import com.demo.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxCommandRepository extends JpaRepository<OutboxCommand, UUID> {
    List<OutboxCommand> findByStatus(Status status);
}