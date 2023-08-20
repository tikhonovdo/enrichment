package ru.tikhonovdo.enrichment.domain.enitity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.persistence.*
import lombok.NoArgsConstructor
import ru.tikhonovdo.enrichment.util.MillisToLocalDateTimeDeserializer
import ru.tikhonovdo.enrichment.util.LocalDateTimeToMillisSerializer
import ru.tikhonovdo.enrichment.util.ZeroAsNullDeserializer
import ru.tikhonovdo.enrichment.util.NullAsZeroSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@NoArgsConstructor
class Type(@Id var id: Long, var name: String)

@Entity
@NoArgsConstructor
class Event(@Id var id: Long, var name: String)

@Entity
@NoArgsConstructor
data class Currency(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_id_seq")
    @SequenceGenerator(sequenceName = "currency_id_seq", allocationSize = 1, name = "currency_id_seq")
    @Id
    var id: Long? = null,

    var name: String,

    @JsonProperty("shortName")
    var shortName: String,

    var point: Int = 2,

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    var available: Boolean = true
)

@Entity
@NoArgsConstructor
data class Account(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_id_seq")
    @SequenceGenerator(sequenceName = "account_id_seq", allocationSize = 1, name = "account_id_seq")
    @Id
    var id: Long? = null,

    var name: String,

    var icon: Int = 1,

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var balance: BigDecimal,

    var currencyId: Long,

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    var active : Boolean,

    @JsonProperty("isDef")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Column(name = "is_default")
    var default: Boolean = false,

    var orderId: Int
)

@Entity
@JsonIgnoreProperties(value = ["operationType", "parentCategory"])
@NoArgsConstructor
data class Category(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_id_seq")
    @SequenceGenerator(sequenceName = "category_id_seq", allocationSize = 1, name = "category_id_seq")
    @Id
    var id: Long? = null,

    var name: String,

    @Column(name = "type", nullable = false)
    @JsonProperty("type")
    var typeId: Long,

    @Column(name = "parent_id", nullable = true)
    @JsonSerialize(using = NullAsZeroSerializer::class)
    @JsonDeserialize(using = ZeroAsNullDeserializer::class)
    var parentId: Long?,

    var orderId: Int,

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    var available: Boolean = true,

    @ManyToOne(targetEntity = Type::class)
    @JoinColumn(name = "type", nullable = false, insertable = false, updatable = false)
    var operationType: Type? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true, insertable = false, updatable = false)
    var parentCategory: Category? = null
)

@Entity
@JsonIgnoreProperties(value = ["event", "bankId", "operationType", "category", "account"])
@NoArgsConstructor
data class Transaction(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_id_seq")
    @SequenceGenerator(sequenceName = "transaction_id_seq", allocationSize = 1, name = "transaction_id_seq")
    @Id
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("id")
    var id: Long? = null,

    @JsonProperty("name")
    var name: String,

    @Column(name = "type", nullable = false)
    @JsonProperty("type")
    var typeId: Long,

    @Column(name = "category_id")
    @JsonSerialize(using = NullAsZeroSerializer::class)
    @JsonDeserialize(using = ZeroAsNullDeserializer::class)
    @JsonProperty("categoryId")
    var categoryId: Long?,

    @JsonSerialize(using = LocalDateTimeToMillisSerializer::class)
    @JsonDeserialize(using = MillisToLocalDateTimeDeserializer::class)
    @JsonProperty("date")
    var date: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("sum")
    var sum: BigDecimal,

    @Column(name = "account_id")
    @JsonProperty("accountId")
    var accountId: Long,

    @JsonProperty("description")
    var description: String,

    @JsonSerialize(using = NullAsZeroSerializer::class)
    @JsonDeserialize(using = ZeroAsNullDeserializer::class)
    @Column(name = "event_id")
    @JsonProperty("source")
    var eventId: Long?,

    @ManyToOne(targetEntity = Event::class)
    @JoinColumn(name = "event_id", nullable = true, insertable = false, updatable = false)
    var event: Event? = null,

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonProperty("available")
    var available: Boolean = true,

    var bankId: Long? = null,

    @ManyToOne(targetEntity = Type::class)
    @JoinColumn(name = "type", nullable = false, insertable = false, updatable = false)
    var operationType: Type? = null,

    @ManyToOne(targetEntity = Category::class)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: Category? = null,

    @ManyToOne(targetEntity = Account::class)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    var account: Account? = null
)

@Entity
@JsonIgnoreProperties(value = ["transactionFrom", "transactionTo", "validated"])
data class Transfer(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transfer_id_seq")
    @SequenceGenerator(sequenceName = "transfer_id_seq", allocationSize = 1, name = "transfer_id_seq")
    @Id
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var id: Long? = null,

    var name: String,

    @Column(name = "transaction_id_from")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var transactionIdFrom: Long,

    @Column(name = "transaction_id_to")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var transactionIdTo: Long,

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    var available: Boolean = true,

    var validated: Boolean = false,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id_from", insertable = false, updatable = false)
    var transactionFrom: Transaction? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id_to", insertable = false, updatable = false)
    var transactionTo: Transaction? = null
)

@Entity
data class Arrear(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "arrear_id_seq")
    @SequenceGenerator(sequenceName = "arrear_id_seq", allocationSize = 1, name = "arrear_id_seq")
    @Id
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var id: Long? = null,

    var name: String,

    @JsonSerialize(using = LocalDateTimeToMillisSerializer::class)
    @JsonDeserialize(using = MillisToLocalDateTimeDeserializer::class)
    var date: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var balance: BigDecimal,

    @Column(name = "account_id")
    var accountId: Long,

    var description: String,

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    var available: Boolean = true,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = true, insertable = false, updatable = false)
    var account: Account? = null
)

@Entity
data class ArrearTransaction(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "arrear_transaction_id_seq")
    @SequenceGenerator(sequenceName = "arrear_transaction_id_seq", allocationSize = 1, name = "arrear_transaction_id_seq")
    @Id
    var id: Long? = null,

    var arrearId: Long,

    var transactionId: Long
)
