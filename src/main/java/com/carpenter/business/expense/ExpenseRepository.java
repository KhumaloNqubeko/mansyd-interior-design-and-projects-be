package com.carpenter.business.expense;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Page<Expense> findByProjectId(UUID projectId, Pageable pageable);
    Page<Expense> findBySupplierId(UUID supplierId, Pageable pageable);
}
