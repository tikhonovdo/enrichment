package ru.tikhonovdo.enrichment.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.ImportStepResult
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioData
import ru.tikhonovdo.enrichment.service.importscenario.ImportService
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState
import java.time.LocalDateTime
import java.util.function.Supplier

@RestController
@RequestMapping("/import")
@Controller
class ImportController(private val importService: ImportService) {

    @PostMapping("/{bank}/{stepName}")
    fun performImportStep(@PathVariable("bank") bank: Bank,
                          @PathVariable("stepName") state: ScenarioState,
                          @RequestBody scenarioData: ImportScenarioData): ResponseEntity<Any> {
        return response { importService.performImportStep(bank, state, scenarioData) }
    }

    @GetMapping("/{bank}/last-update")
    fun getLastUpdateDate(@PathVariable("bank") bank: Bank): ResponseEntity<LocalDateTime> {
        return ResponseEntity.ok(importService.getLastUpdateDate(bank))
    }

    private fun response(responseSupplier: Supplier<ImportStepResult?>): ResponseEntity<Any> {
        val response = responseSupplier.get()
        return if (response != null) {
            when (response.state) {
                ScenarioState.FAILURE ->
                    ResponseEntity.internalServerError().body(response)

                else ->
                    ResponseEntity.ok(response)
            }
        } else {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }
    }

}
