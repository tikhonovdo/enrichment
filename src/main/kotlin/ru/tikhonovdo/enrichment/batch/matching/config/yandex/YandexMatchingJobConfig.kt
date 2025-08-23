package ru.tikhonovdo.enrichment.batch.matching.config.yandex

import org.springframework.batch.core.Step
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.support.IteratorItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.common.CustomFlowBuilder
import ru.tikhonovdo.enrichment.batch.matching.category.CategoryMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.config.BaseMatchingJobConfig
import ru.tikhonovdo.enrichment.batch.matching.currency.CurrencyMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.transaction.yandex.YandexRecordReader
import ru.tikhonovdo.enrichment.batch.matching.transaction.yandex.YandexTransactionStepProcessor
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YandexRecord
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CurrencyMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import javax.sql.DataSource

@Configuration
@Import(BaseMatchingJobConfig::class)
class YandexMatchingJobConfig(
    jobRepository: JobRepository,
    private val dataSource: DataSource,
    private val transactionManager: PlatformTransactionManager,
    private val draftTransactionRepository: DraftTransactionRepository,
    private val categoryMatchingRepository: CategoryMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
    private val currencyMatchingRepository: CurrencyMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
): AbstractJobConfig(jobRepository) {

    @Bean
    fun yandexMatchingFlow(
        yandexCategoryMatchingStep: Step,
        yandexCurrencyMatchingStep: Step,
        yandexAccountMatchingStep: Step,
        yandexTransactionMatchingStep: Step,
    ): Flow = CustomFlowBuilder("yandexMatchingFlow")
        .addStep(yandexCategoryMatchingStep)
        .addStep(yandexCurrencyMatchingStep)
        .addStep(yandexAccountMatchingStep)
        .addStep(yandexTransactionMatchingStep)
        .build()

    @Bean
    fun yandexCategoryMatchingStep(
        yandexCategoryMatchingStepReader: ItemReader<CategoryMatching>,
        categoryMatchingStepProcessor: ItemProcessor<CategoryMatching, CategoryMatching>
    ): Step {
        return step("yandexCategoryMatchingStep")
            .chunk<CategoryMatching, CategoryMatching>(10, transactionManager)
            .reader(yandexCategoryMatchingStepReader)
            .processor(categoryMatchingStepProcessor)
            .writer { categoryMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun yandexCategoryMatchingStepReader(): ItemReader<CategoryMatching> =
        CategoryMatchingStepReader(dataSource, Bank.YANDEX,
            """
                WITH t AS (
                    SELECT DISTINCT ON (data->>'description', data->>'name')
                            data->>'description' as bank_category_name,
                            null as mcc,
                            data->>'name' as description
                    FROM matching.draft_transaction dt
                    WHERE dt.bank_id = 3
                ) SELECT * FROM t WHERE t.bank_category_name IS NOT NULL;
            """.trimIndent()
        )

    @Bean
    fun yandexCurrencyMatchingStep(
        yandexCurrencyMatchingStepReader: ItemReader<CurrencyMatching>,
        currencyMatchingStepProcessor: ItemProcessor<CurrencyMatching, CurrencyMatching>
    ): Step {
        return step("yandexCurrencyMatchingStep")
            .chunk<CurrencyMatching, CurrencyMatching>(10, transactionManager)
            .reader(yandexCurrencyMatchingStepReader)
            .processor(currencyMatchingStepProcessor)
            .writer { currencyMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun yandexCurrencyMatchingStepReader(): ItemReader<CurrencyMatching> =
        CurrencyMatchingStepReader(dataSource, Bank.YANDEX,
            """
                SELECT DISTINCT ON (data#>>'{money,currency}')
                        data#>>'{money,currency}' as currency
                FROM matching.draft_transaction
                WHERE bank_id = ${Bank.YANDEX.id} AND data#>>'{money,currency}' IS NOT NULL
                UNION DISTINCT
                SELECT DISTINCT ON (data#>>'{amount,money,currency}')
                        data#>>'{amount,money,currency}' as currency
                FROM matching.draft_transaction
                WHERE bank_id = ${Bank.YANDEX.id} AND data#>>'{amount,money,currency}' IS NOT NULL;
            """.trimIndent()
        )

    @Bean
    fun yandexAccountMatchingStep(
        yandexAccountMatchingStepReader: ItemReader<AccountMatching>,
        accountMatchingStepProcessor: ItemProcessor<AccountMatching, AccountMatching>
    ): Step {
        return step("yandexAccountMatchingStep")
            .chunk<AccountMatching, AccountMatching>(10, transactionManager)
            .reader(yandexAccountMatchingStepReader)
            .processor(accountMatchingStepProcessor)
            .writer { accountMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun yandexAccountMatchingStepReader(): ItemReader<AccountMatching> =
        IteratorItemReader(listOf(AccountMatching(
            bankId = Bank.YANDEX.id,
            bankAccountCode = YandexRecord.ACCOUNT_NAME
        )))

    @Bean
    fun yandexTransactionMatchingStep(
        yandexTransactionMatchingStepReader: ItemReader<YandexRecord>,
        yandexTransactionMatchingStepProcessor: ItemProcessor<YandexRecord, TransactionMatching>
    ): Step {
        return step("yandexTransactionMatchingStep")
            .chunk<YandexRecord, TransactionMatching>(10, transactionManager)
            .reader(yandexTransactionMatchingStepReader)
            .listener(yandexTransactionMatchingStepProcessor)
            .processor(yandexTransactionMatchingStepProcessor)
            .writer { transactionMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun yandexTransactionMatchingStepReader(): ItemReader<YandexRecord> =
        YandexRecordReader(dataSource)

    @Bean
    fun yandexTransactionMatchingStepProcessor(): ItemProcessor<YandexRecord, TransactionMatching> =
        YandexTransactionStepProcessor(
            draftTransactionRepository,
            categoryMatchingRepository,
            transactionMatchingRepository,
            accountMatchingRepository
        )

}