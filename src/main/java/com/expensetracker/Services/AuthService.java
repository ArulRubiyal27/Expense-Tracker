package com.expensetracker.Services;


import com.expensetracker.Entity.AppUser;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.LoginResponse;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider =  jwtTokenProvider;
    }

    public AppUser registerUser(String userName, String email, String password) {
        if (userRepository.existsByEmail(email) || userRepository.existsByUserName(userName)) {
            throw new IllegalArgumentException("Email or username already in use");
        }


        AppUser user = new AppUser();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCreatedAt(java.time.LocalDateTime.now());

        return userRepository.save(user);
    }

    public LoginResponse loginUser(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        Optional<AppUser> user = Optional.of(userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password")));

        if (!passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.get().getEmail());
        return new LoginResponse(token, 3600, "Bearer", user.get().getUserName());
    }
}
