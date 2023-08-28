package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface AccountMatchingRepository:
    JpaRepository<AccountMatching, AccountMatching.AccountMatchingId>,
    CustomAccountMatchingRepository

interface CustomAccountMatchingRepository: BatchRepository<AccountMatching>

@Repository
class CustomAccountMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate):
    CustomAccountMatchingRepository,
    AbstractBatchRepository<AccountMatching>(
        namedParameterJdbcTemplate,
        "INSERT INTO account_matching (account_id, bank_id, bank_account_code, bank_currency_code, pattern) " +
                "VALUES (:accountId, :bankId, :bankAccountCode, :bankCurrencyCode, :pattern)",
    )