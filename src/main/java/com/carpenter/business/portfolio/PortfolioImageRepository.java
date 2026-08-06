package com.carpenter.business.portfolio;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, UUID> {
    @Query("""
            select new com.carpenter.business.portfolio.PortfolioImageResponse(
                image.id, image.title, image.category, image.description, image.fileName,
                image.contentType, image.fileSizeBytes, image.createdAt)
            from PortfolioImage image
            order by image.createdAt desc
            """)
    List<PortfolioImageResponse> findAllMetadataByOrderByCreatedAtDesc();
}
