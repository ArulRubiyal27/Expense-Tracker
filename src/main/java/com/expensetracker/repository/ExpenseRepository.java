package com.expensetracker.repository;

import com.expensetracker.Entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    List<Expense> findByUser_EmailAndExpenseDateBetweenOrderByExpenseDateDesc(
            String email, LocalDate from, LocalDate to);
    java.util.Optional<Expense> findByIdAndUser_Email(Long id, String email);

    @Query("""
            select e.category.name as category, sum(e.amount) as total
            from Expense e
            where e.user.email = :email and e.expenseDate between :from and :to
            group by e.category.name
            """)
    List<CategoryTotal> totalsByCategory(@Param("email") String email,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    interface CategoryTotal {
        String getCategory();

        BigDecimal getTotal();
    }
}
