package ru.tikhonovdo.enrichment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
class FinanceDataEnricherApplication

fun main(vararg args: String) {
    runApplication<FinanceDataEnricherApplication>(*args)
}