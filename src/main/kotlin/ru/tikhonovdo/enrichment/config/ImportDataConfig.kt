package ru.tikhonovdo.enrichment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ImportDataConfig {

    @Bean(name = ["alfaProperties"])
    @ConfigurationProperties(prefix = "import.alfa")
    fun alfaProperties(): ImportDataProperties = ImportDataProperties()

    @Bean(name = ["tinkoffProperties"])
    @ConfigurationProperties(prefix = "import.tinkoff")
    fun tinkoffProperties(): ImportDataProperties = ImportDataProperties()

    @Bean(name = ["yandexProperties"])
    @ConfigurationProperties(prefix = "import.yandex")
    fun yandexProperties(): ImportDataProperties = ImportDataProperties()
}

class ImportDataProperties {
    lateinit var startUrl: String
    lateinit var apiUrl: String
}