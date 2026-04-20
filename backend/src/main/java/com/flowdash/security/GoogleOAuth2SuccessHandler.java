package com.flowdash.security;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.AuthProvider;
import com.flowdash.repository.AppUserRepository;
import com.flowdash.service.UserBootstrapService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository userRepository;
    private final UserBootstrapService userBootstrapService;

    public GoogleOAuth2SuccessHandler(AppUserRepository userRepository, UserBootstrapService userBootstrapService) {
        this.userRepository = userRepository;
        this.userBootstrapService = userBootstrapService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = valueAsString(principal.getAttribute("email"));
        String name = valueAsString(principal.getAttribute("name"));
        String subject = valueAsString(principal.getAttribute("sub"));
        if (name == null || name.isBlank()) {
            name = email;
        }
        if (subject == null || subject.isBlank()) {
            subject = UUID.randomUUID().toString();
        }
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Google account did not provide an email address.");
            return;
        }

        final String resolvedEmail = email;
        final String resolvedName = name;
        final String resolvedSubject = subject;

        AppUser user = userRepository.findByOauthProviderAndOauthSubject(AuthProvider.GOOGLE, resolvedSubject)
                .orElseGet(() -> userRepository.findByEmail(resolvedEmail)
                        .map(existing -> {
                            existing.setOauthProvider(AuthProvider.GOOGLE);
                            existing.setOauthSubject(resolvedSubject);
                            existing.setDisplayName(resolvedName);
                            return existing;
                        })
                        .orElseGet(() -> new AppUser(resolvedEmail, resolvedName, null, AuthProvider.GOOGLE, resolvedSubject)));

        if (user.getId() != null) {
            user.setDisplayName(resolvedName);
        }

        AppUser saved = userRepository.save(user);
        userBootstrapService.ensureDefaultAiSettings(saved);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                saved,
                "N/A",
                java.util.List.of(new SimpleGrantedAuthority(saved.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        response.sendRedirect("/");
    }

    private static String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }
}
