package com.expensetracker.Services;

import com.expensetracker.Entity.Category;
import com.expensetracker.Entity.Expense;
import com.expensetracker.Entity.AppUser;
import com.expensetracker.Exception.ResourcenotFoundException;
import com.expensetracker.dto.CategoryTotalResponse;
import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
    @RequiredArgsConstructor
    public class ExpenseService {

        private final ExpenseRepository expenseRepository;
        private final CategoryRepository categoryRepository;
        private final UserRepository userRepository;


        @Transactional
        public ExpenseResponse add(ExpenseRequest req, String email) {
            Expense e = new Expense();
            apply(e, req);
            e.setUser(findUser(email));
            return ExpenseResponse.from(expenseRepository.save(e));
        }

        @Transactional(readOnly = true)
        public List<ExpenseResponse> list(String email, LocalDate from, LocalDate to) {
            validateRange(from, to);
            return expenseRepository.findByUser_EmailAndExpenseDateBetweenOrderByExpenseDateDesc(email, from, to)
                    .stream().map(ExpenseResponse::from).toList();
        }

        @Transactional
        public ExpenseResponse update(Long id, ExpenseRequest req, String email) {
            Expense e = findOrThrow(id, email);
            apply(e, req);
            return ExpenseResponse.from(e);   // dirty checking saves it
        }

        @Transactional
        public void delete(Long id, String email) {
            expenseRepository.delete(findOrThrow(id, email));
        }

        @Transactional(readOnly = true)
        public List<CategoryTotalResponse> summary(String email, LocalDate from, LocalDate to) {
            validateRange(from, to);
            return expenseRepository.totalsByCategory(email, from, to).stream()
                    .map(t -> new CategoryTotalResponse(t.getCategory(), t.getTotal()))
                    .toList();
        }

        // ---- helpers / business rules ----
        private Expense findOrThrow(Long id, String email) {
            return expenseRepository.findByIdAndUser_Email(id, email)
                    .orElseThrow(() -> new ResourcenotFoundException("Expense " + id + " not found"));
        }

        private AppUser findUser(String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourcenotFoundException("User not found"));
        }

        private void apply(Expense e, ExpenseRequest req) {
            Long categoryId = categoryRepository.findIdByName(req.category_name())
                    .orElseThrow(() -> new ResourcenotFoundException(
                            "Category " + req.category_name() + " not found"));
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourcenotFoundException(
                            "Category " + req.category_name() + " not found"));
            e.setAmount(req.amount());
            e.setCategory(category);// normalize categories
            e.setNote(req.note());
            e.setExpenseDate(req.expenseDate());
        }



    private void validateRange(LocalDate from, LocalDate to) {
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("'from' must not be after 'to'");
            }
        }

    @Transactional
    public ExpenseResponse patch(Long id, @Valid ExpenseRequest req, String email) {
        Expense e = findOrThrow(id, email);
        apply(e, req);
        return ExpenseResponse.from(e);
    }
}
