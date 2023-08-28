package ru.tikhonovdo.enrichment.batch.matching

import org.springframework.batch.item.ItemProcessor
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Pageable
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import ru.tikhonovdo.enrichment.repository.matching.CurrencyMatchingRepository

class CurrencyMatchingStepProcessor(private val currencyMatchingRepository: CurrencyMatchingRepository):
    ItemProcessor<CurrencyMatching, CurrencyMatching> {

    override fun process(item: CurrencyMatching): CurrencyMatching? {
        val probe = CurrencyMatching(
            bankId = item.bankId,
            bankCurrencyCode = item.bankCurrencyCode,
        )
        val matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
            .withMatcher("bankId", ExampleMatcher.GenericPropertyMatchers.exact())
            .withMatcher("bankCurrencyCode", ExampleMatcher.GenericPropertyMatchers.exact())
        val query = currencyMatchingRepository.findAll(Example.of(probe, matcher), Pageable.unpaged())

        return if (query.isEmpty) {
            item
        } else {
            null
        }
    }
}