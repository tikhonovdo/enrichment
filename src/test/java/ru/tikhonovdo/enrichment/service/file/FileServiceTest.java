package ru.tikhonovdo.enrichment.service.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tikhonovdo.enrichment.DatabaseAwareTest;
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

public class FileServiceTest extends DatabaseAwareTest {

    @Autowired
    FileService fileService;

    @Autowired
    DraftTransactionRepository draftTransactionRepository;

    final ObjectMapper MAPPER = JsonMapper.Companion.getJSON_MAPPER();

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
                .post("/file")
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
                .post("/file")
                .then()
                .statusCode(200);
        given()
                .multiPart("file", source)
                .when()
                .post("/file")
                .then()
                .statusCode(200);
        FinancePmData actual = MAPPER.readValue(fileService.load().getContentAsByteArray(), FinancePmData.class) ;

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldSaveTheSameTinkoffDataOnlyOnce() throws URISyntaxException {
        URL sourceUrl = FileServiceTest.class.getResource("tinkoff/operations_03.08.20-04.08.20.xls");
        File source = Paths.get(sourceUrl.toURI()).toFile();

        given()
                .multiPart("file", source)
                .when()
                .post("/file")
                .then()
                .statusCode(200);
        given()
                .multiPart("file", source)
                .when()
                .post("/file")
                .then()
                .statusCode(200);

        List<DraftTransaction> draftTransactions = draftTransactionRepository.findAllByBankId(Bank.TINKOFF.getId());

        Assertions.assertEquals(6, draftTransactions.size());
    }

    @Test
    void shouldSaveTheSameAlfaDataOnlyOnce() throws URISyntaxException {
        URL sourceUrl = FileServiceTest.class.getResource("alfa/Statement 06.06.2021 - 06.06.2021.xlsx");
        File source = Paths.get(sourceUrl.toURI()).toFile();

        given()
                .multiPart("file", source)
                .when()
                .post("/file")
                .then()
                .statusCode(200);
        given()
                .multiPart("file", source)
                .when()
                .post("/file")
                .then()
                .statusCode(200);

        List<DraftTransaction> draftTransactions = draftTransactionRepository.findAllByBankId(Bank.ALFA.getId());

        Assertions.assertEquals(2, draftTransactions.size());
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
