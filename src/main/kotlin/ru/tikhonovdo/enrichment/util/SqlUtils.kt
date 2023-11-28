package ru.tikhonovdo.enrichment.util

import java.sql.ResultSet

fun <T: Any?> ResultSet.getNullable(resultExtractor: java.util.function.Function<ResultSet, T>): T? {
    return resultExtractor.apply(this).apply { if (wasNull()) return null }
}