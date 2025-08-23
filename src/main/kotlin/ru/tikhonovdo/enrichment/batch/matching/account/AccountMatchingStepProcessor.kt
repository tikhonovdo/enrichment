package ru.tikhonovdo.enrichment.batch.matching.account

import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository

/**
 * Процессор занимается отсеиванием существующих аккаунтов из входных данных
 * с целью поддержания уникальности данных в БД.
 */
class AccountMatchingStepProcessor(
    private val accountMatchingRepository: AccountMatchingRepository
): ItemProcessor<AccountMatching, AccountMatching>, StepExecutionListener {

    private var accountMatchingCandidates = listOf<AccountMatching>()

    override fun beforeStep(stepExecution: StepExecution) {
        accountMatchingCandidates = accountMatchingRepository.findAll()
            .map { AccountMatching(
                bankId = it.bankId,
                bankAccountCode = it.bankAccountCode
            ) }
    }

    override fun process(item: AccountMatching): AccountMatching? {
        val probe = AccountMatching(
            bankId = item.bankId,
            bankAccountCode = item.bankAccountCode
        )

        return if (accountMatchingCandidates.contains(probe)) {
            null
        } else {
            item
        }
    }
}