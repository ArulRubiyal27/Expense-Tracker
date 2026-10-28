package com.expensetracker.security;

import com.expensetracker.Entity.AppUser;
import com.expensetracker.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public OAuth2LoginSuccessHandler(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User googleUser = (OAuth2User) authentication.getPrincipal();
        String email = googleUser.getAttribute("email");
        Boolean emailVerified = googleUser.getAttribute("email_verified");
        if (email == null || email.isBlank() || !email.contains("@")
                || !Boolean.TRUE.equals(emailVerified)) {
            response.sendRedirect("/?oauth_error");
            return;
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(googleUser, email));
        String token = jwtTokenProvider.generateToken(user.getEmail());
        String fragment = "access_token=" + encode(token)
                + "&username=" + encode(user.getUserName());
        response.sendRedirect("/#" + fragment);
    }

    private AppUser createUser(OAuth2User googleUser, String email) {
        String displayName = googleUser.getAttribute("name");
        String baseUserName = displayName == null || displayName.isBlank()
                ? email.substring(0, email.indexOf('@'))
                : displayName.trim();
        String userName = baseUserName;
        if (userRepository.existsByUserName(userName)) {
            userName = baseUserName + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        AppUser user = new AppUser();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
