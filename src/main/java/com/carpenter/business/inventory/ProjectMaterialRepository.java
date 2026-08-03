package com.carpenter.business.inventory;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMaterialRepository extends JpaRepository<ProjectMaterial, UUID> {
    Optional<ProjectMaterial> findByProjectIdAndMaterialId(UUID projectId, UUID materialId);
    Page<ProjectMaterial> findByProjectId(UUID projectId, Pageable pageable);
}
