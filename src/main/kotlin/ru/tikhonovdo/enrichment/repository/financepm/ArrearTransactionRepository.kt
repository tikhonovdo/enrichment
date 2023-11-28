package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.ArrearTransaction
import java.util.function.Function

interface ArrearTransactionRepository : JpaRepository<ArrearTransaction, Long>, FinancePmRepository<ArrearTransaction>

@Repository
class ArrearTransactionRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractFinancePmRepository<ArrearTransaction>(
    namedParameterJdbcTemplate,
    "financepm.arrear_transaction",
    Function { it.id },
    "INSERT INTO financepm.arrear_transaction (id, arrear_id, transaction_id) VALUES (:id, :arrearId, :transactionId)",
)
