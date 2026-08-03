package com.carpenter.business.inventory;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.project.Project;
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
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_transactions")
public class StockTransaction extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockTransactionType type;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, length = 500)
    private String notes;

    protected StockTransaction() { }

    public StockTransaction(Material material, Project project, User createdBy, StockTransactionType type,
                            BigDecimal quantity, String notes) {
        this.material = material;
        this.project = project;
        this.createdBy = createdBy;
        this.type = type;
        this.quantity = quantity;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public Material getMaterial() { return material; }
    public Project getProject() { return project; }
    public User getCreatedBy() { return createdBy; }
    public StockTransactionType getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public String getNotes() { return notes; }
}
