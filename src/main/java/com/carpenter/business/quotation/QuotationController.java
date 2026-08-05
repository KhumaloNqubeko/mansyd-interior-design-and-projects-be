package com.carpenter.business.quotation;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.quotation.dto.QuotationItemRequest;
import com.carpenter.business.quotation.dto.QuotationRequest;
import com.carpenter.business.quotation.dto.QuotationResponse;
import com.carpenter.business.quotation.dto.QuotationRejectRequest;
import com.carpenter.business.quotation.dto.QuotationUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/quotations")
public class QuotationController {
    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    QuotationResponse create(@Valid @RequestBody QuotationRequest request, Authentication authentication) {
        return quotationService.create(request, authentication);
    }

    @GetMapping
    PageResponse<QuotationResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return quotationService.all(authentication, pageable);
    }

    @GetMapping("/my")
    PageResponse<QuotationResponse> myQuotations(Authentication authentication,
                                                 @PageableDefault(size = 20) Pageable pageable) {
        return quotationService.myQuotations(authentication, pageable);
    }

    @GetMapping("/{id}")
    QuotationResponse get(@PathVariable UUID id, Authentication authentication) {
        return quotationService.get(id, authentication);
    }

    @PutMapping("/{id}")
    QuotationResponse update(@PathVariable UUID id, @Valid @RequestBody QuotationUpdateRequest request,
                             Authentication authentication) {
        return quotationService.update(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id, Authentication authentication) {
        quotationService.delete(id, authentication);
    }

    @PostMapping("/{id}/items")
    QuotationResponse addItem(@PathVariable UUID id, @Valid @RequestBody QuotationItemRequest request,
                              Authentication authentication) {
        return quotationService.addItem(id, request, authentication);
    }

    @PutMapping("/{id}/items/{itemId}")
    QuotationResponse updateItem(@PathVariable UUID id, @PathVariable UUID itemId,
                                 @Valid @RequestBody QuotationItemRequest request, Authentication authentication) {
        return quotationService.updateItem(id, itemId, request, authentication);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    QuotationResponse deleteItem(@PathVariable UUID id, @PathVariable UUID itemId, Authentication authentication) {
        return quotationService.deleteItem(id, itemId, authentication);
    }

    @PatchMapping("/{id}/submit")
    QuotationResponse submit(@PathVariable UUID id, Authentication authentication) {
        return quotationService.submit(id, authentication);
    }

    @PatchMapping("/{id}/accept")
    QuotationResponse accept(@PathVariable UUID id, Authentication authentication) {
        return quotationService.accept(id, authentication);
    }

    @PatchMapping("/{id}/reject")
    QuotationResponse reject(@PathVariable UUID id, @Valid @RequestBody QuotationRejectRequest request,
                             Authentication authentication) {
        return quotationService.reject(id, request, authentication);
    }
}
