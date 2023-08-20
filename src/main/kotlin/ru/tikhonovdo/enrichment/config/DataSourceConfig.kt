package ru.tikhonovdo.enrichment.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@Configuration
@Import()
class DataSourceConfig {

    @Primary
    @Bean(name = ["jdbcPrimary"])
    fun primaryJdbcTemplate(dataSource: DataSource) = JdbcTemplate(dataSource)

    @Bean
    @Primary
    fun namedParameterJdbcTemplate(jdbcTemplate: JdbcTemplate): NamedParameterJdbcTemplate? {
        return NamedParameterJdbcTemplate(jdbcTemplate)
    }
}