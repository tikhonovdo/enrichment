package ru.tikhonovdo.enrichment.controller

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/session")
@Controller
class SessionController {

    @DeleteMapping
    fun invalidateSession(session: HttpSession) {
        session.invalidate()
    }
}
