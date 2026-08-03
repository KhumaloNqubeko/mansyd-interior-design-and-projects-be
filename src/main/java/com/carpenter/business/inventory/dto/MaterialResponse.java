package com.carpenter.business.inventory.dto;

import com.carpenter.business.inventory.Material;
import com.carpenter.business.inventory.UnitOfMeasure;
import java.math.BigDecimal;
import java.util.UUID;

public record MaterialResponse(UUID id, String code, String name, UnitOfMeasure unitOfMeasure, BigDecimal unitCost,
                               BigDecimal stockQuantity, BigDecimal reorderLevel, UUID supplierId,
                               String supplierName, boolean active, boolean lowStock) {
    public static MaterialResponse from(Material material) {
        return new MaterialResponse(material.getId(), material.getCode(), material.getName(),
                material.getUnitOfMeasure(), material.getUnitCost(), material.getStockQuantity(),
                material.getReorderLevel(), material.getSupplier() == null ? null : material.getSupplier().getId(),
                material.getSupplier() == null ? null : material.getSupplier().getName(),
                material.isActive(), material.isLowStock());
    }
}
