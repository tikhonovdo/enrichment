package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Category
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository
import ru.tikhonovdo.enrichment.util.getNullable

interface CategoryMatchingRepository:
    JpaRepository<CategoryMatching, CategoryMatching.CategoryMatchingId>,
    CustomCategoryMatchingRepository {
}

interface CustomCategoryMatchingRepository: BatchRepository<CategoryMatching> {
    fun findAllByBankId(bankId: Long): List<CategoryMatching>
}

@Repository
class CustomCategoryMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate):
    CustomCategoryMatchingRepository,
    AbstractBatchRepository<CategoryMatching>(
        namedParameterJdbcTemplate,
        "INSERT INTO matching.category (category_id, bank_id, bank_category_name, mcc, pattern, sum) " +
                "VALUES (:categoryId, :bankId, :bankCategoryName, :mcc, :pattern, :sum)"
    ) {

    override fun findAllByBankId(bankId: Long): List<CategoryMatching> {
        return namedParameterJdbcTemplate.query("""
            SELECT mc.category_id, mc.bank_id, mc.bank_category_name, mc.mcc, mc.pattern, mc.sum, 
            c.name, c.type, c.parent_id, c.order_id
            FROM matching.category mc 
            JOIN financepm.category c ON mc.category_id = c.id
            WHERE mc.bank_id = :bankId;
        """.trimIndent(), MapSqlParameterSource(mapOf("bankId" to bankId))
        ) { rs, _ ->
            CategoryMatching(
                categoryId = rs.getNullable { it.getLong("category_id") },
                bankId = rs.getLong("bank_id"),
                bankCategoryName = rs.getString("bank_category_name"),
                mcc = rs.getNullable { it.getString("mcc") },
                pattern = rs.getNullable { it.getString("pattern") },
                sum = rs.getNullable { it.getDouble("sum") },
                category = Category(
                    id = rs.getLong("category_id"),
                    name = rs.getString("name"),
                    typeId = rs.getLong("type"),
                    orderId = rs.getInt("order_id"),
                    parentId = rs.getNullable { it.getLong("parent_id") }
                )
            )
        }
    }

}