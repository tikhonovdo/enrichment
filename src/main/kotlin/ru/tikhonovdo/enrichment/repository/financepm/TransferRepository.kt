package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Transfer
import java.util.function.Function

interface TransferRepository : JpaRepository<Transfer, Long>, FinancePmRepository<Transfer>

@Repository
class TransferRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractFinancePmRepository<Transfer>(
    namedParameterJdbcTemplate,
    "financepm.transfer",
    Function { it.id },
    "INSERT INTO financepm.transfer (id, name, transaction_id_from, transaction_id_to, available) " +
            "VALUES (:id, :name, :transactionIdFrom, :transactionIdTo, :available)"
)
