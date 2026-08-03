package com.carpenter.business.inventory.dto;

import com.carpenter.business.inventory.ProjectMaterial;
import java.math.BigDecimal;
import java.util.UUID;

public record ProjectMaterialResponse(UUID id, UUID projectId, UUID materialId, String materialCode,
                                      String materialName, BigDecimal allocatedQuantity) {
    public static ProjectMaterialResponse from(ProjectMaterial projectMaterial) {
        return new ProjectMaterialResponse(projectMaterial.getId(), projectMaterial.getProject().getId(),
                projectMaterial.getMaterial().getId(), projectMaterial.getMaterial().getCode(),
                projectMaterial.getMaterial().getName(), projectMaterial.getAllocatedQuantity());
    }
}
