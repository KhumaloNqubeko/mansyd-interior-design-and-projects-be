package com.carpenter.business.quotation;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.servicerequest.ServiceRequest;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quotations")
public class Quotation extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "quotation_number", nullable = false, unique = true, length = 40)
    private String quotationNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_request_id", nullable = false, unique = true)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuotationStatus status;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 1000)
    private String notes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "tax_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationItem> items = new ArrayList<>();

    protected Quotation() { }

    public Quotation(String quotationNumber, ServiceRequest serviceRequest, LocalDate expiryDate, String notes) {
        this.quotationNumber = quotationNumber;
        this.serviceRequest = serviceRequest;
        this.customer = serviceRequest.getCustomer();
        this.status = QuotationStatus.DRAFT;
        this.expiryDate = expiryDate;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public String getQuotationNumber() { return quotationNumber; }
    public ServiceRequest getServiceRequest() { return serviceRequest; }
    public Customer getCustomer() { return customer; }
    public QuotationStatus getStatus() { return status; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getNotes() { return notes; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscountTotal() { return discountTotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public BigDecimal getTotal() { return total; }
    public List<QuotationItem> getItems() { return items; }

    public void update(LocalDate expiryDate, String notes) {
        this.expiryDate = expiryDate;
        this.notes = notes;
    }

    public void addItem(QuotationItem item) {
        items.add(item);
    }

    public void removeItem(QuotationItem item) {
        items.remove(item);
    }

    public void updateTotals(BigDecimal subtotal, BigDecimal discountTotal, BigDecimal taxTotal, BigDecimal total) {
        this.subtotal = subtotal;
        this.discountTotal = discountTotal;
        this.taxTotal = taxTotal;
        this.total = total;
    }

    public void changeStatus(QuotationStatus status) {
        this.status = status;
    }
}
