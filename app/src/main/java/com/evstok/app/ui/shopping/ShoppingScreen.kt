@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.evstok.app.ui.shopping

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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evstok.app.EvStokApp
import com.evstok.app.data.StockEntity
import com.evstok.app.ui.components.EmptyState
import com.evstok.app.ui.components.ItemEmoji
import com.evstok.app.ui.components.SectionHeader
import com.evstok.app.ui.components.StatusChip
import com.evstok.app.ui.dialogs.ConfirmDialog
import com.evstok.app.ui.dialogs.ItemActionSheet
import com.evstok.app.ui.dialogs.ItemEditDialog
import com.evstok.app.ui.theme.StatusAmber
import com.evstok.app.ui.theme.StatusRed
import kotlinx.coroutines.launch

@Composable
fun ShoppingScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val viewModel: ShoppingViewModel = viewModel {
        ShoppingViewModel((context.applicationContext as EvStokApp).container.repository)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    var actionSheetItem by remember { mutableStateOf<StockEntity?>(null) }
    var editItem by remember { mutableStateOf<StockEntity?>(null) }
    var deleteItem by remember { mutableStateOf<StockEntity?>(null) }
    var confirmClearAll by remember { mutableStateOf(false) }

    fun purchase(item: StockEntity) {
        viewModel.markPurchased(item)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "${item.name} stoğa eklendi",
                actionLabel = "GERİ AL",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undo()
            }
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
                text = "Market Listesi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Markette işaretledikçe ürünler stoğa döner",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.isEmpty) {
                EmptyState(
                    emoji = "🛒",
                    title = "Liste tertemiz",
                    subtitle = "Bir ürün bittiğinde stoğunda işaretle; alışveriş listen burada kendiliğinden oluşur."
                )
            } else {
                Spacer(Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = { confirmClearAll = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Done, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tümünü Alındı İşaretle (${state.total})")
                }
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (state.bitti.isNotEmpty()) {
                        item(key = "header_bitti") {
                            SectionHeader(text = "BİTTİ", color = StatusRed, count = state.bitti.size)
                        }
                        items(state.bitti, key = { it.id }) { item ->
                            ShoppingRow(
                                item = item,
                                onPurchase = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    purchase(item)
                                },
                                onLongPress = { actionSheetItem = item }
                            )
                        }
                    }
                    if (state.az.isNotEmpty()) {
                        item(key = "header_az") {
                            SectionHeader(text = "AZ KALDI", color = StatusAmber, count = state.az.size)
                        }
                        items(state.az, key = { it.id }) { item ->
                            ShoppingRow(
                                item = item,
                                onPurchase = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    purchase(item)
                                },
                                onLongPress = { actionSheetItem = item }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
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
            text = "\"${item.name}\" stoğundan kaldırılacak.",
            confirmLabel = "Sil",
            onConfirm = {
                viewModel.delete(item)
                deleteItem = null
            },
            onDismiss = { deleteItem = null }
        )
    }
    if (confirmClearAll) {
        ConfirmDialog(
            title = "Listeyi temizle",
            text = "Listedeki ${state.total} ürün alındı olarak işaretlenip tekrar stoğa dönecek.",
            confirmLabel = "Tamam",
            onConfirm = {
                viewModel.markAllPurchased()
                confirmClearAll = false
            },
            onDismiss = { confirmClearAll = false }
        )
    }
}

@Composable
private fun ShoppingRow(
    item: StockEntity,
    onPurchase: () -> Unit,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .combinedClickable(onClick = onPurchase, onLongClick = onLongPress)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = false,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(Modifier.width(4.dp))
        ItemEmoji(emoji = item.emoji, size = 36.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        StatusChip(status = item.status)
    }
    Spacer(Modifier.height(8.dp))
}
