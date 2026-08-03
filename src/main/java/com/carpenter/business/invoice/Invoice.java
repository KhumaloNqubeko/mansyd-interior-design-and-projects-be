package com.carpenter.business.invoice;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.order.Order;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "balance_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceDue;

    @Column(nullable = false, length = 1000)
    private String notes;

    protected Invoice() { }

    public Invoice(String invoiceNumber, Order order, LocalDate dueDate, BigDecimal totalAmount, String notes) {
        this.invoiceNumber = invoiceNumber;
        this.order = order;
        this.customer = order.getCustomer();
        this.status = InvoiceStatus.DRAFT;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.paidAmount = BigDecimal.ZERO.setScale(2);
        this.balanceDue = totalAmount;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Order getOrder() { return order; }
    public Customer getCustomer() { return customer; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getBalanceDue() { return balanceDue; }
    public String getNotes() { return notes; }

    public void update(LocalDate dueDate, BigDecimal totalAmount, String notes) {
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.notes = notes;
        recalculateBalance();
    }

    public void issue(LocalDate issueDate) {
        this.issueDate = issueDate;
        this.status = InvoiceStatus.ISSUED;
    }

    public void cancel() {
        this.status = InvoiceStatus.CANCELLED;
    }

    public void applyPayment(BigDecimal amount) {
        this.paidAmount = paidAmount.add(amount);
        recalculateBalance();
    }

    private void recalculateBalance() {
        this.balanceDue = totalAmount.subtract(paidAmount);
        if (balanceDue.signum() == 0 && status != InvoiceStatus.CANCELLED) {
            status = InvoiceStatus.PAID;
        } else if (paidAmount.signum() > 0 && status != InvoiceStatus.CANCELLED) {
            status = InvoiceStatus.PARTIALLY_PAID;
        }
    }
}
