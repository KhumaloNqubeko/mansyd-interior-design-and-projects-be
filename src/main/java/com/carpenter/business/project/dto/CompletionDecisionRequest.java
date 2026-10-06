package com.carpenter.business.project.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public record CompletionDecisionRequest(@NotNull UUID reviewId, @NotNull Boolean confirmed,
                                        @Size(max = 2000) String message) { }
