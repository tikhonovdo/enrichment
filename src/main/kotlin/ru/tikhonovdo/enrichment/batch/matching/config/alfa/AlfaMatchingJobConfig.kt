package ru.tikhonovdo.enrichment.batch.matching.config.alfa

import org.springframework.batch.core.Step
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.common.CustomFlowBuilder
import ru.tikhonovdo.enrichment.batch.matching.account.AlfaAccountMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.category.AlfaCategoryMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.config.BaseMatchingJobConfig
import ru.tikhonovdo.enrichment.batch.matching.currency.AlfaCurrencyMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.transaction.alfa.AlfaRecordReader
import ru.tikhonovdo.enrichment.batch.matching.transaction.alfa.AlfaTransactionStepProcessor
import ru.tikhonovdo.enrichment.domain.dto.transaction.AlfaRecord
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
class AlfaMatchingJobConfig(
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
    fun alfaMatchingFlow(
        alfaCategoryMatchingStep: Step,
        alfaCurrencyMatchingStep: Step,
        alfaAccountMatchingStep: Step,
        alfaTransactionMatchingStep: Step,
    ): Flow = CustomFlowBuilder("alfaMatchingFlow")
        .addStep(alfaCategoryMatchingStep)
        .addStep(alfaCurrencyMatchingStep)
        .addStep(alfaAccountMatchingStep)
        .addStep(alfaTransactionMatchingStep)
        .build()

    @Bean
    fun alfaCategoryMatchingStep(
        alfaCategoryMatchingStepReader: ItemReader<CategoryMatching>,
        categoryMatchingStepProcessor: ItemProcessor<CategoryMatching, CategoryMatching>
    ): Step {
        return step("alfaCategoryMatchingStep")
            .chunk<CategoryMatching, CategoryMatching>(10, transactionManager)
            .reader(alfaCategoryMatchingStepReader)
            .processor(categoryMatchingStepProcessor)
            .writer { categoryMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun alfaCategoryMatchingStepReader(): ItemReader<CategoryMatching> =
        AlfaCategoryMatchingStepReader(dataSource)
    
    @Bean
    fun alfaCurrencyMatchingStep(
        alfaCurrencyMatchingStepReader: ItemReader<CurrencyMatching>,
        currencyMatchingStepProcessor: ItemProcessor<CurrencyMatching, CurrencyMatching>
    ): Step {
        return step("alfaCurrencyMatchingStep")
            .chunk<CurrencyMatching, CurrencyMatching>(10, transactionManager)
            .reader(alfaCurrencyMatchingStepReader)
            .processor(currencyMatchingStepProcessor)
            .writer { currencyMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun alfaCurrencyMatchingStepReader(): ItemReader<CurrencyMatching> =
        AlfaCurrencyMatchingStepReader(dataSource)

    @Bean
    fun alfaAccountMatchingStep(
        alfaAccountMatchingStepReader: ItemReader<AccountMatching>,
        accountMatchingStepProcessor: ItemProcessor<AccountMatching, AccountMatching>
    ): Step {
        return step("alfaAccountMatchingStep")
            .chunk<AccountMatching, AccountMatching>(10, transactionManager)
            .reader(alfaAccountMatchingStepReader)
            .processor(accountMatchingStepProcessor)
            .writer { accountMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun alfaAccountMatchingStepReader(): ItemReader<AccountMatching> =
        AlfaAccountMatchingStepReader(dataSource)

    @Bean
    fun alfaTransactionMatchingStep(
        alfaTransactionMatchingStepReader: ItemReader<AlfaRecord>,
        alfaTransactionMatchingStepProcessor: ItemProcessor<AlfaRecord, TransactionMatching>
    ): Step {
        return step("alfaTransactionMatchingStep")
            .chunk<AlfaRecord, TransactionMatching>(10, transactionManager)
            .reader(alfaTransactionMatchingStepReader)
            .listener(alfaTransactionMatchingStepProcessor)
            .processor(alfaTransactionMatchingStepProcessor)
            .writer { transactionMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun alfaTransactionMatchingStepReader(): ItemReader<AlfaRecord> =
        AlfaRecordReader(dataSource)

    @Bean
    fun alfaTransactionMatchingStepProcessor(): ItemProcessor<AlfaRecord, TransactionMatching> =
        AlfaTransactionStepProcessor(
            draftTransactionRepository,
            categoryMatchingRepository,
            transactionMatchingRepository,
            accountMatchingRepository
        )


}