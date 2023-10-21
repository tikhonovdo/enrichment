package ru.tikhonovdo.enrichment.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import ru.tikhonovdo.enrichment.domain.enitity.*
import ru.tikhonovdo.enrichment.domain.enitity.Currency
import java.util.*

data class FinancePmData(
    val version: Int = 2,
    val transactions: List<Transaction>,
    val transfers: List<Transfer>,
    val accounts: List<Account>,
    val categories: List<Category>,
    val currencies: List<Currency>,
    val arrears: List<Arrear>,
    @JsonProperty("arrear_transaction_relations")
    val arrearTransaction: List<ArrearTransaction>
)
