package com.carpenter.business.inventory;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    Page<StockTransaction> findByMaterialId(UUID materialId, Pageable pageable);
}
