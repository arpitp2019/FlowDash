package com.flowdash.repository;

import com.flowdash.domain.HabitItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitRepository extends JpaRepository<HabitItem, Long> {
    List<HabitItem> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
}
