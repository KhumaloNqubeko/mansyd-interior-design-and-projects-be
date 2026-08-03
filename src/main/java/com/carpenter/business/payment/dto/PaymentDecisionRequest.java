package com.carpenter.business.payment.dto;

import jakarta.validation.constraints.Size;

public record PaymentDecisionRequest(@Size(max = 1000) String notes) {
}
