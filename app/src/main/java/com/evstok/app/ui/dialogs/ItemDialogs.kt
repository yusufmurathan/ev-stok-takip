@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.evstok.app.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.evstok.app.data.Category
import com.evstok.app.data.ItemStatus
import com.evstok.app.data.StockEntity
import com.evstok.app.ui.components.ItemEmoji
import com.evstok.app.ui.components.statusColor

@Composable
fun ItemEditDialog(
    initial: StockEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, category: Category, status: ItemStatus) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "📦") }
    var category by remember { mutableStateOf(initial?.category ?: Category.GIDA) }
    var status by remember { mutableStateOf(initial?.status ?: ItemStatus.VAR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = if (initial == null) "Yeni Ürün" else "Ürünü Düzenle",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = limitEmoji(it) },
                        modifier = Modifier.width(72.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Ürün adı") },
                        singleLine = true
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Category.entries.forEach { candidate ->
                            FilterChip(
                                selected = category == candidate,
                                onClick = { category = candidate },
                                label = { Text(candidate.label) }
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Durum",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ItemStatus.entries.forEach { candidate ->
                            FilterChip(
                                selected = status == candidate,
                                onClick = { status = candidate },
                                label = { Text(candidate.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = candidate.statusColor.copy(alpha = 0.16f),
                                    selectedLabelColor = candidate.statusColor
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), emoji.ifBlank { "📦" }, category, status) },
                enabled = name.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

private fun limitEmoji(input: String): String {
    if (input.isEmpty()) return ""
    val builder = StringBuilder()
    var index = 0
    var count = 0
    while (index < input.length && count < 2) {
        val codePoint = input.codePointAt(index)
        builder.appendCodePoint(codePoint)
        index += Character.charCount(codePoint)
        count++
    }
    return builder.toString()
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

@Composable
fun ItemActionSheet(
    item: StockEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ItemEmoji(item.emoji)
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.category.label} · ${item.status.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        SheetAction(label = "Düzenle", icon = Icons.Filled.Create, onClick = onEdit)
        SheetAction(label = "Sil", icon = Icons.Filled.Delete, destructive = true, onClick = onDelete)
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SheetAction(
    label: String,
    icon: ImageVector,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}
