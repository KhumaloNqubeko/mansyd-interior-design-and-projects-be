package com.carpenter.business.order;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.quotation.Quotation;
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
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_id", nullable = false, unique = true)
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "accepted_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal acceptedTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    protected Order() { }

    public Order(String orderNumber, Quotation quotation) {
        this.orderNumber = orderNumber;
        this.quotation = quotation;
        this.customer = quotation.getCustomer();
        this.acceptedTotal = quotation.getTotal();
        this.status = OrderStatus.CREATED;
    }

    public UUID getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Quotation getQuotation() { return quotation; }
    public Customer getCustomer() { return customer; }
    public BigDecimal getAcceptedTotal() { return acceptedTotal; }
    public OrderStatus getStatus() { return status; }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
}
