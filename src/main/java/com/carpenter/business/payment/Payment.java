package com.carpenter.business.payment;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.invoice.Invoice;
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
@Table(name = "payments")
public class Payment extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "proof_reference", nullable = false, length = 180)
    private String proofReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false, length = 1000)
    private String notes;

    protected Payment() { }

    public Payment(Invoice invoice, BigDecimal amount, LocalDate paymentDate, String proofReference, String notes) {
        this.invoice = invoice;
        this.customer = invoice.getCustomer();
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.proofReference = proofReference;
        this.notes = notes;
        this.status = PaymentStatus.PENDING_REVIEW;
    }

    public UUID getId() { return id; }
    public Invoice getInvoice() { return invoice; }
    public Customer getCustomer() { return customer; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getProofReference() { return proofReference; }
    public PaymentStatus getStatus() { return status; }
    public String getNotes() { return notes; }

    public void approve() { this.status = PaymentStatus.APPROVED; }
    public void reject(String notes) {
        this.status = PaymentStatus.REJECTED;
        this.notes = notes;
    }
}
