package ru.tikhonovdo.enrichment.domain.financepm

import org.springframework.boot.ApplicationArguments
import ru.tikhonovdo.enrichment.domain.dto.IdRecord
import java.nio.file.Paths

fun MutableList<out IdRecord>.getNextId() =
    if (size > 1) map { it.id }.sortedBy { it }.last()!! else 1

fun ApplicationArguments.getDataFilePath() =
    nonOptionArgs[0]?.let { Paths.get(it) } ?:
    throw IllegalStateException("Source path for FinancePm data file is null")
