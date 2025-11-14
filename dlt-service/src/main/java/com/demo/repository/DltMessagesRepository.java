package com.demo.repository;

import com.demo.model.DltMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DltMessagesRepository extends JpaRepository<DltMessage, UUID> { }