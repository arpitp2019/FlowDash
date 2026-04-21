package com.flowdash.repository;

import com.flowdash.domain.GoalActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalActivityRepository extends JpaRepository<GoalActivity, Long> {
    List<GoalActivity> findAllByUserIdAndActivityDateBetweenOrderByActivityDateDesc(Long userId, LocalDate from, LocalDate to);

    List<GoalActivity> findAllByGoalIdAndUserIdAndActivityDateBetweenOrderByActivityDateDesc(Long goalId, Long userId, LocalDate from, LocalDate to);

    Optional<GoalActivity> findByGoalIdAndUserIdAndActivityDate(Long goalId, Long userId, LocalDate activityDate);

    void deleteAllByGoalIdAndUserId(Long goalId, Long userId);
}
