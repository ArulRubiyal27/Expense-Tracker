package com.expensetracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.beans.ConstructorProperties;

@Getter @Setter

public class RegisterRequest {

    @NotNull
    private String email;
    @NotNull
    private String password;
    @NotNull
    private String username;
}
