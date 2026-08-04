package com.example.doline.data

data class FlashPricing(
    val startTime: Long,
    val endTime: Long,
    val flashPrice: Double,
    val discountPercentage: Double? = null
)

data class ItemImage(
    val url: String,
    val sortOrder: Int = 0
)

sealed class PricingDetails {

    // Empty for Fixed Price
    data class Fixed(val scheme: String =  "Fixed") : PricingDetails()

    data class UnitPrice(
        val scheme: String =  "Unit",
        val units: String? = null,
        val conversionFactor: Double? = null,
        val conversionRatios: List<Map<String, Double>> = emptyList()
    ) : PricingDetails()

    data class PriceRange(
        val scheme: String =  "Range",
        val specs: Map<String, String>? = null,
        val qty: Double
    ) : PricingDetails()

    data class RecurringPrice(
        val scheme: String? =  "Recurring",
        val period: String
    ) : PricingDetails()

    data class PriceMenu(
        val scheme: String =  "Menu",
        val image: String? = null,
        val title: String,
        val description: String? = null,
        val qty: Int? = null
    ) : PricingDetails()
}

data class UPCResponse(
    val code: String,
    val total: Int,
    val items: List<UPCItem>?
)

data class UPCItem(
    val title: String?,
    val description: String?,
    val brand: String?,
    val category: String?,
    val images: List<String>?,
    val price: String?,
    val currency: String?
)

data class SelectOption<T>(
    val label: String,
    val value: T
)

data class PricingDraft(
    val itemId: Long,
    val amount: Double? = null,
    val currency: Currency = SELECTED_CURRENCY,
    val details: PricingDetails? = null,
)

data class BatchDraft(
    val itemId: Long,
    val units: String? = null,
    val quantity: Double? = null,
    val available: Double? = null,
    val expiryDate: Long? = null,
    val manufactureDate: Long? = null,
    val batchNumber: String? = null,
    val buyingPrice: Double? = null
)

data class PriceRangeDraft(
    val specs: Map<String, String>? = null,
    val qty: Double? = null
)

enum class Periods{
    HOUR, DAY, WEEK, MONTH, QUARTER, YEAR
}
