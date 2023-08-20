package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Category
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface CategoryRepository : JpaRepository<Category, Long>, BatchRepository<Category> {
    @Query("SELECT setval('category_id_seq', (SELECT MAX(id) FROM category))", nativeQuery = true)
    fun resetSequence()

    @Modifying(flushAutomatically = true)
    @Query("TRUNCATE TABLE category CASCADE;", nativeQuery = true)
    fun truncate()

    fun findByName(name: String): Category?
}

@Repository
class CategoryRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractBatchRepository<Category>(
    namedParameterJdbcTemplate,
    "INSERT INTO category (id, name, type, parent_id, order_id, available) VALUES (:id, :name, :typeId, :parentId, :orderId, :available)"
) {

}