package com.loyalty.gateway.repository;

import com.loyalty.gateway.model.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    Optional<ProcessedEvent> findByEventId(String eventId);
}

