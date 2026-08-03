package com.carpenter.business.inventory;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "project_materials")
public class ProjectMaterial extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "allocated_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal allocatedQuantity;

    protected ProjectMaterial() { }

    public ProjectMaterial(Project project, Material material, BigDecimal allocatedQuantity) {
        this.project = project;
        this.material = material;
        this.allocatedQuantity = allocatedQuantity;
    }

    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public Material getMaterial() { return material; }
    public BigDecimal getAllocatedQuantity() { return allocatedQuantity; }

    public void add(BigDecimal quantity) { this.allocatedQuantity = allocatedQuantity.add(quantity); }
    public void subtract(BigDecimal quantity) { this.allocatedQuantity = allocatedQuantity.subtract(quantity); }
}
