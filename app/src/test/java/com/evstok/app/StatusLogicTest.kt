package com.evstok.app

import com.evstok.app.data.Category
import com.evstok.app.data.ItemStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusLogicTest {

    @Test
    fun statusCycleOrder() {
        assertEquals(ItemStatus.AZ, ItemStatus.VAR.next())
        assertEquals(ItemStatus.BITTI, ItemStatus.AZ.next())
        assertEquals(ItemStatus.VAR, ItemStatus.BITTI.next())
    }

    @Test
    fun categoryFallbackForUnknownName() {
        assertEquals(Category.DIGER, Category.fromName("BILINMEYEN"))
        assertEquals(Category.GIDA, Category.fromName("GIDA"))
    }

    @Test
    fun statusFallbackForUnknownName() {
        assertEquals(ItemStatus.VAR, ItemStatus.fromName("YOK"))
        assertEquals(ItemStatus.BITTI, ItemStatus.fromName("BITTI"))
    }

    @Test
    fun chipLabelsUseTurkishCasing() {
        assertEquals("VAR", ItemStatus.VAR.chipLabel)
        assertEquals("AZ", ItemStatus.AZ.chipLabel)
        assertEquals("BİTTİ", ItemStatus.BITTI.chipLabel)
    }
}
