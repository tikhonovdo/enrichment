package ru.tikhonovdo.enrichment.config

import org.apache.commons.csv.CSVFormat
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BaseConfig {

    @Bean
    fun csvFormat(): CSVFormat = CSVFormat.DEFAULT.builder()
        .setIgnoreHeaderCase(true)
        .setHeader().setSkipHeaderRecord(true)
        .setDelimiter(';')
        .build()

    @Bean
    @ConfigurationProperties(prefix = "mapping")
    fun mappingConfig() = MappingConfig()

    @Bean
    @ConfigurationProperties(prefix = "output-file")
    fun outputFileConfig() = OutputFileConfig()

}