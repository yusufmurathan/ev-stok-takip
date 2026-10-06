package com.evstok.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evstok.app.data.CatalogItemWithFlag
import com.evstok.app.data.Category
import com.evstok.app.data.CatalogEntity
import com.evstok.app.data.ItemStatus
import com.evstok.app.data.StockRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

sealed interface CatalogRowModel {
    val key: String

    data class Header(val category: Category, val count: Int) : CatalogRowModel {
        override val key: String get() = "h_${category.name}"
    }

    data class Entry(val entry: CatalogItemWithFlag) : CatalogRowModel {
        override val key: String get() = "i_${entry.catalog.id}"
    }
}

data class CatalogUiState(
    val rows: List<CatalogRowModel> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(private val repository: StockRepository) : ViewModel() {

    val search = MutableStateFlow("")
    val categoryFilter = MutableStateFlow<Category?>(null)

    val uiState: StateFlow<CatalogUiState> =
        combine(search, categoryFilter) { query, category -> query to category }
            .flatMapLatest { (query, category) ->
                repository.catalogWithFlags(query, category)
            }
            .map { entries ->
                val collator = Collator.getInstance(Locale("tr"))
                val sorted = entries.sortedWith(
                    compareBy<CatalogItemWithFlag> { it.catalog.category.ordinal }
                        .thenComparator { a, b -> collator.compare(a.catalog.name, b.catalog.name) }
                )
                val byCategory = sorted.groupBy { it.catalog.category }
                val rows = buildList {
                    Category.entries.forEach { current ->
                        byCategory[current]?.let { group ->
                            add(CatalogRowModel.Header(current, group.size))
                            group.forEach { add(CatalogRowModel.Entry(it)) }
                        }
                    }
                }
                CatalogUiState(rows)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    fun setSearch(value: String) {
        search.value = value
    }

    fun setCategory(value: Category?) {
        categoryFilter.value = value
    }

    fun addToStock(catalog: CatalogEntity) {
        viewModelScope.launch { repository.addFromCatalog(catalog) }
    }

    fun addCustom(name: String, emoji: String, category: Category, status: ItemStatus) {
        viewModelScope.launch { repository.addCustom(name, emoji, category, status) }
    }

    fun removeFromCatalog(catalog: CatalogEntity) {
        viewModelScope.launch { repository.deleteCatalogItem(catalog.id) }
    }
}
