package com.flowdash.repository;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByOauthProviderAndOauthSubject(AuthProvider oauthProvider, String oauthSubject);
}
