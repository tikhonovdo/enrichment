package ru.tikhonovdo.enrichment.batch.matching.transaction

import org.slf4j.LoggerFactory
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.jdbc.core.JdbcTemplate

class ValidatableRowCounter(private val jdbcTemplate: JdbcTemplate): Tasklet {

    private val log = LoggerFactory.getLogger(ValidatableRowCounter::class.java)

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val needValidateRowCount = jdbcTemplate.queryForObject("""
            SELECT count(1) FROM matching.transaction
            WHERE NOT (validated OR account_id IS NOT NULL
                AND ((category_id IS NOT NULL AND event_id IS NULL) OR (category_id IS NULL AND event_id IS NOT NULL)));
        """.trimIndent(), Long::class.java)
        log.info("$needValidateRowCount rows need to validate in matching.transaction")

        return RepeatStatus.FINISHED
    }
}