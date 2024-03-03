package ru.tikhonovdo.enrichment.batch.matching.category

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.util.getNullable
import javax.sql.DataSource

class AlfaCategoryMatchingStepReader(dataSource: DataSource): CategoryMatchingStepReader(dataSource, Bank.ALFA)
class TinkoffCategoryMatchingStepReader(dataSource: DataSource): CategoryMatchingStepReader(dataSource, Bank.TINKOFF)

abstract class CategoryMatchingStepReader(dataSource: DataSource, bank: Bank): JdbcCursorItemReader<CategoryMatching>() {
    init {
        this.dataSource = dataSource
        sql = """
            WITH t AS (
                SELECT DISTINCT ON (data->>'category', data->>'mcc', data->>'description')
                    data->>'category' as bank_category_name,
                    data->>'mcc' as mcc,
                    data->>'description' as description
                FROM matching.draft_transaction dt
                --     LEFT JOIN matching.category mc ON (
                --        dt.data->>'category' = mc.bank_category_name AND 
                --        dt.bank_id = mc.bank_id AND
                --        dt.data->>'description' like ('%' || mc.pattern || '%')
                --     )
                WHERE dt.bank_id = ${bank.id} AND lower(dt.data->>'category') not like '%наличн%'
            ) SELECT * FROM t WHERE t.bank_category_name IS NOT NULL;
        """.trimIndent()
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