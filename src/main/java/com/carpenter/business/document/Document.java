package com.carpenter.business.document;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.invoice.Invoice;
import com.carpenter.business.project.Project;
import com.carpenter.business.servicerequest.ServiceRequest;
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
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    @Column(nullable = false, length = 160)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status;

    @Column(name = "file_name", nullable = false, length = 180)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "storage_url", nullable = false, length = 500)
    private String storageUrl;

    @Column(nullable = false, length = 1000)
    private String notes;

    @Column(name = "customer_visible", nullable = false)
    private boolean customerVisible;

    protected Document() { }

    public Document(Customer customer, ServiceRequest serviceRequest, Project project, Invoice invoice, User uploadedBy,
                    String title, DocumentType type, String fileName, String contentType, Long fileSizeBytes,
                    String storageUrl, String notes, boolean customerVisible) {
        this.customer = customer;
        this.serviceRequest = serviceRequest;
        this.project = project;
        this.invoice = invoice;
        this.uploadedBy = uploadedBy;
        this.title = title;
        this.type = type;
        this.status = DocumentStatus.ACTIVE;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.storageUrl = storageUrl;
        this.notes = notes;
        this.customerVisible = customerVisible;
    }

    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public ServiceRequest getServiceRequest() { return serviceRequest; }
    public Project getProject() { return project; }
    public Invoice getInvoice() { return invoice; }
    public User getUploadedBy() { return uploadedBy; }
    public String getTitle() { return title; }
    public DocumentType getType() { return type; }
    public DocumentStatus getStatus() { return status; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public String getStorageUrl() { return storageUrl; }
    public String getNotes() { return notes; }
    public boolean isCustomerVisible() { return customerVisible; }

    public void update(Customer customer, ServiceRequest serviceRequest, Project project, Invoice invoice,
                       String title, DocumentType type, String fileName, String contentType, Long fileSizeBytes,
                       String storageUrl, String notes, boolean customerVisible) {
        this.customer = customer;
        this.serviceRequest = serviceRequest;
        this.project = project;
        this.invoice = invoice;
        this.title = title;
        this.type = type;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.storageUrl = storageUrl;
        this.notes = notes;
        this.customerVisible = customerVisible;
    }

    public void archive() { this.status = DocumentStatus.ARCHIVED; }
}
