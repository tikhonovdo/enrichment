package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface CurrencyMatchingRepository:
    JpaRepository<CurrencyMatching, CurrencyMatching.CurrencyMatchingId>,
    CustomCurrencyMatchingRepository

interface CustomCurrencyMatchingRepository: BatchRepository<CurrencyMatching>

@Repository
class CustomCurrencyMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate):
    CustomCurrencyMatchingRepository,
    AbstractBatchRepository<CurrencyMatching> (
        namedParameterJdbcTemplate,
        "INSERT INTO matching.currency (currency_id, bank_id, bank_currency_code) VALUES (:currencyId, :bankId, :bankCurrencyCode)",
    )