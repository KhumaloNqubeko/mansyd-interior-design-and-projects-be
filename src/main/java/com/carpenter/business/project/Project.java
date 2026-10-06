package com.carpenter.business.project;

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
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_number", nullable = false, unique = true, length = 40)
    private String projectNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectStatus status;

    @Column(nullable = false)
    private int progress;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_completion_date")
    private LocalDate plannedCompletionDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;

    @Column(nullable = false, length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_review_status", nullable = false, length = 30)
    private CompletionReviewStatus completionReviewStatus = CompletionReviewStatus.NOT_REQUESTED;
    @Column(name = "completion_review_id")
    private UUID completionReviewId;
    @Column(name = "customer_confirmed_at")
    private Instant customerConfirmedAt;
    @Column(name = "customer_confirmed_by")
    private UUID customerConfirmedBy;

    protected Project() { }

    public Project(String projectNumber, Order order) {
        this.projectNumber = projectNumber;
        this.order = order;
        this.customer = order.getCustomer();
        this.status = ProjectStatus.CREATED;
        this.progress = 0;
        this.notes = "";
    }

    public UUID getId() { return id; }
    public String getProjectNumber() { return projectNumber; }
    public Order getOrder() { return order; }
    public Customer getCustomer() { return customer; }
    public ProjectStatus getStatus() { return status; }
    public int getProgress() { return progress; }
    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public LocalDate getPlannedCompletionDate() { return plannedCompletionDate; }
    public LocalDate getActualStartDate() { return actualStartDate; }
    public LocalDate getActualCompletionDate() { return actualCompletionDate; }
    public String getNotes() { return notes; }
    public CompletionReviewStatus getCompletionReviewStatus() { return completionReviewStatus; }
    public UUID getCompletionReviewId() { return completionReviewId; }
    public Instant getCustomerConfirmedAt() { return customerConfirmedAt; }
    public UUID getCustomerConfirmedBy() { return customerConfirmedBy; }

    public void requestCompletionReview() {
        completionReviewStatus = CompletionReviewStatus.PENDING_REVIEW;
        completionReviewId = UUID.randomUUID();
        customerConfirmedAt = null;
        customerConfirmedBy = null;
    }

    public void reviewCompletion(boolean confirmed, UUID userId) {
        completionReviewStatus = confirmed ? CompletionReviewStatus.CONFIRMED : CompletionReviewStatus.ISSUE_REPORTED;
        customerConfirmedAt = confirmed ? Instant.now() : null;
        customerConfirmedBy = confirmed ? userId : null;
    }

    public void update(LocalDate plannedStartDate, LocalDate plannedCompletionDate, String notes) {
        this.plannedStartDate = plannedStartDate;
        this.plannedCompletionDate = plannedCompletionDate;
        this.notes = notes;
    }

    public void changeStatus(ProjectStatus status, int progress, LocalDate actualStartDate, LocalDate actualCompletionDate) {
        this.status = status;
        this.progress = progress;
        this.actualStartDate = actualStartDate;
        this.actualCompletionDate = actualCompletionDate;
    }
}
