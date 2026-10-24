package com.expensetracker.dto;

import com.expensetracker.Entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id, BigDecimal amount, String category, String note, LocalDate expenseDate)
{
    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getCategory().getName(),
                e.getNote(),
                e.getExpenseDate());
    }
}
