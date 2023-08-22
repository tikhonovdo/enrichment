package ru.tikhonovdo.enrichment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching


@Repository
interface AccountMatchingRepository : JpaRepository<AccountMatching, AccountMatching.AccountMatchingId> {
}

@Repository
interface CategoryMatchingRepository : JpaRepository<CategoryMatching, CategoryMatching.CategoryMatchingId> {
}

@Repository
interface CurrencyMatchingRepository : JpaRepository<CurrencyMatching, CurrencyMatching.CurrencyMatchingId> {
}
