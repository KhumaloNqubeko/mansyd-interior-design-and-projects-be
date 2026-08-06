package com.carpenter.business.portfolio;

import java.time.Instant;
import java.util.UUID;

public record PortfolioImageResponse(UUID id, String title, String category, String description,
                                     String fileName, String contentType, long fileSizeBytes, Instant createdAt) {
    static PortfolioImageResponse from(PortfolioImage image) {
        return new PortfolioImageResponse(image.getId(), image.getTitle(), image.getCategory(),
                image.getDescription(), image.getFileName(), image.getContentType(), image.getFileSizeBytes(),
                image.getCreatedAt());
    }
}
