package com.flowdash.repository;

import com.flowdash.domain.MindVaultLearningItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MindVaultLearningItemRepository extends JpaRepository<MindVaultLearningItem, Long> {

    List<MindVaultLearningItem> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<MindVaultLearningItem> findByIdAndUserId(Long id, Long userId);
}
