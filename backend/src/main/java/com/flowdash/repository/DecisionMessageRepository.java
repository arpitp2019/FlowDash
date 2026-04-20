package com.flowdash.repository;

import com.flowdash.domain.DecisionMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DecisionMessageRepository extends JpaRepository<DecisionMessage, Long> {
    List<DecisionMessage> findAllByThreadIdOrderByCreatedAtAsc(Long threadId);
}
