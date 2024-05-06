package ru.tikhonovdo.enrichment;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class DatabaseAwareTest {
    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13.3-alpine");
    static Path seleniumDownloadPath = Paths.get("target", "tmp");

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    static {
        postgres.start();
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        Files.createDirectories(seleniumDownloadPath.toAbsolutePath());
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("selenoid-host-download-path", () -> seleniumDownloadPath.toFile().getAbsolutePath());
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        cleanTables();
    }

    private void cleanTables() {
        jdbcTemplate.update("TRUNCATE TABLE matching.draft_transaction CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE financepm.transaction CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE financepm.transfer CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE financepm.account CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE financepm.category CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE financepm.currency CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE financepm.arrear CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE financepm.arrear_transaction CASCADE");
    }

}
