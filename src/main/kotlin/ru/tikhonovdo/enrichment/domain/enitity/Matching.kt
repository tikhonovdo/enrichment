package ru.tikhonovdo.enrichment.domain.enitity

import jakarta.persistence.*
import lombok.NoArgsConstructor
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(schema = "matching", name = "currency")
@IdClass(CurrencyMatching.CurrencyMatchingId::class)
data class CurrencyMatching(
    @Id
    var currencyId: Long? = null,
    @Id
    var bankId: Long,

    var bankCurrencyCode: String
) {
    data class CurrencyMatchingId(
        var currencyId: Long? = null,
        var bankId: Long? = null
    ): java.io.Serializable
}

@Entity
@Table(schema = "matching", name = "account")
@IdClass(AccountMatching.AccountMatchingId::class)
data class AccountMatching(
    @Id
    var accountId: Long? = null,
    @Id
    var bankId: Long,

    var bankAccountCode: String? = null,
 ) {
    data class AccountMatchingId(
        var accountId: Long? = null,
        var bankId: Long? = null
    ): java.io.Serializable

    data class Tinkoff(
        var bankAccountCode: String? = null,
        var bankCurrencyCode: String? = null,
        var pattern: String? = null
    )
}

@Entity
@Table(schema = "matching", name = "category")
@IdClass(CategoryMatching.CategoryMatchingId::class)
data class CategoryMatching(
    @Id
    @Column(name = "category_id", nullable = true)
    var categoryId: Long? = null,
    @Id
    var bankId: Long,

    var bankCategoryName: String? = null,

    var mcc: String?,

    var pattern: String?,

    var sum: Double? = null,

    @ManyToOne(targetEntity = Category::class)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: Category? = null

) {
    data class CategoryMatchingId(
        var categoryId: Long? = null,
        var bankId: Long? = null
    ): java.io.Serializable

}

@Entity
@Table(schema = "matching")
data class DraftTransaction(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "draft_transaction_id_seq")
    @SequenceGenerator(sequenceName = "draft_transaction_id_seq", allocationSize = 1, name = "draft_transaction_id_seq")
    @Id
    var id: Long? = null,

    var bankId: Long,

    var date: LocalDateTime,

    var sum: String,

    var data: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DraftTransaction

        if (id != other.id) return false
        if (bankId != other.bankId) return false
        if (date != other.date) return false
        if (sum != other.sum) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + bankId.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + sum.hashCode()
        return result
    }
}

@Entity
@Table(schema = "matching", name = "transaction")
@NoArgsConstructor
data class TransactionMatching(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_id_seq")
    @SequenceGenerator(schema = "matching", sequenceName = "transaction_id_seq", allocationSize = 1, name = "transaction_id_seq")
    @Id
    var id: Long? = null,

    var draftTransactionId: Long? = null,

    var name: String,

    @Column(name = "type", nullable = false)
    var typeId: Long,

    @Column(name = "category_id")
    var categoryId: Long?,

    var date: LocalDateTime,

    var sum: BigDecimal,

    @Column(name = "account_id")
    var accountId: Long?,

    var description: String = "",

    @Column(name = "event_id")
    var eventId: Long? = null,

    var validated: Boolean = false,

    var refundForId: Long? = null,

    @ManyToOne(targetEntity = Category::class)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: Category? = null,

    @ManyToOne(targetEntity = Account::class)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    var account: Account? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionMatching

        if (draftTransactionId != other.draftTransactionId) return false
        if (name != other.name) return false
        if (typeId != other.typeId) return false
        if (categoryId != other.categoryId) return false
        if (date != other.date) return false
        if (sum != other.sum) return false
        if (accountId != other.accountId) return false
        if (description != other.description) return false
        if (eventId != other.eventId) return false
        if (validated != other.validated) return false

        return true
    }

    override fun hashCode(): Int {
        var result = draftTransactionId?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + typeId.hashCode()
        result = 31 * result + (categoryId?.hashCode() ?: 0)
        result = 31 * result + date.hashCode()
        result = 31 * result + sum.hashCode()
        result = 31 * result + (accountId?.hashCode() ?: 0)
        result = 31 * result + description.hashCode()
        result = 31 * result + (eventId?.hashCode() ?: 0)
        result = 31 * result + validated.hashCode()
        return result
    }
}

@Entity
@Table(schema = "matching", name = "transfer")
class TransferMatching (
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transfer_id_seq")
    @SequenceGenerator(schema = "matching", sequenceName = "transfer_id_seq", allocationSize = 1, name = "transfer_id_seq")
    @Id
    var id: Long? = null,

    var name: String,

    var matchingTransactionIdFrom: Long,

    var matchingTransactionIdTo: Long,

    var validated: Boolean? = false

) {
    fun getTransactionIds() = listOf(matchingTransactionIdFrom, matchingTransactionIdTo)
}