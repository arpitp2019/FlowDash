package com.flowdash.repository;

import com.flowdash.domain.AiProviderSetting;
import com.flowdash.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiProviderSettingRepository extends JpaRepository<AiProviderSetting, Long> {
    Optional<AiProviderSetting> findFirstByUserIsNullAndDefaultSelectedTrue();

    Optional<AiProviderSetting> findFirstByUserAndDefaultSelectedTrue(AppUser user);

    List<AiProviderSetting> findAllByUserIdOrderByCreatedAtAsc(Long userId);
}
