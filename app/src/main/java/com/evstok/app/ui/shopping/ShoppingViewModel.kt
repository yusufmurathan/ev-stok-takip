package com.evstok.app.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evstok.app.data.Category
import com.evstok.app.data.ItemStatus
import com.evstok.app.data.StockEntity
import com.evstok.app.data.StockRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

data class ShoppingUiState(
    val bitti: List<StockEntity> = emptyList(),
    val az: List<StockEntity> = emptyList()
) {
    val isEmpty: Boolean get() = bitti.isEmpty() && az.isEmpty()
    val total: Int get() = bitti.size + az.size
}

class ShoppingViewModel(private val repository: StockRepository) : ViewModel() {

    private var lastPurchased: StockEntity? = null

    val uiState: StateFlow<ShoppingUiState> = repository.shoppingItems
        .map { items ->
            val collator = Collator.getInstance(Locale("tr"))
            val sorted = items.sortedWith(
                compareBy<StockEntity> { it.category.ordinal }
                    .thenComparator { a, b -> collator.compare(a.name, b.name) }
            )
            ShoppingUiState(
                bitti = sorted.filter { it.status == ItemStatus.BITTI },
                az = sorted.filter { it.status == ItemStatus.AZ }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingUiState())

    fun markPurchased(item: StockEntity) {
        lastPurchased = item
        viewModelScope.launch { repository.setStatus(item.id, ItemStatus.VAR) }
    }

    fun undo() {
        val item = lastPurchased ?: return
        lastPurchased = null
        viewModelScope.launch { repository.setStatus(item.id, item.status) }
    }

    fun markAllPurchased() {
        lastPurchased = null
        viewModelScope.launch { repository.markAllPurchased() }
    }

    fun delete(item: StockEntity) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    fun update(item: StockEntity, name: String, emoji: String, category: com.evstok.app.data.Category, status: ItemStatus) {
        viewModelScope.launch {
            repository.updateItem(
                item.copy(
                    name = name,
                    emoji = emoji.ifBlank { "📦" },
                    category = category,
                    status = status
                )
            )
        }
    }
}
