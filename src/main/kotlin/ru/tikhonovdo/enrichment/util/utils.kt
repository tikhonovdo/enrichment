package ru.tikhonovdo.enrichment.util

import ru.tikhonovdo.enrichment.financepm.IdRecord
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

const val datePattern = "dd.MM.yyyy"

fun initLastId(collection: List<IdRecord>) =
    if (collection.size > 1) {
        collection.map { it.id }.sortedBy { it }.last()!!
    } else {
        1
    }

// financePM использует для дат время в UTC
fun LocalDate.toDate() =
    Date(this.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli())
