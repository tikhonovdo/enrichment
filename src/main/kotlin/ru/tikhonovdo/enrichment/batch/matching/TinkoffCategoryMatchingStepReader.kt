package ru.tikhonovdo.enrichment.batch.matching

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import javax.sql.DataSource

class TinkoffCategoryMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<CategoryMatching>() {
    init {
        this.dataSource = dataSource
        sql = """
            WITH t AS (
                SELECT DISTINCT ON (items.item->>'category', items.item->>'mcc')
                    items.item->>'category' as bank_category_name,
                    items.item->>'mcc' as mcc,
                    items.item->>'description' as description
                FROM
                     (SELECT jsonb_array_elements(data) item
                      FROM draft_transaction
                      WHERE bank_id = ${Bank.TINKOFF.id}) items)
                SELECT * FROM t WHERE t.bank_category_name IS NOT NULL;
        """.trimIndent()
        setRowMapper { rs, _ ->
            CategoryMatching(
                bankId = Bank.TINKOFF.id,
                bankCategoryName = rs.getString("bank_category_name"),
                mcc = rs.getInt("mcc").toString(),
                pattern = rs.getString("description"),
                categoryId = null
            )
        }
    }
}