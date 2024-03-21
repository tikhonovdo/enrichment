package ru.tikhonovdo.enrichment.batch.matching.transfer.pattern

data class PatternTransferMatchingInfo(
    val sourceName: String,
    val sourceDescription: String,
    val sourceType: Long,
    val sourceAccountId: Long,
    val targetAccountId: Long
)
