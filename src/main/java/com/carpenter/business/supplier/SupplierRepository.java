package com.carpenter.business.supplier;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
