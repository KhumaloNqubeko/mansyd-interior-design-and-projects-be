package com.carpenter.business.expense;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.inventory.Material;
import com.carpenter.business.project.Project;
import com.carpenter.business.supplier.Supplier;
import com.carpenter.business.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class Expense extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "receipt_reference", nullable = false, length = 180)
    private String receiptReference;

    @Column(nullable = false, length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    protected Expense() { }

    public Expense(String title, ExpenseCategory category, BigDecimal amount, LocalDate expenseDate,
                   String receiptReference, String notes, Supplier supplier, Project project, Material material,
                   User createdBy) {
        this.title = title;
        this.category = category;
        this.status = ExpenseStatus.DRAFT;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.receiptReference = receiptReference;
        this.notes = notes;
        this.supplier = supplier;
        this.project = project;
        this.material = material;
        this.createdBy = createdBy;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public ExpenseCategory getCategory() { return category; }
    public ExpenseStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public String getReceiptReference() { return receiptReference; }
    public String getNotes() { return notes; }
    public Supplier getSupplier() { return supplier; }
    public Project getProject() { return project; }
    public Material getMaterial() { return material; }
    public User getCreatedBy() { return createdBy; }

    public void update(String title, ExpenseCategory category, BigDecimal amount, LocalDate expenseDate,
                       String receiptReference, String notes, Supplier supplier, Project project, Material material) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.receiptReference = receiptReference;
        this.notes = notes;
        this.supplier = supplier;
        this.project = project;
        this.material = material;
    }

    public void approve() { this.status = ExpenseStatus.APPROVED; }
    public void reimburse() { this.status = ExpenseStatus.REIMBURSED; }
    public void voidExpense() { this.status = ExpenseStatus.VOID; }
}
