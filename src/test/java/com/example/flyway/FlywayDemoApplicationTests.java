package com.example.flyway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
class FlywayDemoApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    )
    .withDatabaseName("flyway_test")
    .withUsername("test_user")
    .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoadsAndFlywayMigrates() {
        // If the context loads successfully, it means Flyway migrated the DB successfully
        // without crashing upon startup.

        // We can also verify our seed data from V3__insert_seed_data.sql is present
        long count = userRepository.count();
        assertEquals(3, count, "There should be 3 users from the seed data");

        User alice = userRepository.findByUsername("alice").orElse(null);
        assertNotNull(alice, "Alice should be present");
        assertEquals("alice@example.com", alice.getEmail(), "Alice should have the correct email added from V2 schema change");
    }
}
