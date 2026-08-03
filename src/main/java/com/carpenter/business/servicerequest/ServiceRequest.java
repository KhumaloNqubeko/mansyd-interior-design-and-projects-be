package com.carpenter.business.servicerequest;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.customer.Customer;
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
import java.util.UUID;

@Entity
@Table(name = "service_requests")
public class ServiceRequest extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(name = "preferred_contact_method", nullable = false, length = 30)
    private String preferredContactMethod;

    @Column(name = "site_address", nullable = false, length = 300)
    private String siteAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ServiceRequestStatus status;

    protected ServiceRequest() { }

    public ServiceRequest(Customer customer, String title, String description,
                          String preferredContactMethod, String siteAddress) {
        this.customer = customer;
        this.title = title;
        this.description = description;
        this.preferredContactMethod = preferredContactMethod;
        this.siteAddress = siteAddress;
        this.status = ServiceRequestStatus.SUBMITTED;
    }

    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPreferredContactMethod() { return preferredContactMethod; }
    public String getSiteAddress() { return siteAddress; }
    public ServiceRequestStatus getStatus() { return status; }

    public void update(String title, String description, String preferredContactMethod, String siteAddress) {
        this.title = title;
        this.description = description;
        this.preferredContactMethod = preferredContactMethod;
        this.siteAddress = siteAddress;
    }

    public void changeStatus(ServiceRequestStatus status) {
        this.status = status;
    }
}
