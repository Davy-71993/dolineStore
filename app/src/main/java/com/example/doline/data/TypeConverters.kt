package com.example.doline.data



import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken


class PricingDetailsConverter {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(PricingDetails::class.java, PricingDetailsAdapter())
        .create()

    @TypeConverter
    fun fromPricingDetails(details: PricingDetails?): String? {
        return if (details == null) {
            null
        } else {
            val json = gson.toJson(details)
            json
        }
    }

    @TypeConverter
    fun toPricingDetails(json: String?): PricingDetails? {
        if (json.isNullOrBlank()) {
            return null
        }
        return try {
            gson.fromJson(json, PricingDetails::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class FlashPricingConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromFlashSale(flashPricing: FlashPricing?): String? {
        return flashPricing?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toFlashPricing(json: String?): FlashPricing? {
        if (json.isNullOrBlank()) return null

        return try {
            val type = object : TypeToken<FlashPricing>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
}


class PricingSchemeConverter {
    // PricingScheme
    @TypeConverter
    fun fromScheme(scheme: PricingScheme): String = scheme.name

    @TypeConverter
    fun toScheme(value: String): PricingScheme = PricingScheme.valueOf(value)
}

class StoreCategoryConverter {
    @TypeConverter
    fun fromStoreCategory(category: StoreCategory): String = category.name

    @TypeConverter
    fun toStoreCategory(value: String): StoreCategory = StoreCategory.valueOf(value)
}


class CurrencyConverter {
    @TypeConverter
    fun fromCurrency(currency: Currency): String = currency.name

    @TypeConverter
    fun toCurrency(value: String): Currency = Currency.valueOf(value)
}

class SyncEntityTypeConverter {
    @TypeConverter
    fun fromSyncEntityType(type: SyncEntityType): String = type.name

    @TypeConverter
    fun toSyncEntityType(value: String): SyncEntityType = SyncEntityType.valueOf(value)
}

class SyncOperationConverter {
    @TypeConverter
    fun fromSyncOperation(operation: SyncOperation): String = operation.name

    @TypeConverter
    fun toSyncOperation(value: String): SyncOperation = SyncOperation.valueOf(value)
}

class SpecsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromSpecs(specs: Map<String, Any?>?): String? {
        return specs?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toSpecs(json: String?): Map<String, Any?>? {
        if (json.isNullOrBlank()) return null
        return try {
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
}

class DeterminantsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromDts(specs: Map<String, List<String>>?): String? {
        return specs?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDts(json: String?): Map<String,List<String>>? {
        if (json.isNullOrBlank()) return null
        return try {
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
}

class ItemImageConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromImages(images: List<ItemImage>?): String? {
        return images?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toImages(json: String?): List<ItemImage>? {
        if (json.isNullOrBlank()) return null
        return try {
            val type = object : TypeToken<List<ItemImage>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
}

class CartItemConverters {

    private val gson = Gson()

    // ---------- ItemEntity ----------
    @TypeConverter
    fun fromItemEntity(item: ItemEntity?): String? {
        return item?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toItemEntity(json: String?): ItemEntity? {
        return json?.let {
            gson.fromJson(it, ItemEntity::class.java)
        }
    }

    // ---------- Pricing ----------
    @TypeConverter
    fun fromPricing(pricing: Pricing?): String? {
        return pricing?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toPricing(json: String?): Pricing? {
        return json?.let {
            gson.fromJson(it, Pricing::class.java)
        }
    }

    // ---------- Map<String, Double> (batchesDetails) ----------
    @TypeConverter
    fun fromStringDoubleMap(map: Map<String, Double>?): String? {
        return map?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringDoubleMap(json: String?): Map<String, Double>? {
        if (json.isNullOrBlank()) return emptyMap()
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(json, type)
    }

    // ---------- Map<String, Any>? (specs) ----------
    @TypeConverter
    fun fromStringAnyMap(map: Map<String, Any>?): String? {
        return map?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringAnyMap(json: String?): Map<String, Any>? {
        if (json.isNullOrBlank()) return null
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(json, type)
    }
}