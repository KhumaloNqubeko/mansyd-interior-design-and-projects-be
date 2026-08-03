package com.carpenter.business.inventory;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.inventory.dto.MaterialRequest;
import com.carpenter.business.inventory.dto.MaterialResponse;
import com.carpenter.business.inventory.dto.ProjectMaterialResponse;
import com.carpenter.business.inventory.dto.StockChangeRequest;
import com.carpenter.business.inventory.dto.StockTransactionResponse;
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
@RequestMapping("/api")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/materials")
    @ResponseStatus(HttpStatus.CREATED)
    MaterialResponse createMaterial(@Valid @RequestBody MaterialRequest request, Authentication authentication) {
        return inventoryService.createMaterial(request, authentication);
    }

    @GetMapping("/materials")
    PageResponse<MaterialResponse> materials(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return inventoryService.materials(authentication, pageable);
    }

    @PutMapping("/materials/{id}")
    MaterialResponse updateMaterial(@PathVariable UUID id, @Valid @RequestBody MaterialRequest request,
                                    Authentication authentication) {
        return inventoryService.updateMaterial(id, request, authentication);
    }

    @PostMapping("/stock-transactions")
    @ResponseStatus(HttpStatus.CREATED)
    StockTransactionResponse changeStock(@Valid @RequestBody StockChangeRequest request, Authentication authentication) {
        return inventoryService.changeStock(request, authentication);
    }

    @GetMapping("/stock-transactions")
    PageResponse<StockTransactionResponse> transactions(Authentication authentication,
                                                       @PageableDefault(size = 20) Pageable pageable) {
        return inventoryService.transactions(authentication, pageable);
    }

    @GetMapping("/projects/{projectId}/materials")
    PageResponse<ProjectMaterialResponse> projectMaterials(@PathVariable UUID projectId, Authentication authentication,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        return inventoryService.projectMaterials(projectId, authentication, pageable);
    }
}
