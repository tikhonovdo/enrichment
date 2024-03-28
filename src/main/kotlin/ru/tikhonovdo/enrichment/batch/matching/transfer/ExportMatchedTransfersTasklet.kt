package ru.tikhonovdo.enrichment.batch.matching.transfer

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Тасклет экспортирует данные из `matching.transfer` в `financepm.transfer` при условии
 *
 *     1. формальной валидности значений записи ограничениям `financepm.transfer` и
 *     2. в случае, если запись не была сматчена ранее
 */
class ExportMatchedTransfersTasklet(private val jdbcTemplate: JdbcTemplate): Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val updated = jdbcTemplate.update("""
            INSERT INTO financepm.transfer (name, transaction_id_from, transaction_id_to)
                SELECT t_from.name, t_from.id, t_to.id
                FROM matching.transfer mt
                JOIN financepm.transaction t_from ON t_from.matching_transaction_id = mt.matching_transaction_id_from 
                JOIN financepm.transaction t_to ON t_to.matching_transaction_id = mt.matching_transaction_id_to
                WHERE NOT EXISTS(
                        SELECT 1
                        FROM financepm.transfer
                        WHERE transaction_id_from = t_from.id AND transaction_id_to = t_to.id
                    )
                    AND t_from.event_id IS NOT NULL AND t_from.category_id IS NULL AND t_from.account_id IS NOT NULL
                    AND t_to.event_id IS NOT NULL AND t_to.category_id IS NULL AND t_to.account_id IS NOT NULL;
        """.trimIndent())
        contribution.incrementWriteCount(updated.toLong())
        return RepeatStatus.FINISHED
    }

}