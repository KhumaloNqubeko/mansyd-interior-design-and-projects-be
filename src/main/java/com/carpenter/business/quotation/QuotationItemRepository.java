package com.carpenter.business.quotation;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationItemRepository extends JpaRepository<QuotationItem, UUID> {
}
