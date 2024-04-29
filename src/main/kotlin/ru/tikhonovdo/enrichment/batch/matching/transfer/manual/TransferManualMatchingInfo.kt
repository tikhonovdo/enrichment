package ru.tikhonovdo.enrichment.batch.matching.transfer.manual

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(schema = "matching", name = "transfer_manual")
open class TransferManualMatchingInfo(
    @Id
    var sourceName: String,
    @Id
    var sourceDescription: String,
    @Id
    var sourceAccountId: Long,
    @Id
    var targetName: String,
    @Id
    var targetDescription: String,
    @Id
    var targetAccountId: Long
)
