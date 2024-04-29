package ru.tikhonovdo.enrichment.batch.matching.transfer.manual

import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.jdbc.core.BeanPropertyRowMapper
import javax.sql.DataSource

class TransferManualMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<TransferManualMatchingInfo>() {

    init {
        this.dataSource = dataSource
        sql = "SELECT * FROM matching.transfer_manual"
        this.setRowMapper(BeanPropertyRowMapper(TransferManualMatchingInfo::class.java))
    }
}