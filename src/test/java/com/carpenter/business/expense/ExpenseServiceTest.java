package com.carpenter.business.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.expense.dto.ExpenseRequest;
import com.carpenter.business.inventory.MaterialRepository;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.supplier.SupplierRepository;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock ExpenseRepository expenses;
    @Mock SupplierRepository suppliers;
    @Mock ProjectRepository projects;
    @Mock MaterialRepository materials;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    private ExpenseService service;

    @BeforeEach
    void setUp() {
        service = new ExpenseService(expenses, suppliers, projects, materials, currentUser);
    }

    @Test
    void approvedExpenseCannotBeEdited() {
        Expense expense = expense();
        expense.approve();
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user());
        when(expenses.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> service.update(expense.getId(), request(LocalDate.now()), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void futureDatedExpenseCannotBeApproved() {
        Expense expense = expense();
        ReflectionTestUtils.setField(expense, "expenseDate", LocalDate.now().plusDays(1));
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user());
        when(expenses.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> service.approve(expense.getId(), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void approvedExpenseCanBeReimbursed() {
        Expense expense = expense();
        expense.approve();
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user());
        when(expenses.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThat(service.reimburse(expense.getId(), authentication).status()).isEqualTo(ExpenseStatus.REIMBURSED);
    }

    private Expense expense() {
        Expense expense = new Expense("Wood delivery", ExpenseCategory.MATERIALS, new BigDecimal("250.00"),
                LocalDate.now(), "RCPT-1", "", null, null, null, user());
        ReflectionTestUtils.setField(expense, "id", UUID.randomUUID());
        return expense;
    }

    private ExpenseRequest request(LocalDate date) {
        return new ExpenseRequest("Updated", ExpenseCategory.OTHER, new BigDecimal("50.00"), date,
                "RCPT-2", "", null, null, null);
    }

    private User user() {
        User user = new User("carpenter@example.com", "hash", Role.CARPENTER, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
