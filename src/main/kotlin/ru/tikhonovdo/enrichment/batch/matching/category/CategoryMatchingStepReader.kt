package ru.tikhonovdo.enrichment.batch.matching.category

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.util.getNullable
import javax.sql.DataSource

class CategoryMatchingStepReader(dataSource: DataSource, bank: Bank, sql: String = defaultSql(bank)): JdbcCursorItemReader<CategoryMatching>() {
    init {
        this.dataSource = dataSource
        this.sql = sql
        setRowMapper { rs, _ ->
            CategoryMatching(
                bankId = bank.id,
                bankCategoryName = rs.getString("bank_category_name"),
                mcc = rs.getNullable { it.getInt("mcc") }?.toString(),
                pattern = rs.getNullable { it.getString("description") },
                categoryId = null
            )
        }
    }
}

private fun defaultSql(bank: Bank) =
    """
        WITH t AS (
            SELECT DISTINCT ON (data->>'category', data->>'mcc', data->>'description')
                data->>'category' as bank_category_name,
                data->>'mcc' as mcc,
                data->>'description' as description
            FROM matching.draft_transaction dt
            WHERE dt.bank_id = ${bank.id}
        ) SELECT * FROM t WHERE t.bank_category_name IS NOT NULL;
    """.trimIndent()