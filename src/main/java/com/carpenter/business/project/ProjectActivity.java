package com.carpenter.business.project;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.user.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "project_activity")
public class ProjectActivity extends AuditableEntity {
    public enum Kind { COMMENT, PHOTO, REVIEW_REQUESTED, COMPLETION_CONFIRMED, ISSUE_REPORTED }
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id") private Project project;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "author_id") private User author;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Kind kind;
    @Column(nullable = false, length = 2000) private String message;
    @Column(name = "file_name", length = 255) private String fileName;
    @Column(name = "content_type", length = 40) private String contentType;
    @Column(name = "photo_data", columnDefinition = "bytea") private byte[] photoData;
    protected ProjectActivity() { }
    public ProjectActivity(Project project, User author, Kind kind, String message) {
        this.project = project; this.author = author; this.kind = kind; this.message = message;
    }
    public void attachPhoto(String fileName, String contentType, byte[] data) {
        this.fileName = fileName; this.contentType = contentType; this.photoData = data;
    }
    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public User getAuthor() { return author; }
    public Kind getKind() { return kind; }
    public String getMessage() { return message; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public byte[] getPhotoData() { return photoData; }
}
