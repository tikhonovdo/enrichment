package ru.tikhonovdo.enrichment.old.mapping

import org.apache.commons.csv.CSVFormat
import ru.tikhonovdo.enrichment.domain.dto.TinkoffRecord

/**
 * Содержит сопоставление идентифекаторов кошельков FinancePM с TinkoffRecord
 * @see TinkoffRecord
 */
class AccountMapper(fileName: String, csvFormat: CSVFormat) : AbstractFinancePmIdMapper(fileName, csvFormat) {
    private val cardNumberToAccountIdMap = mutableMapOf<String, Int>()

    init {
        for (record in csv) {
            cardNumberToAccountIdMap[record.get("cardNumber")] = record.get("financePmAccountId").toInt()
        }
    }

    override fun getFinancePmId(tinkoffRecord: TinkoffRecord): Int? {
        return cardNumberToAccountIdMap[tinkoffRecord.cardNumber]
    }
}