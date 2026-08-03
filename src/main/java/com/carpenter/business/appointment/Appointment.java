package com.carpenter.business.appointment;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.customer.Customer;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointment extends AuditableEntity {
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 160)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus status;

    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;

    @Column(nullable = false, length = 300)
    private String location;

    @Column(nullable = false, length = 1000)
    private String notes;

    protected Appointment() { }

    public Appointment(Customer customer, ServiceRequest serviceRequest, Project project, User createdBy,
                       String title, AppointmentType type, LocalDateTime scheduledStart,
                       LocalDateTime scheduledEnd, String location, String notes) {
        this.customer = customer;
        this.serviceRequest = serviceRequest;
        this.project = project;
        this.createdBy = createdBy;
        this.title = title;
        this.type = type;
        this.status = AppointmentStatus.SCHEDULED;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.location = location;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public ServiceRequest getServiceRequest() { return serviceRequest; }
    public Project getProject() { return project; }
    public User getCreatedBy() { return createdBy; }
    public String getTitle() { return title; }
    public AppointmentType getType() { return type; }
    public AppointmentStatus getStatus() { return status; }
    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }

    public void update(Customer customer, ServiceRequest serviceRequest, Project project, String title,
                       AppointmentType type, LocalDateTime scheduledStart, LocalDateTime scheduledEnd,
                       String location, String notes) {
        this.customer = customer;
        this.serviceRequest = serviceRequest;
        this.project = project;
        this.title = title;
        this.type = type;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.location = location;
        this.notes = notes;
    }

    public void changeStatus(AppointmentStatus status) {
        this.status = status;
    }
}
