package com.carpenter.business.document.dto;

import com.carpenter.business.document.DocumentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record DocumentRequest(@NotBlank @Size(max = 160) String title,
                              @NotNull DocumentType type,
                              @NotBlank @Size(max = 180) String fileName,
                              @NotBlank @Size(max = 120) String contentType,
                              @Min(0) Long fileSizeBytes,
                              @NotBlank @Size(max = 500) String storageUrl,
                              @Size(max = 1000) String notes,
                              @NotNull Boolean customerVisible,
                              @NotNull UUID customerId,
                              UUID serviceRequestId,
                              UUID projectId,
                              UUID invoiceId) {
}
