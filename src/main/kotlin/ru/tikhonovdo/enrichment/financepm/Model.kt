package ru.tikhonovdo.enrichment.financepm

import com.beust.klaxon.Json
import java.util.*

interface IdRecord {
    val id: Int?
}

data class AccountRecord(
    override val id: Int?,
    val name: String,
    val icon: Int?,
    val balance: Double,
    val currencyId: Int?,
    val active : Boolean,
    val default: Boolean,
    val orderId: Int?
): IdRecord

data class CategoryRecord(
    override val id: Int?,
    val name: String,
    val type: Int?,
    val parentId: Int?,
    val orderId: Int?,
    val available: Boolean
): IdRecord

data class CurrencyRecord(
    override val id: Int?,
    val name: String,
    val shortName: String,
    val point: Int?,
    val available: Int?
): IdRecord

data class TransactionRecord(
    override val id: Int?,
    var name: String,
    val type: Int?,
    var categoryId: Int?,
    val date: Date,
    val sum: Double,
    val accountId: Int?,
    var description: String,
    val sourceId: Int? = 0,
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
    override val id: Int?,
    val name: String,
    val transactionIdFrom: Int?,
    val transactionIdTo: Int?,
    val available: Boolean = true
): IdRecord

data class ArrearRecord(
    override val id: Int?,
    val name: String,
    val balance: Double,
    val accountId: Int?,
    val date: Date,
    val description: String,
    val available: Boolean = true
): IdRecord

data class ArrearTransactionRecord(
    override val id: Int?,
    val arrearId: Int?,
    val transactionId: Int?
): IdRecord

data class FinancePmData(
    val version: Int = 2,
    val transactions: MutableList<TransactionRecord>,
    val transfers: MutableList<TransferRecord>,
    val accounts: MutableList<AccountRecord>,
    val categories: MutableList<CategoryRecord>,
    val currencies: MutableList<CurrencyRecord>,
    val arrears: MutableList<ArrearRecord>,
    @Json(name = "arrear_transaction_relations")
    val arrearTransactionRelations: MutableList<ArrearTransactionRecord>
) {
    constructor() : this(-1, mutableListOf(),
        mutableListOf(), mutableListOf(), mutableListOf(),
        mutableListOf(), mutableListOf(), mutableListOf())

    val isEmpty: Boolean
        get() = this.version == -1
}
