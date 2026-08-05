package com.carpenter.business.document.dto;

import com.carpenter.business.document.Document;
import com.carpenter.business.document.DocumentStatus;
import com.carpenter.business.document.DocumentType;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(UUID id, String title, DocumentType type, DocumentStatus status, String fileName,
                               String contentType, Long fileSizeBytes, String storageUrl, String notes,
                               boolean customerVisible, UUID customerId, String customerName,
                               UUID serviceRequestId, String serviceRequestTitle, UUID projectId, String projectNumber,
                               UUID invoiceId, String invoiceNumber, String uploadedByEmail,
                               Instant createdAt, Instant updatedAt) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(document.getId(), document.getTitle(), document.getType(), document.getStatus(),
                document.getFileName(), document.getContentType(), document.getFileSizeBytes(), document.getStorageUrl(),
                document.getNotes(), document.isCustomerVisible(), document.getCustomer().getId(),
                document.getCustomer().getFullName(),
                document.getServiceRequest() == null ? null : document.getServiceRequest().getId(),
                document.getServiceRequest() == null ? null : document.getServiceRequest().getTitle(),
                document.getProject() == null ? null : document.getProject().getId(),
                document.getProject() == null ? null : document.getProject().getProjectNumber(),
                document.getInvoice() == null ? null : document.getInvoice().getId(),
                document.getInvoice() == null ? null : document.getInvoice().getInvoiceNumber(),
                document.getUploadedBy().getEmail(), document.getCreatedAt(), document.getUpdatedAt());
    }
}
