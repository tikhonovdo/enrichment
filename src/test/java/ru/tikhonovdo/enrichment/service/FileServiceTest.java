package ru.tikhonovdo.enrichment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.tikhonovdo.enrichment.domain.Bank;
import ru.tikhonovdo.enrichment.domain.dto.FinancePmData;
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction;
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository;
import ru.tikhonovdo.enrichment.util.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileServiceTest {
    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:13.3-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    FileService fileService;

    @Autowired
    DraftTransactionRepository draftTransactionRepository;

    final ObjectMapper MAPPER = JsonMapper.Companion.getJSON_MAPPER();

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Test
    void shouldSaveFinancePMAndLoadExactlySame() throws IOException, URISyntaxException {
        URL sourceUrl = FileServiceTest.class.getResource("financePM_test.data");
        File source = Paths.get(sourceUrl.toURI()).toFile();
        FinancePmData expected = MAPPER.readValue(source, FinancePmData.class);
        bringScaleToValue(expected, 6);

        given()
                .multiPart("file", source)
                .param("reset", "true")
                .when()
                .post("/file/upload")
                .then()
                .statusCode(200);
        FinancePmData actual = MAPPER.readValue(fileService.load().getContentAsByteArray(), FinancePmData.class) ;

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldSaveTheSameFinancePMDataOnlyOnce() throws IOException, URISyntaxException {
        URL sourceUrl = FileServiceTest.class.getResource("financePM_test.data");
        File source = Paths.get(sourceUrl.toURI()).toFile();
        FinancePmData expected = MAPPER.readValue(source, FinancePmData.class);
        bringScaleToValue(expected, 6);

        given()
                .multiPart("file", source)
                .param("reset", "true")
                .when()
                .post("/file/upload")
                .then()
                .statusCode(200);
        given()
                .multiPart("file", source)
                .when()
                .post("/file/upload")
                .then()
                .statusCode(200);
        FinancePmData actual = MAPPER.readValue(fileService.load().getContentAsByteArray(), FinancePmData.class) ;

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldSaveTheSameTinkoffDataOnlyOnce() throws URISyntaxException {
        URL sourceUrl = FileServiceTest.class.getResource("operations_tinkoff_test_03.08.20-09.08.20.xls");
        File source = Paths.get(sourceUrl.toURI()).toFile();

        given()
                .multiPart("file", source)
                .when()
                .post("/file/upload")
                .then()
                .statusCode(200);
        given()
                .multiPart("file", source)
                .when()
                .post("/file/upload")
                .then()
                .statusCode(200);


        List<DraftTransaction> draftTransactions = draftTransactionRepository.findAllByBankId(Bank.TINKOFF.getId());

        Assertions.assertEquals(25, draftTransactions.size());
    }


    private void bringScaleToValue(FinancePmData data, int newScale) {
        data.getTransactions().forEach(transaction ->
                transaction.setSum(transaction.getSum().setScale(newScale)));
        data.getAccounts().forEach(account ->
                account.setBalance(account.getBalance().setScale(newScale)));
        data.getArrears().forEach(arrear ->
                arrear.setBalance(arrear.getBalance().setScale(newScale)));
    }

}
