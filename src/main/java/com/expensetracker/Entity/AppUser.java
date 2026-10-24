package com.expensetracker.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@RequiredArgsConstructor @AllArgsConstructor
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String userName;

    @NotNull
    private String email;

    @NotNull
    private String passwordHash;

    private LocalDateTime createdAt;


}
