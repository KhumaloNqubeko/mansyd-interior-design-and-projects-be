package com.carpenter.business.inventory;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, UUID> {
    boolean existsByCodeIgnoreCase(String code);
}
