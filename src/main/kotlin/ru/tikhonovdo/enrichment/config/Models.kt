package ru.tikhonovdo.enrichment.config

open class MappingConfig (
    var accounts: String = "",
    var categories: String = ""
)

open class OutputFileConfig (
    var name: String = "financePM_{date}_enriched.data",
    var datePattern: String = "dd-MM-yyyy"
)