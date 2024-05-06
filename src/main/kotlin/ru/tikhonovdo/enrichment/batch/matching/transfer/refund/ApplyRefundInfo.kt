package ru.tikhonovdo.enrichment.batch.matching.transfer.refund

import java.math.BigDecimal

data class ApplyRefundInfo(
    val sum: BigDecimal,
    val refundForId: Long,
    val sourceId: Long
)
