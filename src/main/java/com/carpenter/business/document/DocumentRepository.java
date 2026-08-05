package com.carpenter.business.document;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Page<Document> findByCustomerUserIdAndCustomerVisibleTrueAndStatus(UUID userId, DocumentStatus status, Pageable pageable);
    Page<Document> findByProjectId(UUID projectId, Pageable pageable);
    Page<Document> findByServiceRequestId(UUID serviceRequestId, Pageable pageable);
    Page<Document> findByInvoiceId(UUID invoiceId, Pageable pageable);
}
