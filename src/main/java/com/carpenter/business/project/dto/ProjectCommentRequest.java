package com.carpenter.business.project.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ProjectCommentRequest(@NotBlank @Size(max = 2000) String message) { }
