package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface AccountMatchingRepository:
    JpaRepository<AccountMatching, AccountMatching.AccountMatchingId>,
    CustomAccountMatchingRepository {
    fun findByBankAccountCodeAndBankId(bankAccountCode: String, bankId: Long): AccountMatching
}

interface CustomAccountMatchingRepository: BatchRepository<AccountMatching> {
}

@Repository
class CustomAccountMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate):
    CustomAccountMatchingRepository,
    AbstractBatchRepository<AccountMatching>(
        namedParameterJdbcTemplate,
        "INSERT INTO matching.account (account_id, bank_id, bank_account_code) " +
                "VALUES (:accountId, :bankId, :bankAccountCode)",
    ) {
}