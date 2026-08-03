package com.carpenter.business.expense;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.expense.dto.ExpenseRequest;
import com.carpenter.business.expense.dto.ExpenseResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    ExpenseResponse create(@Valid @RequestBody ExpenseRequest request, Authentication authentication) {
        return expenseService.create(request, authentication);
    }

    @GetMapping("/expenses")
    PageResponse<ExpenseResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return expenseService.all(authentication, pageable);
    }

    @GetMapping("/projects/{projectId}/expenses")
    PageResponse<ExpenseResponse> byProject(@PathVariable UUID projectId, Authentication authentication,
                                            @PageableDefault(size = 20) Pageable pageable) {
        return expenseService.byProject(projectId, authentication, pageable);
    }

    @GetMapping("/suppliers/{supplierId}/expenses")
    PageResponse<ExpenseResponse> bySupplier(@PathVariable UUID supplierId, Authentication authentication,
                                             @PageableDefault(size = 20) Pageable pageable) {
        return expenseService.bySupplier(supplierId, authentication, pageable);
    }

    @PutMapping("/expenses/{id}")
    ExpenseResponse update(@PathVariable UUID id, @Valid @RequestBody ExpenseRequest request,
                           Authentication authentication) {
        return expenseService.update(id, request, authentication);
    }

    @PostMapping("/expenses/{id}/approve")
    ExpenseResponse approve(@PathVariable UUID id, Authentication authentication) {
        return expenseService.approve(id, authentication);
    }

    @PostMapping("/expenses/{id}/reimburse")
    ExpenseResponse reimburse(@PathVariable UUID id, Authentication authentication) {
        return expenseService.reimburse(id, authentication);
    }

    @PostMapping("/expenses/{id}/void")
    ExpenseResponse voidExpense(@PathVariable UUID id, Authentication authentication) {
        return expenseService.voidExpense(id, authentication);
    }
}
