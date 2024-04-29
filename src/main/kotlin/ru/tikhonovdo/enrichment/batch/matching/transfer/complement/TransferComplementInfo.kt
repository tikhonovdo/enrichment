package ru.tikhonovdo.enrichment.batch.matching.transfer.complement

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(schema = "matching", name = "transfer_complement")
open class TransferComplementInfo(
    @Id
    var sourceName: String,
    @Id
    var sourceDescription: String,
    @Id
    var sourceType: Long,
    @Id
    var sourceAccountId: Long,
    @Id
    var targetAccountId: Long
)
