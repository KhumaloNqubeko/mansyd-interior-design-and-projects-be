package com.carpenter.business.project;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "project_updates")
public class ProjectUpdate extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    protected ProjectUpdate() { }

    public ProjectUpdate(Project project, User createdBy, String title, String message) {
        this.project = project;
        this.createdBy = createdBy;
        this.title = title;
        this.message = message;
    }

    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public User getCreatedBy() { return createdBy; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
}
