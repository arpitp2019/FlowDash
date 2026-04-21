package com.flowdash.repository;

import com.flowdash.domain.MindVaultSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MindVaultSubjectRepository extends JpaRepository<MindVaultSubject, Long> {

    List<MindVaultSubject> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<MindVaultSubject> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndTitleIgnoreCase(Long userId, String title);
}
