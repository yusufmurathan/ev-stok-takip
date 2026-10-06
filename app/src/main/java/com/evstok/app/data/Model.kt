package com.evstok.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Category(val label: String) {
    GIDA("Gıda"),
    ICECEK("İçecek"),
    TEMIZLIK("Temizlik"),
    HIJIYEN("Hijyen"),
    DIGER("Diğer");

    val labelUpper: String
        get() = when (this) {
            GIDA -> "GIDA"
            ICECEK -> "İÇECEK"
            TEMIZLIK -> "TEMİZLİK"
            HIJIYEN -> "HİJYEN"
            DIGER -> "DİĞER"
        }

    companion object {
        fun fromName(value: String): Category = entries.firstOrNull { it.name == value } ?: DIGER
    }
}

enum class ItemStatus(val label: String) {
    VAR("Var"),
    AZ("Az"),
    BITTI("Bitti");

    val chipLabel: String
        get() = when (this) {
            VAR -> "VAR"
            AZ -> "AZ"
            BITTI -> "BİTTİ"
        }

    fun next(): ItemStatus = when (this) {
        VAR -> AZ
        AZ -> BITTI
        BITTI -> VAR
    }

    companion object {
        fun fromName(value: String): ItemStatus = entries.firstOrNull { it.name == value } ?: VAR
    }
}

@Entity(
    tableName = "catalog",
    indices = [Index(value = ["name", "category"], unique = true)]
)
data class CatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: Category,
    val emoji: String,
    val isCustom: Boolean = false
)

@Entity(
    tableName = "stock",
    indices = [Index(value = ["catalogId"], unique = true)]
)
data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogId: Long? = null,
    val name: String,
    val category: Category,
    val emoji: String,
    val status: ItemStatus = ItemStatus.VAR,
    val addedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
