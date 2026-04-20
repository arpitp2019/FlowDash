package com.flowdash.repository;

import com.flowdash.domain.DecisionThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DecisionThreadRepository extends JpaRepository<DecisionThread, Long> {
    List<DecisionThread> findAllByUserIdOrderByLastActiveAtDesc(Long userId);
}
