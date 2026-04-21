package com.flowdash.repository;

import com.flowdash.domain.MindVaultSprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MindVaultSprintRepository extends JpaRepository<MindVaultSprint, Long> {

    List<MindVaultSprint> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<MindVaultSprint> findByIdAndUserId(Long id, Long userId);
}
