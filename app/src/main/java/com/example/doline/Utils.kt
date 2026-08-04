package com.example.doline

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.window.core.layout.WindowSizeClass
import com.example.doline.data.BatchEntity
import com.example.doline.views.screens.store.inventory.FieldsError
import java.io.File
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun Number.formatWithCommas(): String{
    return NumberFormat
        .getNumberInstance(Locale.US)
        .format(this)
}

fun Number.zeroed(): String{
    return this.toLong().toString().padStart(4, '0')
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toEasyString(): String {
    return this.format(DateTimeFormatter.ofPattern("MMM dd yyyy"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toDateString(): String {
    return this.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.toDateString(): String {
    return this.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.to12HourTimeString(): String {
    return this.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalTime.to12HourTimeString(): String {
    return this.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

fun String.capitalize(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun timestampToDateTime(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0) return "" // Handle empty/unset timestamp defaults safely

    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    return formatter.format(instant)
}

@RequiresApi(Build.VERSION_CODES.O)
fun timestampToDate(timestamp: Long?): String {
    if (timestamp == null){
        return ""
    }
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

@RequiresApi(Build.VERSION_CODES.O)
fun timestampToTime(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

fun getPersistentImageUrl(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "images/${System.currentTimeMillis()}.jpg")
        file.parentFile?.mkdirs()

        file.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        file.toURI().toString()   // or upload to Firebase/Supabase and get real URL
    } catch (e: Exception) {
        e.printStackTrace()
        uri.toString() // fallback
    }
}

enum class DeviceConfiguration {
    MOBILE_PORTRAIT, MOBILE_LANDSCAPE, TABLET_PORTRAIT, TABLET_LANDSCAPE, DESKTOP;

    companion object {
        fun getWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            val width = windowSizeClass.minWidthDp
            val height = windowSizeClass.minHeightDp

            return when {
                // Mobile
                height <  480 && width > height -> MOBILE_LANDSCAPE
                width < 600 -> MOBILE_PORTRAIT

                // Tablet portrait (narrower width)
                width < 840 -> TABLET_PORTRAIT

                // Tablet landscape or larger tablets
                width < 1200 -> TABLET_LANDSCAPE

                else -> DESKTOP
            }
        }

        fun getGridColumnCount(config: DeviceConfiguration): Int {
            return when (config) {
                MOBILE_PORTRAIT -> 1
                MOBILE_LANDSCAPE, TABLET_PORTRAIT -> 2
                TABLET_LANDSCAPE, DESKTOP -> 3
            }
        }

        fun getGridWideColumnCount(config: DeviceConfiguration): Int {
            return when (config) {
                MOBILE_PORTRAIT, MOBILE_LANDSCAPE, TABLET_PORTRAIT -> 1
                TABLET_LANDSCAPE, DESKTOP -> 2
            }
        }
    }
}

fun FieldsError.hasAnyError(): Boolean {
    return name != null ||
            description != null ||
            sku != null ||
            category != null ||
            images != null
}

fun prepareBatchDetails(batches: List<BatchEntity>, qty: Double?, factor: Double = 1.0): Map<String, Double>{
    // If the requested quantity is 0 or less, there's nothing to allocate
    if (qty == null || qty <= 0) return emptyMap()

    val numAvailable = qty * factor
    val result = mutableMapOf<String, Double>()
    var remainingQtyToCover = numAvailable

    // 1. Sort batches by createdAt to ensure we process the oldest first
    val sortedBatches = batches.sortedBy { it.createdAt }

    // 2. Iterate through the sorted batches
    for (batch in sortedBatches) {
        if (remainingQtyToCover <= 0) break
        if (batch.quantity <= 0) continue // Skip empty batches

        // Determine how much to take from this specific batch
        val quantityToTake = minOf(batch.quantity, remainingQtyToCover)

        // Add to our result map (casting the Long to Double as requested)
        result["BAT${batch.id.zeroed()}"] = quantityToTake

        // Deduct what we took from our target goal
        remainingQtyToCover -= quantityToTake
    }
    return result
}




