package com.carpenter.business.supplier;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.supplier.dto.SupplierRequest;
import com.carpenter.business.supplier.dto.SupplierResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SupplierResponse create(@Valid @RequestBody SupplierRequest request, Authentication authentication) {
        return supplierService.create(request, authentication);
    }

    @GetMapping
    PageResponse<SupplierResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return supplierService.all(authentication, pageable);
    }

    @PutMapping("/{id}")
    SupplierResponse update(@PathVariable UUID id, @Valid @RequestBody SupplierRequest request,
                            Authentication authentication) {
        return supplierService.update(id, request, authentication);
    }
}
