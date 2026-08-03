package com.carpenter.business.inventory;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.DuplicateResourceException;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.inventory.dto.MaterialRequest;
import com.carpenter.business.inventory.dto.MaterialResponse;
import com.carpenter.business.inventory.dto.ProjectMaterialResponse;
import com.carpenter.business.inventory.dto.StockChangeRequest;
import com.carpenter.business.inventory.dto.StockTransactionResponse;
import com.carpenter.business.project.Project;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.supplier.Supplier;
import com.carpenter.business.supplier.SupplierRepository;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMaterialRepository projectMaterialRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CurrentUser currentUser;

    public InventoryService(MaterialRepository materialRepository, SupplierRepository supplierRepository,
                            ProjectRepository projectRepository, ProjectMaterialRepository projectMaterialRepository,
                            StockTransactionRepository stockTransactionRepository, CurrentUser currentUser) {
        this.materialRepository = materialRepository;
        this.supplierRepository = supplierRepository;
        this.projectRepository = projectRepository;
        this.projectMaterialRepository = projectMaterialRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        if (materialRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new DuplicateResourceException("A material with this code already exists.");
        }
        Material material = materialRepository.save(new Material(trim(request.code()), trim(request.name()),
                request.unitOfMeasure(), money(request.unitCost()), quantity(request.reorderLevel()),
                supplier(request.supplierId())));
        return MaterialResponse.from(material);
    }

    @Transactional(readOnly = true)
    public PageResponse<MaterialResponse> materials(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(materialRepository.findAll(pageable).map(MaterialResponse::from));
    }

    @Transactional
    public MaterialResponse updateMaterial(UUID id, MaterialRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Material material = material(id);
        material.update(trim(request.name()), request.unitOfMeasure(), money(request.unitCost()),
                quantity(request.reorderLevel()), supplier(request.supplierId()), request.active());
        return MaterialResponse.from(material);
    }

    @Transactional
    public StockTransactionResponse changeStock(StockChangeRequest request, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        Material material = material(request.materialId());
        BigDecimal quantity = quantity(request.quantity());
        Project project = request.projectId() == null ? null : project(request.projectId());
        BigDecimal delta = delta(request.type(), quantity);
        if (material.getStockQuantity().add(delta).signum() < 0) {
            throw new UnauthorisedOperationException("Stock cannot become negative.");
        }
        material.applyQuantityChange(delta);
        updateProjectAllocation(project, material, request.type(), quantity);
        StockTransaction transaction = stockTransactionRepository.save(new StockTransaction(material, project, user,
                request.type(), quantity, clean(request.notes())));
        return StockTransactionResponse.from(transaction);
    }

    @Transactional(readOnly = true)
    public PageResponse<StockTransactionResponse> transactions(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(stockTransactionRepository.findAll(pageable).map(StockTransactionResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectMaterialResponse> projectMaterials(UUID projectId, Authentication authentication,
                                                                 Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(projectMaterialRepository.findByProjectId(projectId, pageable).map(ProjectMaterialResponse::from));
    }

    private void updateProjectAllocation(Project project, Material material, StockTransactionType type, BigDecimal quantity) {
        if (type != StockTransactionType.PROJECT_ALLOCATION && type != StockTransactionType.PROJECT_RETURN) return;
        if (project == null) throw new UnauthorisedOperationException("Project stock transactions require a project.");
        ProjectMaterial projectMaterial = projectMaterialRepository.findByProjectIdAndMaterialId(project.getId(), material.getId())
                .orElseGet(() -> projectMaterialRepository.save(new ProjectMaterial(project, material, BigDecimal.ZERO.setScale(3))));
        if (type == StockTransactionType.PROJECT_ALLOCATION) {
            projectMaterial.add(quantity);
        } else {
            if (projectMaterial.getAllocatedQuantity().compareTo(quantity) < 0) {
                throw new UnauthorisedOperationException("Cannot return more material than was allocated.");
            }
            projectMaterial.subtract(quantity);
        }
    }

    private BigDecimal delta(StockTransactionType type, BigDecimal quantity) {
        return switch (type) {
            case STOCK_IN, PROJECT_RETURN, ADJUSTMENT_IN -> quantity;
            case PROJECT_ALLOCATION, ADJUSTMENT_OUT, DAMAGED -> quantity.negate();
        };
    }

    private Material material(UUID id) {
        return materialRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Material was not found."));
    }

    private Project project(UUID id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project was not found."));
    }

    private Supplier supplier(UUID id) {
        return id == null ? null : supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier was not found."));
    }

    private BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal quantity(BigDecimal value) { return value.setScale(3, RoundingMode.HALF_UP); }
    private String trim(String value) { return value.trim(); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
