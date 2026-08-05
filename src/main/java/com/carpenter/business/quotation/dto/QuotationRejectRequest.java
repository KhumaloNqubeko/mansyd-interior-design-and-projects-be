package com.carpenter.business.quotation.dto;

import jakarta.validation.constraints.Size;

public record QuotationRejectRequest(@Size(max = 1000) String rejectionNotes) {
}
