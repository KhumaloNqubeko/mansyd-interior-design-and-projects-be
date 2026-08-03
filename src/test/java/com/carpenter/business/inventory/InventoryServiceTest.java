package com.carpenter.business.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.inventory.dto.StockChangeRequest;
import com.carpenter.business.order.Order;
import com.carpenter.business.project.Project;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.quotation.Quotation;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.supplier.SupplierRepository;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock MaterialRepository materials;
    @Mock SupplierRepository suppliers;
    @Mock ProjectRepository projects;
    @Mock ProjectMaterialRepository projectMaterials;
    @Mock StockTransactionRepository transactions;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(materials, suppliers, projects, projectMaterials, transactions, currentUser);
    }

    @Test
    void stockCannotBecomeNegative() {
        Material material = material();
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(materials.findById(material.getId())).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> service.changeStock(new StockChangeRequest(material.getId(), null,
                StockTransactionType.ADJUSTMENT_OUT, new BigDecimal("1.000"), ""), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void allocationReducesStockAndCreatesProjectAllocation() {
        Material material = material();
        material.applyQuantityChange(new BigDecimal("5.000"));
        Project project = project();
        ProjectMaterial allocation = new ProjectMaterial(project, material, BigDecimal.ZERO.setScale(3));
        ReflectionTestUtils.setField(allocation, "id", UUID.randomUUID());
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(materials.findById(material.getId())).thenReturn(Optional.of(material));
        when(projects.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMaterials.findByProjectIdAndMaterialId(project.getId(), material.getId())).thenReturn(Optional.of(allocation));
        when(transactions.save(any(StockTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.changeStock(new StockChangeRequest(material.getId(), project.getId(),
                StockTransactionType.PROJECT_ALLOCATION, new BigDecimal("2.000"), ""), authentication);

        assertThat(material.getStockQuantity()).isEqualByComparingTo("3.000");
        assertThat(allocation.getAllocatedQuantity()).isEqualByComparingTo("2.000");
    }

    private Material material() {
        Material material = new Material("PLY", "Plywood", UnitOfMeasure.SHEET, new BigDecimal("100.00"),
                new BigDecimal("2.000"), null);
        ReflectionTestUtils.setField(material, "id", UUID.randomUUID());
        return material;
    }

    private Project project() {
        User user = user(Role.CUSTOMER);
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ServiceRequest request = new ServiceRequest(customer, "Cupboards", "Bedroom", "Email", "1 Main");
        Quotation quotation = new Quotation("QUO-TEST", request, LocalDate.now().plusDays(7), "");
        Order order = new Order("ORD-TEST", quotation);
        Project project = new Project("PRJ-TEST", order);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        return project;
    }

    private User user(Role role) {
        User user = new User(role.name().toLowerCase() + "@example.com", "hash", role, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
