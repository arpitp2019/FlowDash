package com.flowdash.repository;

import com.flowdash.domain.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaultRepository extends JpaRepository<VaultEntry, Long> {
    List<VaultEntry> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
}
