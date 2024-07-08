package ru.tikhonovdo.enrichment.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.tikhonovdo.enrichment.domain.Bank
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
                          @PathVariable("stepName") stepName: String,
                          @RequestBody scenarioData: ImportScenarioData): ResponseEntity<Any> {
         return response { importService.performImportStep(bank, stepName, scenarioData) }
    }

    @GetMapping("/{bank}/last-update")
    fun getLastUpdateDate(@PathVariable("bank") bank: Bank): ResponseEntity<LocalDateTime> {
        return ResponseEntity.ok(importService.getLastUpdateDate(bank))
    }

    private fun response(resultSupplier: Supplier<ScenarioState?>): ResponseEntity<Any> {
        val state = resultSupplier.get()
        return if (state != null) {
            ResponseEntity.ok(state.stepName)
        } else {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }
    }

}
