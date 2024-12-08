package ru.tikhonovdo.enrichment.controller

import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioContext
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState

@RestController
@RequestMapping("/session")
@Controller
class SessionController {

    private val log = LoggerFactory.getLogger(this::class.java)

    @DeleteMapping
    fun invalidateSession(session: HttpSession, scenarioContext: ImportScenarioContext) {
        log.info("Session ${session.id} invalidation requested")
        scenarioContext.resetContextWithState(ScenarioState.START)
        session.invalidate()
    }
}
