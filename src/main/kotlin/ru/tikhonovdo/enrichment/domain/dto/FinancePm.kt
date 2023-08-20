package ru.tikhonovdo.enrichment.domain.dto

import com.beust.klaxon.Json
import com.fasterxml.jackson.annotation.JsonProperty
import ru.tikhonovdo.enrichment.domain.enitity.*
import ru.tikhonovdo.enrichment.domain.enitity.Currency
import java.util.*

interface IdRecord {
    val id: Long?
}

data class AccountRecord(
    override val id: Long?,
    val name: String,
    val icon: Int?,
    val balance: Double,
    val currencyId: Long?,
    val active : Boolean,
    val default: Boolean,
    val orderId: Int?
): IdRecord

data class CategoryRecord(
    override val id: Long?,
    val name: String,
    val type: Long?,
    val parentId: Long?,
    val orderId: Int?,
    val available: Boolean
): IdRecord

data class CurrencyRecord(
    override val id: Long?,
    val name: String,
    val shortName: String,
    val point: Int?,
    val available: Int?
): IdRecord

data class TransactionRecord(
    override val id: Long?,
    var name: String,
    val type: Long?,
    var categoryId: Long?,
    val date: Date,
    val sum: Double,
    val accountId: Long?,
    var description: String,
    val sourceId: Long? = 0,
    val available: Boolean = true,
    val new: Boolean = true
): IdRecord {
    override fun equals(other: Any?): Boolean {
        if (other !is TransactionRecord) {
            return false
        }

        return date == other.date && sum == other.sum;
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + sum.hashCode()
        return result
    }
}

data class TransferRecord(
    override val id: Long?,
    val name: String,
    val transactionIdFrom: Long?,
    val transactionIdTo: Long?,
    val available: Boolean = true
): IdRecord

data class ArrearRecord(
    override val id: Long?,
    val name: String,
    val date: Date,
    val balance: Double,
    val accountId: Long?,
    val description: String,
    val available: Boolean = true
): IdRecord

data class ArrearTransactionRecord(
    override val id: Long?,
    val arrearId: Long?,
    val transactionId: Long?
): IdRecord

data class FinancePmData(
    val version: Int = 2,
    val transactions: MutableList<Transaction>,
    val transfers: MutableList<Transfer>,
    val accounts: MutableList<Account>,
    val categories: MutableList<Category>,
    val currencies: MutableList<Currency>,
    val arrears: MutableList<Arrear>,
    @Json(name = "arrear_transaction_relations")
    @JsonProperty("arrear_transaction_relations")
    val arrearTransaction: MutableList<ArrearTransaction>
) {
    constructor() : this(-1, mutableListOf(),
        mutableListOf(), mutableListOf(), mutableListOf(),
        mutableListOf(), mutableListOf(), mutableListOf())

    val isEmpty: Boolean
        get() = this.version == -1
}
