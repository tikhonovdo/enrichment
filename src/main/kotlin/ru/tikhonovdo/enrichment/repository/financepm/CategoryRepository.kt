package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Category
import java.util.function.Function

interface CategoryRepository : JpaRepository<Category, Long>, FinancePmRepository<Category> {
    fun findByName(name: String): Category?
}

@Repository
class CategoryRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractFinancePmRepository<Category>(
    namedParameterJdbcTemplate,
    "financepm.category",
    Function { it.id },
    "INSERT INTO financepm.category (id, name, type, parent_id, order_id, available) VALUES (:id, :name, :typeId, :parentId, :orderId, :available)"
)
