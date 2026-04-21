package com.flowdash.repository;

import com.flowdash.domain.MindVaultReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MindVaultReviewLogRepository extends JpaRepository<MindVaultReviewLog, Long> {

    List<MindVaultReviewLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
