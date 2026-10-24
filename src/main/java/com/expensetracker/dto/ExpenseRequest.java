package com.expensetracker.dto;

import com.expensetracker.Entity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String category_name,
        String note,
        @NotNull @PastOrPresent LocalDate expenseDate
) {}
