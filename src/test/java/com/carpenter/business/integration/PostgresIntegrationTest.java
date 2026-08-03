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

    @Test
    void flywaySchemaSupportsUserPersistence() {
        users.saveAndFlush(new User("integration@example.com", "bcrypt", Role.CUSTOMER, AccountStatus.ACTIVE));
        assertThat(users.findByEmailIgnoreCase("INTEGRATION@example.com")).isPresent();
    }
}

