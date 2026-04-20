package com.flowdash.web;

import com.flowdash.domain.AppUser;
import com.flowdash.dto.UserResponse;
import com.flowdash.security.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final CurrentUserService currentUserService;

    public MeController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/api/me")
    public UserResponse me() {
        AppUser user = currentUserService.requireCurrentUser();
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getOauthProvider().name());
    }
}
