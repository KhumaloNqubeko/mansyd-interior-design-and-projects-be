package com.carpenter.business.inventory;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.supplier.Supplier;
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
@Table(name = "materials")
public class Material extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "stock_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockQuantity;

    @Column(name = "reorder_level", nullable = false, precision = 12, scale = 3)
    private BigDecimal reorderLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private boolean active = true;

    protected Material() { }

    public Material(String code, String name, UnitOfMeasure unitOfMeasure, BigDecimal unitCost,
                    BigDecimal reorderLevel, Supplier supplier) {
        this.code = code;
        this.name = name;
        this.unitOfMeasure = unitOfMeasure;
        this.unitCost = unitCost;
        this.stockQuantity = BigDecimal.ZERO.setScale(3);
        this.reorderLevel = reorderLevel;
        this.supplier = supplier;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getStockQuantity() { return stockQuantity; }
    public BigDecimal getReorderLevel() { return reorderLevel; }
    public Supplier getSupplier() { return supplier; }
    public boolean isActive() { return active; }
    public boolean isLowStock() { return stockQuantity.compareTo(reorderLevel) <= 0; }

    public void update(String name, UnitOfMeasure unitOfMeasure, BigDecimal unitCost, BigDecimal reorderLevel,
                       Supplier supplier, boolean active) {
        this.name = name;
        this.unitOfMeasure = unitOfMeasure;
        this.unitCost = unitCost;
        this.reorderLevel = reorderLevel;
        this.supplier = supplier;
        this.active = active;
    }

    public void applyQuantityChange(BigDecimal delta) {
        this.stockQuantity = stockQuantity.add(delta);
    }
}
