package com.expensetracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {
    @NotNull
    private String email;
    @NotNull
    private String password;





}
