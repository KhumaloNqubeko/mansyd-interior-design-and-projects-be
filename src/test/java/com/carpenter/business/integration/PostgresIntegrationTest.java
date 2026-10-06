package com.carpenter.business.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import com.carpenter.business.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class PostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired UserRepository users;
    @Autowired jakarta.persistence.EntityManager entities;
    @Autowired com.carpenter.business.project.ProjectCollaborationService collaboration;

    @Test
    @org.springframework.transaction.annotation.Transactional
    void projectPhotosAndCompletionReviewPersistWithOwnershipChecks() throws Exception {
        String unique = java.util.UUID.randomUUID().toString();
        User customer = users.save(new User(unique + "@customer.test", "hash", Role.CUSTOMER, AccountStatus.ACTIVE));
        User owner = users.save(new User(unique + "@owner.test", "hash", Role.CARPENTER, AccountStatus.ACTIVE));
        User other = users.save(new User(unique + "@other.test", "hash", Role.CUSTOMER, AccountStatus.ACTIVE));
        var profile = new com.carpenter.business.customer.Customer(customer, "Integration Customer", "+27" + unique.substring(0, 8), "1 Main", null, "Johannesburg", "2000");
        entities.persist(profile);
        var request = new com.carpenter.business.servicerequest.ServiceRequest(profile, "Kitchen", "Cabinetry", "Email", "1 Main");
        entities.persist(request);
        var quote = new com.carpenter.business.quotation.Quotation("QUO-" + unique.substring(0, 8), request, java.time.LocalDate.now().plusDays(7), "");
        entities.persist(quote);
        var order = new com.carpenter.business.order.Order("ORD-" + unique.substring(0, 8), quote);
        entities.persist(order);
        var project = new com.carpenter.business.project.Project("PRJ-" + unique.substring(0, 8), order);
        project.changeStatus(com.carpenter.business.project.ProjectStatus.INSTALLED, 95, java.time.LocalDate.now(), null);
        entities.persist(project); entities.flush();
        var customerAuth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(customer.getEmail(), "", java.util.List.of());
        var ownerAuth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(owner.getEmail(), "", java.util.List.of());
        var otherAuth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(other.getEmail(), "", java.util.List.of());
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", bytes);
        var photo = collaboration.photo(project.getId(), "Installation photo", new org.springframework.mock.web.MockMultipartFile("file", "photo.png", "image/png", bytes.toByteArray()), customerAuth);
        entities.flush(); entities.clear();
        assertThat(collaboration.list(project.getId(), customerAuth, org.springframework.data.domain.PageRequest.of(0, 20)).content()).extracting("id").contains(photo.id());
        assertThat(collaboration.photoContent(project.getId(), photo.id(), customerAuth).getPhotoData()).isEqualTo(bytes.toByteArray());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> collaboration.photoContent(project.getId(), photo.id(), otherAuth))
                .isInstanceOf(com.carpenter.business.exception.UnauthorisedOperationException.class);
        var review = collaboration.requestReview(project.getId(), ownerAuth);
        var completed = collaboration.decide(project.getId(), new com.carpenter.business.project.dto.CompletionDecisionRequest(review.completionReviewId(), true, "Looks good"), customerAuth);
        entities.flush(); entities.clear();
        var stored = entities.find(com.carpenter.business.project.Project.class, project.getId());
        assertThat(stored.getStatus()).isEqualTo(com.carpenter.business.project.ProjectStatus.COMPLETED);
        assertThat(stored.getCustomerConfirmedBy()).isEqualTo(customer.getId());
        assertThat(stored.getCustomerConfirmedAt()).isNotNull();
        assertThat(completed.progress()).isEqualTo(100);
    }
    @Test
    void flywaySchemaSupportsUserPersistence() {
        users.saveAndFlush(new User("integration@example.com", "bcrypt", Role.CUSTOMER, AccountStatus.ACTIVE));
        assertThat(users.findByEmailIgnoreCase("INTEGRATION@example.com")).isPresent();
    }
}

