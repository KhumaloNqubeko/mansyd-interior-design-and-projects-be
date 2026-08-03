package com.carpenter.business.quotation;

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
import java.util.UUID;

@Entity
@Table(name = "quotation_items")
public class QuotationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuotationItemType type;

    @Column(nullable = false, length = 180)
    private String description;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected QuotationItem() { }

    public QuotationItem(Quotation quotation, QuotationItemType type, String description, BigDecimal quantity,
                         BigDecimal unitPrice, BigDecimal discountAmount, BigDecimal taxRate) {
        this.quotation = quotation;
        update(type, description, quantity, unitPrice, discountAmount, taxRate, BigDecimal.ZERO);
    }

    public UUID getId() { return id; }
    public Quotation getQuotation() { return quotation; }
    public QuotationItemType getType() { return type; }
    public String getDescription() { return description; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTaxRate() { return taxRate; }
    public BigDecimal getLineTotal() { return lineTotal; }

    public void update(QuotationItemType type, String description, BigDecimal quantity, BigDecimal unitPrice,
                       BigDecimal discountAmount, BigDecimal taxRate, BigDecimal lineTotal) {
        this.type = type;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountAmount = discountAmount;
        this.taxRate = taxRate;
        this.lineTotal = lineTotal;
    }

    void updateLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
