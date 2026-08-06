package com.carpenter.business.portfolio;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.user.User;
import jakarta.persistence.Basic;
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
@Table(name = "portfolio_images")
public class PortfolioImage extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 80)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_data", nullable = false, columnDefinition = "bytea")
    private byte[] imageData;

    protected PortfolioImage() { }

    public PortfolioImage(User uploadedBy, String title, String category, String description, String fileName,
                          String contentType, long fileSizeBytes, byte[] imageData) {
        this.uploadedBy = uploadedBy;
        this.title = title;
        this.category = category;
        this.description = description;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.imageData = imageData;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public byte[] getImageData() { return imageData; }
}
