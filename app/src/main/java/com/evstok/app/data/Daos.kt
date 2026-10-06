package com.evstok.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CatalogItemWithFlag(
    @Embedded val catalog: CatalogEntity,
    val inStock: Boolean
)

@Dao
interface CatalogDao {

    @Query(
        """
        SELECT c.*, EXISTS(SELECT 1 FROM stock s WHERE s.catalogId = c.id) AS inStock
        FROM catalog c
        WHERE (:query = '' OR c.name LIKE '%' || :query || '%')
          AND (:category IS NULL OR c.category = :category)
        ORDER BY c.category, c.name
        """
    )
    fun observeCatalog(query: String, category: String?): Flow<List<CatalogItemWithFlag>>

    @Query("SELECT * FROM catalog WHERE name = :name AND category = :category LIMIT 1")
    suspend fun findByName(name: String, category: String): CatalogEntity?

    @Insert
    suspend fun insertAll(items: List<CatalogEntity>)

    @Insert
    suspend fun insert(item: CatalogEntity): Long

    @Query("DELETE FROM catalog WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM catalog")
    suspend fun count(): Int
}

@Dao
interface StockDao {

    @Query("SELECT * FROM stock ORDER BY category, name")
    fun observeAll(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock WHERE status != 'VAR' ORDER BY category, name")
    fun observeShopping(): Flow<List<StockEntity>>

    @Query("SELECT COUNT(*) FROM stock WHERE status != 'VAR'")
    fun observeShoppingCount(): Flow<Int>

    @Query("SELECT * FROM stock WHERE id = :id")
    suspend fun getById(id: Long): StockEntity?

    @Update
    suspend fun update(item: StockEntity)

    @Query("UPDATE stock SET status = :status, updatedAt = :now WHERE id = :id")
    suspend fun setStatus(id: Long, status: String, now: Long)

    @Insert
    suspend fun insert(item: StockEntity): Long

    @Delete
    suspend fun delete(item: StockEntity)

    @Query("UPDATE stock SET status = 'VAR', updatedAt = :now WHERE status != 'VAR'")
    suspend fun resetAll(now: Long)

    @Query("DELETE FROM stock WHERE catalogId = :catalogId")
    suspend fun deleteByCatalogId(catalogId: Long)
}
