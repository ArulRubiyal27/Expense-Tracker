package com.expensetracker.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin("0.01")
    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String note;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @NotNull
    @Column(name = "expense_date")
    private LocalDate expenseDate;


}
