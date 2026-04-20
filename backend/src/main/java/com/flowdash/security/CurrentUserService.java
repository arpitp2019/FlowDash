package com.flowdash.security;

import com.flowdash.domain.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public AppUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUser user)) {
            throw new IllegalStateException("No authenticated user in the security context.");
        }
        return user;
    }

    public Long requireCurrentUserId() {
        return requireCurrentUser().getId();
    }
}
