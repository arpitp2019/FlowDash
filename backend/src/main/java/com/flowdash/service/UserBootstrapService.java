package com.flowdash.service;

import com.flowdash.domain.AiProviderKey;
import com.flowdash.domain.AiProviderSetting;
import com.flowdash.domain.AppUser;
import com.flowdash.domain.AuthProvider;
import com.flowdash.repository.AiProviderSettingRepository;
import com.flowdash.repository.AppUserRepository;
import com.flowdash.service.exception.DuplicateResourceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserBootstrapService {

    private final AppUserRepository userRepository;
    private final AiProviderSettingRepository providerSettingRepository;
    private final PasswordEncoder passwordEncoder;

    public UserBootstrapService(AppUserRepository userRepository,
                                AiProviderSettingRepository providerSettingRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.providerSettingRepository = providerSettingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser createLocalUser(String email, String displayName, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }
        try {
            AppUser user = new AppUser(email, displayName, passwordEncoder.encode(rawPassword), AuthProvider.LOCAL, null);
            AppUser saved = userRepository.save(user);
            ensureDefaultAiSettings(saved);
            return saved;
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }
    }

    public AppUser findOrCreateLocalUser(String email, String displayName, String rawPassword) {
        return userRepository.findByEmail(email).orElseGet(() -> createLocalUser(email, displayName, rawPassword));
    }

    public void ensureDefaultAiSettings(AppUser user) {
        if (providerSettingRepository.findFirstByUserAndDefaultSelectedTrue(user).isPresent()) {
            return;
        }
        if (providerSettingRepository.findFirstByUserIsNullAndDefaultSelectedTrue().isEmpty()) {
            providerSettingRepository.save(new AiProviderSetting(
                    null,
                    AiProviderKey.MOCK,
                    "Mock",
                    "flowdash-mock",
                    null,
                    true,
                    true,
                    new BigDecimal("0.30")
            ));
        }
        providerSettingRepository.save(new AiProviderSetting(
                user,
                AiProviderKey.MOCK,
                "Mock",
                "flowdash-mock",
                null,
                true,
                true,
                new BigDecimal("0.30")
        ));
    }
}
