package com.carpenter.business.supplier;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.DuplicateResourceException;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.supplier.dto.SupplierRequest;
import com.carpenter.business.supplier.dto.SupplierResponse;
import com.carpenter.business.user.Role;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final CurrentUser currentUser;

    public SupplierService(SupplierRepository supplierRepository, CurrentUser currentUser) {
        this.supplierRepository = supplierRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        if (supplierRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new DuplicateResourceException("A supplier with this name already exists.");
        }
        return SupplierResponse.from(supplierRepository.save(new Supplier(trim(request.name()),
                clean(request.contactName()), clean(request.email()), clean(request.phoneNumber()))));
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(supplierRepository.findAll(pageable).map(SupplierResponse::from));
    }

    @Transactional
    public SupplierResponse update(UUID id, SupplierRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier was not found."));
        supplier.update(trim(request.name()), clean(request.contactName()), clean(request.email()),
                clean(request.phoneNumber()), request.active());
        return SupplierResponse.from(supplier);
    }

    private String trim(String value) { return value.trim(); }
    private String clean(String value) { return value == null ? null : value.trim(); }
}
