package com.flowdash.repository;

import com.flowdash.domain.MindVaultResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MindVaultResourceRepository extends JpaRepository<MindVaultResource, Long> {

    List<MindVaultResource> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    List<MindVaultResource> findAllByItemIdAndUserIdOrderByUpdatedAtDesc(Long itemId, Long userId);

    Optional<MindVaultResource> findByIdAndUserId(Long id, Long userId);
}
