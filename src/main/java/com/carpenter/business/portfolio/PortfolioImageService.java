package com.carpenter.business.portfolio;

import com.carpenter.business.audit.AuditAction;
import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PortfolioImageService {
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final PortfolioImageRepository repository;
    private final CurrentUser currentUser;
    private final AuditLogService auditLogService;

    public PortfolioImageService(PortfolioImageRepository repository, CurrentUser currentUser,
                                 AuditLogService auditLogService) {
        this.repository = repository;
        this.currentUser = currentUser;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<PortfolioImageResponse> all() {
        return repository.findAllMetadataByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public PortfolioImage content(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio image was not found."));
    }

    @Transactional
    public PortfolioImageResponse upload(String title, String category, String description, MultipartFile file,
                                         Authentication authentication) throws IOException {
        User carpenter = currentUser.requireRole(authentication, Role.CARPENTER);
        validateText(title, "Title", 160);
        validateText(category, "Category", 80);
        if (description != null && description.trim().length() > 500) {
            throw new IllegalArgumentException("Description must not exceed 500 characters.");
        }
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose an image to upload.");
        if (file.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Image size must not exceed 10 MB.");
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPEG, PNG and WebP images are supported.");
        }

        PortfolioImage image = repository.save(new PortfolioImage(carpenter, title.trim(), category.trim(),
                description == null ? "" : description.trim(), safeFileName(file.getOriginalFilename()),
                file.getContentType(), file.getSize(), file.getBytes()));
        auditLogService.record(carpenter, AuditAction.CREATED, "PortfolioImage", image.getId(),
                "Uploaded portfolio image " + image.getTitle());
        return PortfolioImageResponse.from(image);
    }

    private void validateText(String value, String label, int maxLength) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label + " is required.");
        if (value.trim().length() > maxLength) throw new IllegalArgumentException(label + " is too long.");
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "portfolio-image";
        String normalized = fileName.replace('\\', '/');
        String leaf = normalized.substring(normalized.lastIndexOf('/') + 1);
        return leaf.length() > 255 ? leaf.substring(leaf.length() - 255) : leaf;
    }
}
