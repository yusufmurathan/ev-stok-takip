@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.evstok.app.ui.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evstok.app.EvStokApp
import com.evstok.app.data.CatalogEntity
import com.evstok.app.data.CatalogItemWithFlag
import com.evstok.app.ui.components.CategoryFilterRow
import com.evstok.app.ui.components.EmptyState
import com.evstok.app.ui.components.ItemEmoji
import com.evstok.app.ui.components.SearchField
import com.evstok.app.ui.dialogs.ConfirmDialog
import com.evstok.app.ui.dialogs.ItemEditDialog
import com.evstok.app.ui.theme.StatusGreen
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val viewModel: CatalogViewModel = viewModel {
        CatalogViewModel((context.applicationContext as EvStokApp).container.repository)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var removeItem by remember { mutableStateOf<CatalogEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    fun addToStock(catalog: CatalogEntity) {
        viewModel.addToStock(catalog)
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "${catalog.name} stoğa eklendi",
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Katalog",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Hazır ürün havuzu · tek dokunuşla stoğa ekle",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            SearchField(value = search, onValueChange = viewModel::setSearch)
            Spacer(Modifier.height(10.dp))
            CategoryFilterRow(selected = categoryFilter, onSelect = viewModel::setCategory)
            Spacer(Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                if (state.rows.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            emoji = "🔎",
                            title = "Sonuç yok",
                            subtitle = "Aramayı değiştir ya da sağ alttaki butonla kendi ürününü ekle."
                        )
                    }
                } else {
                    items(state.rows, key = { it.key }) { row ->
                        when (row) {
                            is CatalogRowModel.Header -> {
                                Text(
                                    text = "${row.category.labelUpper} · ${row.count}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp, bottom = 6.dp)
                                )
                            }
                            is CatalogRowModel.Entry -> {
                                CatalogRow(
                                    entry = row.entry,
                                    onAdd = { addToStock(row.entry.catalog) },
                                    onLongPress = { removeItem = row.entry.catalog }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Özel ürün ekle")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }

    removeItem?.let { catalog ->
        ConfirmDialog(
            title = "Katalogdan kaldır",
            text = "\"${catalog.name}\" katalogdan ve varsa stok kaydından silinecek.",
            confirmLabel = "Kaldır",
            onConfirm = {
                viewModel.removeFromCatalog(catalog)
                removeItem = null
            },
            onDismiss = { removeItem = null }
        )
    }
    if (showAddDialog) {
        ItemEditDialog(
            initial = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, emoji, category, status ->
                viewModel.addCustom(name, emoji, category, status)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CatalogRow(
    entry: CatalogItemWithFlag,
    onAdd: () -> Unit,
    onLongPress: () -> Unit
) {
    val catalog = entry.catalog
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .combinedClickable(
                onClick = { if (!entry.inStock) onAdd() },
                onLongClick = onLongPress
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemEmoji(emoji = catalog.emoji, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = catalog.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (catalog.isCustom) "Özel ürün" else catalog.category.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (entry.inStock) {
            InStockBadge()
        } else {
            FilledTonalIconButton(
                onClick = onAdd,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Stoğa ekle")
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun InStockBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(StatusGreen.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = StatusGreen,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Stokta",
            style = MaterialTheme.typography.labelMedium,
            color = StatusGreen,
            fontWeight = FontWeight.SemiBold
        )
    }
}
