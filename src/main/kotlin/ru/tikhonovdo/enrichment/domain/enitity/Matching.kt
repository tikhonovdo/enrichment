package ru.tikhonovdo.enrichment.domain.enitity

import jakarta.persistence.*

@Entity
@IdClass(CurrencyMatching.CurrencyMatchingId::class)
data class CurrencyMatching(
    @Id
    var currencyId: Long?,
    @Id
    var bankId: Long,

    var bankCurrencyCode: String
) {
    fun isValid(): Boolean = currencyId != null

    data class CurrencyMatchingId(
        var currencyId: Long? = null,
        var bankId: Long? = null
    ): java.io.Serializable
}

@Entity
@IdClass(AccountMatching.AccountMatchingId::class)
data class AccountMatching(
    @Id
    var accountId: Long?,
    @Id
    var bankId: Long,

    var bankAccountCode: String
) {
    fun isValid(): Boolean = accountId != null

    data class AccountMatchingId(
        var accountId: Long? = null,
        var bankId: Long? = null
    ): java.io.Serializable

}

@Entity
@IdClass(CategoryMatching.CategoryMatchingId::class)
data class CategoryMatching(
    @Id
    var categoryId: Long?,
    @Id
    var bankId: Long,

    var bankCategoryName: String,

    var mcc: String,

    var pattern: String
) {
    fun isValid(): Boolean = categoryId != null

    data class CategoryMatchingId(
        var categoryId: Long? = null,
        var bankId: Long? = null
    ): java.io.Serializable

}

@Entity
@Table(name = "draft_transaction")
class DraftTransaction(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "draft_transaction_id_seq")
    @SequenceGenerator(sequenceName = "draft_transaction_id_seq", allocationSize = 1, name = "draft_transaction_id_seq")
    @Id
    var id: Long? = null,

    var bankId: Long,

    var filename: String,

    @Lob
    var data: ByteArray
)