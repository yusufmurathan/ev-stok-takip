package com.evstok.app.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StockRepository(context: Context, scope: CoroutineScope) {

    private val database = AppDatabase.get(context)
    private val catalogDao = database.catalogDao()
    private val stockDao = database.stockDao()

    val stockItems: Flow<List<StockEntity>> = stockDao.observeAll()
    val shoppingItems: Flow<List<StockEntity>> = stockDao.observeShopping()
    val shoppingCount: Flow<Int> = stockDao.observeShoppingCount()

    init {
        scope.launch(Dispatchers.IO) { seedIfNeeded(context) }
    }

    private suspend fun seedIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        if (catalogDao.count() == 0) {
            catalogDao.insertAll(SeedCatalog.items)
        }
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    fun catalogWithFlags(query: String, category: Category?): Flow<List<CatalogItemWithFlag>> =
        catalogDao.observeCatalog(query.trim(), category?.name)

    suspend fun cycleStatus(id: Long) {
        val item = stockDao.getById(id) ?: return
        stockDao.setStatus(id, item.status.next().name, System.currentTimeMillis())
    }

    suspend fun setStatus(id: Long, status: ItemStatus) {
        stockDao.setStatus(id, status.name, System.currentTimeMillis())
    }

    suspend fun addFromCatalog(catalog: CatalogEntity, status: ItemStatus = ItemStatus.VAR): Boolean =
        try {
            stockDao.insert(
                StockEntity(
                    catalogId = catalog.id,
                    name = catalog.name,
                    category = catalog.category,
                    emoji = catalog.emoji,
                    status = status
                )
            )
            true
        } catch (e: Exception) {
            false
        }

    suspend fun addCustom(name: String, emoji: String, category: Category, status: ItemStatus): Boolean {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return false
        val catalogId = catalogDao.findByName(cleanName, category.name)?.id
            ?: catalogDao.insert(
                CatalogEntity(name = cleanName, category = category, emoji = emoji, isCustom = true)
            )
        return try {
            stockDao.insert(
                StockEntity(
                    catalogId = catalogId,
                    name = cleanName,
                    category = category,
                    emoji = emoji,
                    status = status
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateItem(item: StockEntity) {
        stockDao.update(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteItem(item: StockEntity) {
        stockDao.delete(item)
    }

    suspend fun deleteCatalogItem(id: Long) {
        stockDao.deleteByCatalogId(id)
        catalogDao.deleteById(id)
    }

    suspend fun markAllPurchased() {
        stockDao.resetAll(System.currentTimeMillis())
    }

    companion object {
        private const val PREFS_NAME = "evstok"
        private const val KEY_SEEDED = "seeded_v1"
    }
}
