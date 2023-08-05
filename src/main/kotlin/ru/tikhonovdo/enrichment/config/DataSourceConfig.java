package ru.tikhonovdo.enrichment.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

//    private final static String PRIMARY_DS = "primaryDataSource";
    private final static String PRIMARY_PROPERTIES = "primaryProperties";

    @Primary
    @Bean(PRIMARY_PROPERTIES)
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }
}