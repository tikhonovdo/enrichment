package ru.tikhonovdo.enrichment.batch.matching.transaction

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Тасклет задает `financepm.transaction.matching_transaction_id` для расходных транзакций и переводов
 * по точному совпадению кортежа из `matching.transaction`:
 *  - `type`
 *  - `category_id`
 *  - `sum`
 *  - `date`
 *  - `event_id`
 *
 *  //todo: возможно матчинг по дате следует пересмотреть ввиду потери точности при обратном сравнении --
 *  исходное приложение создает записи с менее точным заданием даты транзакции
 */
class MatchWithMasterTransactionsTasklet(private val jdbcTemplate: JdbcTemplate): Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val updated = jdbcTemplate.update("""
        -- refresh financepm.transaction.matching_transaction_id by row data
        UPDATE financepm.transaction SET matching_transaction_id = mapping.matching_id 
        FROM (
            -- outcome transaction
            SELECT ft.id as master_id, mt.id as matching_id
            FROM matching.transaction mt
            JOIN financepm.transaction ft ON
                (ft.type, ft.category_id, ft.sum, ft.account_id, ft.date) = (mt.type, mt.category_id, mt.sum, mt.account_id, mt.date)
            WHERE ft.event_id IS NULL AND mt.event_id IS NULL AND ft.matching_transaction_id IS NULL
            UNION
            -- transfers
            SELECT ft.id as master_id, mt.id as matching_id
            FROM matching.transaction mt
            JOIN financepm.transaction ft ON
                (ft.type, ft.event_id, ft.sum, ft.account_id, ft.date) = (mt.type, mt.event_id, mt.sum, mt.account_id, mt.date)
            WHERE ft.category_id IS NULL AND mt.category_id IS NULL AND ft.matching_transaction_id IS NULL
        ) as mapping 
        WHERE id = mapping.master_id;
        """.trimIndent())
        contribution.incrementWriteCount(updated.toLong())
        return RepeatStatus.FINISHED
    }

}