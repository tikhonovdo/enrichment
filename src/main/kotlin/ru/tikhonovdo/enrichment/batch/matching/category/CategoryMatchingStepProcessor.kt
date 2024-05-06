package ru.tikhonovdo.enrichment.batch.matching.category

import org.springframework.batch.item.ItemProcessor
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Pageable
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository

class CategoryMatchingStepProcessor(private val categoryMatchingRepository: CategoryMatchingRepository):
    ItemProcessor<CategoryMatching, CategoryMatching> {

    override fun process(item: CategoryMatching): CategoryMatching? {
        val probe = CategoryMatching(
            bankId = item.bankId,
            bankCategoryName = item.bankCategoryName,
            mcc = item.mcc,
            pattern = item.pattern
        )
        var matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
            .withMatcher("bankId", ExampleMatcher.GenericPropertyMatchers.exact())
            .withMatcher("bankCategoryName", ExampleMatcher.GenericPropertyMatchers.exact())
        item.mcc?.let {
            matcher = matcher
                .withMatcher("mcc", ExampleMatcher.GenericPropertyMatchers.exact())
        }
        item.pattern?.let {
            matcher = matcher
                .withMatcher("pattern", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains())
        }

        val query = categoryMatchingRepository.findAll(Example.of(probe, matcher), Pageable.unpaged())

        return if (query.isEmpty) {
            item
        } else {
            null
        }
    }
}