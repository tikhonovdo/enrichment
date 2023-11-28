package ru.tikhonovdo.enrichment.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioData
import ru.tikhonovdo.enrichment.service.importscenario.ImportService
import java.util.*
import java.util.function.Supplier

@RestController
@RequestMapping("/import")
@Controller
class ImportController(private val importService: ImportService) {

    @PostMapping("/{bank}")
    fun startLoginSequence(@PathVariable("bank") bank: Bank, @RequestBody scenarioData: ImportScenarioData): ResponseEntity<Any> {
        return response { importService.login(bank, scenarioData) }
    }

    @PostMapping("/{bank}/complete")
    fun importBankData(@PathVariable("bank") bank: Bank, @RequestBody scenarioData: ImportScenarioData): ResponseEntity<Any> {
         return response { importService.confirmLoginAndImport(bank, scenarioData) }
    }

    private fun response(resultSupplier: Supplier<Boolean>): ResponseEntity<Any> {
        return if (resultSupplier.get())
            ResponseEntity.ok(null)
        else
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

}
