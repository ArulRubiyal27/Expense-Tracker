package com.expensetracker.Controller;


import com.expensetracker.Services.AuthService;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody @Valid RegisterRequest request) {
        try {
            authService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
            return "User registered successfully";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.loginUser(request);
    }
}
