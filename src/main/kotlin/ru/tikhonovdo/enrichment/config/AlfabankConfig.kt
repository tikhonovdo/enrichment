package ru.tikhonovdo.enrichment.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.service.importscenario.sixMonthsAgo
import java.util.function.Predicate
import java.util.function.Supplier

@Configuration
class AlfabankConfig {

    @Bean
    fun alfaFileWorkerResultFilter(transactionMatchingRepository: TransactionMatchingRepository): Supplier<Predicate<DraftTransaction>> =
        Supplier { Predicate {
            val lastValidateDate = transactionMatchingRepository.findLastValidatedTransactionDateByBank(Bank.ALFA.id)
                .orElse(sixMonthsAgo()).toLocalDate().atStartOfDay()

            return@Predicate it.date >= lastValidateDate
        } }
}