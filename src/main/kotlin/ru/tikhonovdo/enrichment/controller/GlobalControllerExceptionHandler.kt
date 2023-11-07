package ru.tikhonovdo.enrichment.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalControllerExceptionHandler {

    @ExceptionHandler(MissingPathVariableException::class)
    fun handleConflict(ex: MissingPathVariableException): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
    }

}