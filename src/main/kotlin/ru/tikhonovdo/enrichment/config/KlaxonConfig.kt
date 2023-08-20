package ru.tikhonovdo.enrichment.config

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tikhonovdo.enrichment.domain.dto.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

@Configuration
class KlaxonConfig {

    @Bean
    fun klaxon() = Klaxon()
        .converter(accountConverter)
        .converter(categoryConverter)
        .converter(transferConverter)
        .converter(transactionConverter)
        .converter(arrearConverter)
        .converter(arrearTransactionConverter)

    private var formatter: NumberFormat = DecimalFormat("0.0#", DecimalFormatSymbols(Locale.ROOT))

    private val accountConverter = object: Converter {
        override fun canConvert(cls: Class<*>)
                = cls == AccountRecord::class.java

        override fun toJson(value: Any): String =
            "{\"id\":\"${(value as AccountRecord).id}\"," +
                "\"name\":\"${value.name.withQuotesEscaping()}\"," +
                "\"icon\":${value.icon}," +
                "\"balance\":\"${formatter.format(value.balance)}\"," +
                "\"active\":${if (value.active) 1 else 0}," +
                "\"isDef\":${if (value.default) 1 else 0}," +
                "\"currencyId\":${value.currencyId}," +
                "\"orderId\":${value.orderId}}"

        override fun fromJson(jv: JsonValue) = AccountRecord(
            jv.objInt("id").toLong(),
            jv.objString("name"),
            jv.objInt("icon"),
            jv.objString("balance").toDouble(),
            jv.objInt("currencyId").toLong(),
            jv.objInt("active") != 0,
            jv.objInt("isDef") != 0,
            jv.objInt("orderId")
        )
    }

    private val categoryConverter = object: Converter {
        override fun canConvert(cls: Class<*>)
                = cls == CategoryRecord::class.java

        override fun toJson(value: Any): String =
            "{\"id\":\"${(value as CategoryRecord).id}\"," +
                    "\"type\":${value.type}," +
                    "\"name\":\"${value.name.withQuotesEscaping()}\"," +
                    "\"available\":${if (value.available) 1 else 0}," +
                    "\"orderId\":${value.orderId}," +
                    "\"parentId\":${value.parentId}}"

        override fun fromJson(jv: JsonValue) = CategoryRecord(
            jv.objInt("id").toLong(),
            jv.objString("name"),
            jv.objInt("type").toLong(),
            jv.objInt("parentId").toLong(),
            jv.objInt("orderId"),
            jv.objInt("available") != 0
        )
    }

    private val transactionConverter = object: Converter {
        override fun canConvert(cls: Class<*>)
                = cls == TransactionRecord::class.java

        override fun toJson(value: Any): String =
            "{\"id\":\"${(value as TransactionRecord).id}\"," +
                    "\"name\":\"${value.name.withQuotesEscaping()}\"," +
                    "\"type\":${value.type}," +
                    "\"categoryId\":${value.categoryId}," +
                    "\"date\":\"${value.date.time}\"," +
                    "\"sum\":\"${formatter.format(value.sum)}\"," +
                    "\"accountId\":${value.accountId}," +
                    "\"description\":\"${value.description.withQuotesEscaping()}\"," +
                    "\"source\":${value.sourceId}," +
                    "\"available\":${if (value.available) 1 else 0}}"

        override fun fromJson(jv: JsonValue) = TransactionRecord(
            jv.objString("id").toLong(),
            jv.objString("name"),
            jv.objInt("type").toLong(),
            jv.objInt("categoryId").toLong(),
            Date(jv.objString("date").toLong()),
            jv.objString("sum").toDouble(),
            jv.objInt("accountId").toLong(),
            jv.objString("description"),
            jv.objInt("source").toLong(),
            jv.objInt("available") != 0
        )
    }

    private val transferConverter = object: Converter {
        override fun canConvert(cls: Class<*>)
                = cls == TransferRecord::class.java

        override fun toJson(value: Any): String =
            "{\"id\":\"${(value as TransferRecord).id}\"," +
                    "\"name\":\"${value.name.withQuotesEscaping()}\"," +
                    "\"transactionIdFrom\":\"${value.transactionIdFrom}\"," +
                    "\"transactionIdTo\":\"${value.transactionIdTo}\"," +
                    "\"available\":${if (value.available) 1 else 0}}"

        override fun fromJson(jv: JsonValue) = TransferRecord(
            jv.objString("id").toLong(),
            jv.objString("name"),
            jv.objString("transactionIdFrom").toLong(),
            jv.objString("transactionIdTo").toLong(),
            jv.objInt("available") != 0
        )
    }

    private val arrearConverter = object: Converter {
        override fun canConvert(cls: Class<*>)
                = cls == ArrearRecord::class.java

        override fun toJson(value: Any): String =
            "{\"id\":\"${(value as ArrearRecord).id}\"," +
                    "\"name\":\"${value.name.withQuotesEscaping()}\"," +
                    "\"balance\":\"${formatter.format(value.balance)}\"," +
                    "\"accountId\":${value.accountId}," +
                    "\"date\":\"${value.date.time}\"," +
                    "\"description\":\"${value.description.withQuotesEscaping()}\"," +
                    "\"available\":${if (value.available) 1 else 0}}"

        override fun fromJson(jv: JsonValue) = ArrearRecord(
            jv.objString("id").toLong(),
            jv.objString("name"),
            Date(jv.objString("date").toLong()),
            jv.objString("balance").toDouble(),
            jv.objInt("accountId").toLong(),
            jv.objString("description"),
            jv.objInt("available") != 0
        )
    }

    private val arrearTransactionConverter = object: Converter {
        override fun canConvert(cls: Class<*>)
                = cls == ArrearTransactionRecord::class.java

        override fun toJson(value: Any): String =
            "{\"id\":\"${(value as ArrearTransactionRecord).id}\"," +
                    "\"transactionId\":\"${value.transactionId}\"," +
                    "\"arrearId\":\"${value.arrearId}\"}"

        override fun fromJson(jv: JsonValue) = ArrearTransactionRecord(
            jv.objString("id").toLong(),
            jv.objString("transactionId").toLong(),
            jv.objString("arrearId").toLong()
        )
    }

    private fun String.withQuotesEscaping(): String = this.replace("\"", "\\\"")
}