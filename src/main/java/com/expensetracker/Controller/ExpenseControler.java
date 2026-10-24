package com.expensetracker.Controller;

import com.expensetracker.Services.ExpenseService;
import com.expensetracker.dto.CategoryTotalResponse;
import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expense")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseControler {

    private final ExpenseService expenseService;

    public ExpenseControler(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@Valid @RequestBody ExpenseRequest request, Authentication authentication)
    {

        return expenseService.add(request, authentication.getName());
    }

    @GetMapping
    public List<ExpenseResponse> listExpenses(@RequestParam LocalDate from, @RequestParam LocalDate to,
                                              Authentication authentication) {
        return expenseService.list(authentication.getName(), from, to);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest req,
                                  Authentication authentication) {
        return expenseService.update(id, req, authentication.getName());
    }

    @PatchMapping("/{id}")
    public ExpenseResponse patch(@PathVariable Long id, @Valid @RequestBody ExpenseRequest req,
                                 Authentication authentication) {
        return expenseService.patch(id, req, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        expenseService.delete(id, authentication.getName());
    }

    @GetMapping("/summary")
    public List<CategoryTotalResponse> summary(@RequestParam LocalDate from, @RequestParam LocalDate to,
                                               Authentication authentication) {
        return expenseService.summary(authentication.getName(), from, to);
    }
}
