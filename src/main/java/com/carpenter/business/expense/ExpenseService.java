package com.carpenter.business.expense;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.expense.dto.ExpenseRequest;
import com.carpenter.business.expense.dto.ExpenseResponse;
import com.carpenter.business.inventory.Material;
import com.carpenter.business.inventory.MaterialRepository;
import com.carpenter.business.project.Project;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.supplier.Supplier;
import com.carpenter.business.supplier.SupplierRepository;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final SupplierRepository supplierRepository;
    private final ProjectRepository projectRepository;
    private final MaterialRepository materialRepository;
    private final CurrentUser currentUser;

    public ExpenseService(ExpenseRepository expenseRepository, SupplierRepository supplierRepository,
                          ProjectRepository projectRepository, MaterialRepository materialRepository,
                          CurrentUser currentUser) {
        this.expenseRepository = expenseRepository;
        this.supplierRepository = supplierRepository;
        this.projectRepository = projectRepository;
        this.materialRepository = materialRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        Expense expense = expenseRepository.save(new Expense(trim(request.title()), request.category(),
                money(request.amount()), request.expenseDate(), clean(request.receiptReference()), clean(request.notes()),
                supplier(request.supplierId()), project(request.projectId()), material(request.materialId()), user));
        return ExpenseResponse.from(expense);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(expenseRepository.findAll(pageable).map(ExpenseResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> byProject(UUID projectId, Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(expenseRepository.findByProjectId(projectId, pageable).map(ExpenseResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> bySupplier(UUID supplierId, Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(expenseRepository.findBySupplierId(supplierId, pageable).map(ExpenseResponse::from));
    }

    @Transactional
    public ExpenseResponse update(UUID id, ExpenseRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Expense expense = expense(id);
        requireDraft(expense, "Only draft expenses can be edited.");
        expense.update(trim(request.title()), request.category(), money(request.amount()), request.expenseDate(),
                clean(request.receiptReference()), clean(request.notes()), supplier(request.supplierId()),
                project(request.projectId()), material(request.materialId()));
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public ExpenseResponse approve(UUID id, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Expense expense = expense(id);
        requireDraft(expense, "Only draft expenses can be approved.");
        if (expense.getExpenseDate().isAfter(LocalDate.now())) {
            throw new UnauthorisedOperationException("Future-dated expenses cannot be approved.");
        }
        expense.approve();
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public ExpenseResponse reimburse(UUID id, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Expense expense = expense(id);
        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new UnauthorisedOperationException("Only approved expenses can be marked as reimbursed.");
        }
        expense.reimburse();
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public ExpenseResponse voidExpense(UUID id, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Expense expense = expense(id);
        if (expense.getStatus() == ExpenseStatus.REIMBURSED) {
            throw new UnauthorisedOperationException("Reimbursed expenses cannot be voided.");
        }
        expense.voidExpense();
        return ExpenseResponse.from(expense);
    }

    private void requireDraft(Expense expense, String message) {
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new UnauthorisedOperationException(message);
        }
    }

    private Expense expense(UUID id) {
        return expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense was not found."));
    }

    private Supplier supplier(UUID id) {
        return id == null ? null : supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier was not found."));
    }

    private Project project(UUID id) {
        return id == null ? null : projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project was not found."));
    }

    private Material material(UUID id) {
        return id == null ? null : materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material was not found."));
    }

    private BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private String trim(String value) { return value.trim(); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
