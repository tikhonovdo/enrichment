package ru.tikhonovdo.enrichment.batch.matching.transaction

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Тасклет экспортирует данные из `matching.transaction` в `financepm.transaction` при
 *
 *     1. формальной валидности значений записи ограничениям `financepm.transaction`
 *     2. в случае, если запись не была сматчена ранее
 *     3. не является компенсацией другой транзакции (непустое поле `matching.transaction.refund_for_id`)
 */
class ExportMatchingTransactionsTasklet(private val jdbcTemplate: JdbcTemplate): Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val updated = jdbcTemplate.update("""
            INSERT INTO financepm.transaction (id, name, type, category_id, date, sum, account_id, description, event_id, matching_transaction_id)
                SELECT nextval('financepm.transaction_id_seq'), mt.name, mt.type, mt.category_id, mt.date, mt.sum, mt.account_id, mt.description, mt.event_id, mt.id
                FROM matching.transaction mt
                WHERE mt.account_id IS NOT NULL
                  AND ((mt.category_id IS NOT NULL AND mt.event_id IS NULL) OR (mt.category_id IS NULL AND mt.event_id IS NOT NULL))
                  AND mt.id NOT IN (
                    SELECT distinct matching_transaction_id FROM financepm.transaction
                    WHERE matching_transaction_id IS NOT NULL
                  )
                  AND mt.refund_for_id IS NULL;
        """.trimIndent())
        contribution.incrementWriteCount(updated.toLong())
        return RepeatStatus.FINISHED
    }

}