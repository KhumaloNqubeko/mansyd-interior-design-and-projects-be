package com.carpenter.business.supplier.dto;

import com.carpenter.business.supplier.Supplier;
import java.util.UUID;

public record SupplierResponse(UUID id, String name, String contactName, String email, String phoneNumber,
                               boolean active) {
    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.getContactName(),
                supplier.getEmail(), supplier.getPhoneNumber(), supplier.isActive());
    }
}
