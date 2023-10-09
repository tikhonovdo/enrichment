package ru.tikhonovdo.enrichment.batch.matching.transaction.base

import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.jdbc.core.JdbcTemplate

class ValidationNeededRowCounter(private val jdbcTemplate: JdbcTemplate) : StepExecutionListener {

    private val log = LoggerFactory.getLogger(ValidationNeededRowCounter::class.java)

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val needValidateRowCount = jdbcTemplate.queryForObject("""
            SELECT count(1) FROM matching.transaction
            WHERE NOT (validated OR account_id IS NOT NULL
                AND ((category_id IS NOT NULL AND event_id IS NULL) OR (category_id IS NULL AND event_id IS NOT NULL)));
        """.trimIndent(), Long::class.java)
        log.info("$needValidateRowCount rows need to validate in matching.transaction")

        return super.afterStep(stepExecution)
    }
}