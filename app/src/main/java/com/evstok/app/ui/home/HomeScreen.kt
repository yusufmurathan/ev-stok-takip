@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.evstok.app.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evstok.app.EvStokApp
import com.evstok.app.data.StockEntity
import com.evstok.app.ui.components.CategoryFilterRow
import com.evstok.app.ui.components.EmptyState
import com.evstok.app.ui.components.ItemEmoji
import com.evstok.app.ui.components.SearchField
import com.evstok.app.ui.components.StatPill
import com.evstok.app.ui.components.StatusChip
import com.evstok.app.ui.components.StatusFilterRow
import com.evstok.app.ui.dialogs.ConfirmDialog
import com.evstok.app.ui.dialogs.ItemActionSheet
import com.evstok.app.ui.dialogs.ItemEditDialog
import com.evstok.app.ui.theme.StatusAmber
import com.evstok.app.ui.theme.StatusGreen
import com.evstok.app.ui.theme.StatusRed

@Composable
fun HomeScreen(
    padding: PaddingValues,
    onNavigateToCatalog: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel {
        HomeViewModel((context.applicationContext as EvStokApp).container.repository)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    var actionSheetItem by remember { mutableStateOf<StockEntity?>(null) }
    var editItem by remember { mutableStateOf<StockEntity?>(null) }
    var deleteItem by remember { mutableStateOf<StockEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ev Stoğu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (state.totalCount == 0) {
                            "Henüz ürün yok"
                        } else {
                            "${state.totalCount} ürün · dokununca durum değişir"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatPill(color = StatusGreen, label = "Var", count = state.varCount)
                    StatPill(color = StatusAmber, label = "Az", count = state.azCount)
                    StatPill(color = StatusRed, label = "Bitti", count = state.bittiCount)
                }
            }
            Spacer(Modifier.height(14.dp))
            SearchField(value = search, onValueChange = viewModel::setSearch)
            Spacer(Modifier.height(10.dp))
            CategoryFilterRow(selected = categoryFilter, onSelect = viewModel::setCategory)
            Spacer(Modifier.height(6.dp))
            StatusFilterRow(selected = statusFilter, onSelect = viewModel::setStatusFilter)
            Spacer(Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                if (state.rows.isEmpty()) {
                    item(key = "empty") {
                        if (state.stockEmpty) {
                            EmptyState(
                                emoji = "🏠",
                                title = "Stoğun boş",
                                subtitle = "Katalogdaki hazır ürünlerden tek dokunuşla ekle ya da kendi ürününü tanımla."
                            ) {
                                FilledTonalButton(onClick = onNavigateToCatalog) {
                                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Kataloğa Göz At")
                                }
                            }
                        } else {
                            EmptyState(
                                emoji = "🔍",
                                title = "Sonuç yok",
                                subtitle = "Filtreleri değiştirerek tekrar dene."
                            )
                        }
                    }
                } else {
                    items(state.rows, key = { it.key }) { row ->
                        when (row) {
                            is HomeListRow.Header -> {
                                Text(
                                    text = row.category.labelUpper,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp, bottom = 6.dp)
                                )
                            }
                            is HomeListRow.Item -> {
                                StockRow(
                                    item = row.item,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.cycleStatus(row.item.id)
                                    },
                                    onLongClick = { actionSheetItem = row.item }
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
            Icon(Icons.Filled.Add, contentDescription = "Ürün ekle")
        }
    }

    actionSheetItem?.let { item ->
        ItemActionSheet(
            item = item,
            onEdit = {
                editItem = item
                actionSheetItem = null
            },
            onDelete = {
                deleteItem = item
                actionSheetItem = null
            },
            onDismiss = { actionSheetItem = null }
        )
    }
    editItem?.let { item ->
        ItemEditDialog(
            initial = item,
            onDismiss = { editItem = null },
            onSave = { name, emoji, category, status ->
                viewModel.update(item, name, emoji, category, status)
                editItem = null
            }
        )
    }
    deleteItem?.let { item ->
        ConfirmDialog(
            title = "Ürünü sil",
            text = "\"${item.name}\" stoğundan kaldırılacak. İstersen katalogdan tekrar ekleyebilirsin.",
            confirmLabel = "Sil",
            onConfirm = {
                viewModel.delete(item)
                deleteItem = null
            },
            onDismiss = { deleteItem = null }
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
private fun StockRow(
    item: StockEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemEmoji(emoji = item.emoji)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.category.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        StatusChip(status = item.status)
    }
    Spacer(Modifier.height(8.dp))
}
