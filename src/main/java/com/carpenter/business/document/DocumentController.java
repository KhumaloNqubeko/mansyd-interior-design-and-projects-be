package com.carpenter.business.document;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.document.dto.DocumentRequest;
import com.carpenter.business.document.dto.DocumentResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DocumentResponse create(@Valid @RequestBody DocumentRequest request, Authentication authentication) {
        return documentService.create(request, authentication);
    }

    @GetMapping
    PageResponse<DocumentResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return documentService.all(authentication, pageable);
    }

    @GetMapping("/my")
    PageResponse<DocumentResponse> my(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return documentService.my(authentication, pageable);
    }

    @PutMapping("/{id}")
    DocumentResponse update(@PathVariable UUID id, @Valid @RequestBody DocumentRequest request,
                            Authentication authentication) {
        return documentService.update(id, request, authentication);
    }

    @PatchMapping("/{id}/archive")
    DocumentResponse archive(@PathVariable UUID id, Authentication authentication) {
        return documentService.archive(id, authentication);
    }
}
