package com.carpenter.business.project;

import com.carpenter.business.project.dto.ProjectActivityResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, UUID> {
    @Query("""
        select new com.carpenter.business.project.dto.ProjectActivityResponse(
            a.id, a.project.id, a.author.id, a.author.role, a.kind, a.message, a.fileName, a.contentType, a.createdAt)
        from ProjectActivity a where a.project.id = :projectId order by a.createdAt desc, a.id desc
        """)
    Page<ProjectActivityResponse> activity(@Param("projectId") UUID projectId, Pageable pageable);
}
