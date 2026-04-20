package com.flowdash.web;

import com.flowdash.domain.AppUser;
import com.flowdash.dto.LoginRequest;
import com.flowdash.dto.RegisterRequest;
import com.flowdash.dto.UserResponse;
import com.flowdash.security.CurrentUserService;
import com.flowdash.service.UserBootstrapService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserBootstrapService userBootstrapService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserBootstrapService userBootstrapService,
                          CurrentUserService currentUserService) {
        this.authenticationManager = authenticationManager;
        this.userBootstrapService = userBootstrapService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        servletRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
        return toUserResponse((UserDetails) authentication.getPrincipal());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request,
                                 HttpServletRequest servletRequest) {
        AppUser user = userBootstrapService.createLocalUser(request.email(), request.displayName(), request.password());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        servletRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
        return toUserResponse(user);
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        SecurityContextHolder.clearContext();
        if (servletRequest.getSession(false) != null) {
            servletRequest.getSession(false).invalidate();
        }
    }

    @GetMapping("/me")
    public UserResponse me() {
        return toUserResponse(currentUserService.requireCurrentUser());
    }

    private static UserResponse toUserResponse(UserDetails userDetails) {
        if (userDetails instanceof AppUser user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getOauthProvider().name());
        }
        return new UserResponse(null, userDetails.getUsername(), userDetails.getUsername(), "LOCAL");
    }
}
