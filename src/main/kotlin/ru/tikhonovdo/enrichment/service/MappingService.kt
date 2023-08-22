package ru.tikhonovdo.enrichment.service

import org.springframework.stereotype.Service
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import ru.tikhonovdo.enrichment.domain.dto.TinkoffRecord
import java.math.BigDecimal
import kotlin.math.absoluteValue
import kotlin.math.sign

@Service
class MappingService {

    companion object {
        private const val EPS: Double = 0.005 // todo: переделать на использование точности данного счета (account)
    }

    fun toTransferTransaction(record: TinkoffRecord) = toTransaction(record, true)
    fun toTransaction(record: TinkoffRecord, transfer: Boolean = false): Transaction {
        return Transaction(
            name = record.description,
            typeId = if (record.paymentSum.sign > 0) 1L else 2L,
            categoryId = null, //if (transfer) 0 else categoryMapper.getFinancePmId(record),
            date = record.operationDate,
            sum = if (record.paymentSum.absoluteValue < EPS) BigDecimal.ZERO else record.paymentSum.toBigDecimal(),
            accountId = 1L,// accountMapper.getFinancePmId(record),
            description = "",
            eventId = null, // if (transfer) 1 else 0,
            bankId = 1L
        )
    }

}