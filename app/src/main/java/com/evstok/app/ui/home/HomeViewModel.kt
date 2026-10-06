package com.evstok.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evstok.app.data.Category
import com.evstok.app.data.ItemStatus
import com.evstok.app.data.StockEntity
import com.evstok.app.data.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

sealed interface HomeListRow {
    val key: String

    data class Header(val category: Category) : HomeListRow {
        override val key: String get() = "h_${category.name}"
    }

    data class Item(val item: StockEntity) : HomeListRow {
        override val key: String get() = "i_${item.id}"
    }
}

data class HomeUiState(
    val rows: List<HomeListRow> = emptyList(),
    val totalCount: Int = 0,
    val varCount: Int = 0,
    val azCount: Int = 0,
    val bittiCount: Int = 0,
    val stockEmpty: Boolean = true
)

class HomeViewModel(private val repository: StockRepository) : ViewModel() {

    val search = MutableStateFlow("")
    val categoryFilter = MutableStateFlow<Category?>(null)
    val statusFilter = MutableStateFlow<ItemStatus?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.stockItems,
        search,
        categoryFilter,
        statusFilter
    ) { items, query, category, status ->
        val collator = Collator.getInstance(Locale("tr"))
        val queryTrimmed = query.trim()
        val filtered = items.asSequence()
            .filter { category == null || it.category == category }
            .filter { status == null || it.status == status }
            .filter { queryTrimmed.isEmpty() || it.name.contains(queryTrimmed, ignoreCase = true) }
            .sortedWith(
                compareBy<StockEntity> { it.category.ordinal }
                    .thenComparator { a, b -> collator.compare(a.name, b.name) }
            )
            .toList()
        val byCategory = filtered.groupBy { it.category }
        val rows = buildList {
            Category.entries.forEach { current ->
                byCategory[current]?.let { group ->
                    add(HomeListRow.Header(current))
                    group.forEach { add(HomeListRow.Item(it)) }
                }
            }
        }
        HomeUiState(
            rows = rows,
            totalCount = items.size,
            varCount = items.count { it.status == ItemStatus.VAR },
            azCount = items.count { it.status == ItemStatus.AZ },
            bittiCount = items.count { it.status == ItemStatus.BITTI },
            stockEmpty = items.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setSearch(value: String) {
        search.value = value
    }

    fun setCategory(value: Category?) {
        categoryFilter.value = value
    }

    fun setStatusFilter(value: ItemStatus?) {
        statusFilter.value = value
    }

    fun cycleStatus(id: Long) {
        viewModelScope.launch { repository.cycleStatus(id) }
    }

    fun delete(item: StockEntity) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    fun update(item: StockEntity, name: String, emoji: String, category: Category, status: ItemStatus) {
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

    fun addCustom(name: String, emoji: String, category: Category, status: ItemStatus) {
        viewModelScope.launch { repository.addCustom(name, emoji, category, status) }
    }
}
