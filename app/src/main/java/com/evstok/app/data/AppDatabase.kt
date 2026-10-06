package com.evstok.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun categoryToString(value: Category): String = value.name

    @TypeConverter
    fun stringToCategory(value: String): Category = Category.fromName(value)

    @TypeConverter
    fun statusToString(value: ItemStatus): String = value.name

    @TypeConverter
    fun stringToStatus(value: String): ItemStatus = ItemStatus.fromName(value)
}

@Database(
    entities = [CatalogEntity::class, StockEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao
    abstract fun stockDao(): StockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "evstok.db"
                ).build().also { INSTANCE = it }
            }
    }
}
