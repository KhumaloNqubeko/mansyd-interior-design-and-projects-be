package com.carpenter.business.invoice;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.invoice.dto.InvoiceRequest;
import com.carpenter.business.invoice.dto.InvoiceResponse;
import com.carpenter.business.invoice.dto.InvoiceUpdateRequest;
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
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    InvoiceResponse create(@Valid @RequestBody InvoiceRequest request, Authentication authentication) {
        return invoiceService.create(request, authentication);
    }

    @GetMapping
    PageResponse<InvoiceResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return invoiceService.all(authentication, pageable);
    }

    @GetMapping("/my")
    PageResponse<InvoiceResponse> myInvoices(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return invoiceService.myInvoices(authentication, pageable);
    }

    @GetMapping("/{id}")
    InvoiceResponse get(@PathVariable UUID id, Authentication authentication) {
        return invoiceService.get(id, authentication);
    }

    @PutMapping("/{id}")
    InvoiceResponse update(@PathVariable UUID id, @Valid @RequestBody InvoiceUpdateRequest request,
                           Authentication authentication) {
        return invoiceService.update(id, request, authentication);
    }

    @PatchMapping("/{id}/issue")
    InvoiceResponse issue(@PathVariable UUID id, Authentication authentication) {
        return invoiceService.issue(id, authentication);
    }

    @PatchMapping("/{id}/cancel")
    InvoiceResponse cancel(@PathVariable UUID id, Authentication authentication) {
        return invoiceService.cancel(id, authentication);
    }
}
