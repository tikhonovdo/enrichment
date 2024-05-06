package ru.tikhonovdo.enrichment.batch.matching.transfer.complement

import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.jdbc.core.BeanPropertyRowMapper
import javax.sql.DataSource

class TransferComplementStepReader(dataSource: DataSource): JdbcCursorItemReader<TransferComplementInfo>() {

    init {
        this.dataSource = dataSource
        sql = "SELECT * FROM matching.transfer_complement"
        this.setRowMapper(BeanPropertyRowMapper(TransferComplementInfo::class.java))
    }
}