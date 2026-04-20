package com.flowdash.repository;

import com.flowdash.domain.GoalItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<GoalItem, Long> {
    List<GoalItem> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
}
