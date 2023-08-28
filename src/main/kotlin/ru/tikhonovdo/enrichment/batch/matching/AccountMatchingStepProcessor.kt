package ru.tikhonovdo.enrichment.batch.matching

import org.springframework.batch.item.ItemProcessor
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Pageable
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository

class AccountMatchingStepProcessor(private val accountMatchingRepository: AccountMatchingRepository):
    ItemProcessor<AccountMatching, AccountMatching> {

    override fun process(item: AccountMatching): AccountMatching? {
        val probe = AccountMatching(
            bankId = item.bankId,
            bankAccountCode = item.bankAccountCode
        )
        val matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
            .withMatcher("bankId", ExampleMatcher.GenericPropertyMatchers.exact())
            .withMatcher("bankAccountCode", ExampleMatcher.GenericPropertyMatchers.exact())
        val query = accountMatchingRepository.findAll(Example.of(probe, matcher), Pageable.unpaged())

        return if (query.isEmpty) {
            item
        } else {
            null
        }
    }
}