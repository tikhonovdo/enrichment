package ru.tikhonovdo.enrichment.financepm

import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun MutableList<out IdRecord>.getNextId() =
    if (size > 1) map { it.id }.sortedBy { it }.last()!! else 1

// financePM использует для дат время в UTC
fun LocalDate.toDate() =
    Date(this.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli())
