package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.batch.matching.transfer.manual.TransferManualMatchingInfo
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface TransferMatchingRepository : JpaRepository<TransferMatching, Long>, BatchRepository<TransferMatching>,
    CustomTransferMatchingRepository {
    fun findByMatchingTransactionIdFrom(matchingTransactionIdFrom: Long): TransferMatching?
}

interface CustomTransferMatchingRepository {
    fun findTransfersToManualMatch(transferInfo: TransferManualMatchingInfo): Collection<TransferMatching>
}

@Repository
class TransferMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate):
    CustomTransferMatchingRepository, AbstractBatchRepository<TransferMatching>(
    namedParameterJdbcTemplate,
    "INSERT INTO matching.transfer (name, matching_transaction_id_from, matching_transaction_id_to) " +
            "VALUES (:name, :matchingTransactionIdFrom, :matchingTransactionIdTo)"
) {
    override fun findTransfersToManualMatch(transferInfo: TransferManualMatchingInfo): Collection<TransferMatching> {
        return namedParameterJdbcTemplate.query("""
            SELECT mt1.id as matching_transaction_id_from, mt2.id as matching_transaction_id_to
            FROM matching.transaction mt1
                JOIN matching.transaction mt2 ON
                    mt1.name like :sourceName AND mt2.name like :targetName AND
                    mt1.type > mt2.type AND mt1.account_id = :sourceAccountId AND mt2.account_id = :targetAccountId AND
                    mt1.category_id IS NULL AND mt2.category_id IS NULL AND
                    mt1.date::DATE = mt2.date::DATE
                JOIN financepm.account a1 ON mt1.account_id = a1.id
                JOIN financepm.account a2 ON mt2.account_id = a2.id
            WHERE
                NOT EXISTS(
                    SELECT 1
                    FROM matching.transfer
                    WHERE matching_transaction_id_from = mt1.id AND matching_transaction_id_to = mt2.id
                ) AND
              CASE WHEN (a1.currency_id = a2.currency_id) THEN (mt1.sum = mt2.sum) ELSE TRUE END
                AND CASE WHEN (length(trim( :sourceDescription )) > 0) THEN (mt1.description = :sourceDescription) ELSE TRUE END
                AND CASE WHEN (length(trim( :targetDescription )) > 0) THEN (mt2.description = :targetDescription) ELSE TRUE END
            ORDER BY mt1.date;
        """.trimIndent(),
            MapSqlParameterSource(mapOf(
                "sourceName" to "%${transferInfo.sourceName}%",
                "sourceDescription" to transferInfo.sourceDescription,
                "sourceAccountId" to transferInfo.sourceAccountId,
                "targetName" to "%${transferInfo.targetName}%",
                "targetDescription" to transferInfo.targetDescription,
                "targetAccountId" to transferInfo.targetAccountId
            ))
        ) { rs, _ ->
            TransferMatching(
                name = "Перевод",
                matchingTransactionIdFrom = rs.getLong("matching_transaction_id_from"),
                matchingTransactionIdTo = rs.getLong("matching_transaction_id_to"),
            )
        }
    }
}
