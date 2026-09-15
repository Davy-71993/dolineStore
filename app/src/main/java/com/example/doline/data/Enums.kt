package com.example.doline.data

enum class PricingScheme {
    FIXED, UNIT, RANGE, RECURRING, MENU
}

enum class Currency {
    UGX, KES, USD, TSH
}

enum class ExpenseCategory{
    INVENTORY, UTILITIES, LOGISTICS, STAFF, OTHERS, MARKETING;

    companion object {
        private val enums: Array<ExpenseCategory> = entries.toTypedArray()

        fun getExpenseCategories(): List<ExpenseCategory> = enums.toList()
    }
}

enum class StoreCategory {
    DOLINE_STORE, DOLINE_FOODS, DOLINE_REAL_ESTATES, DOLINE_RIDE, DOLINE_MOTORS;

    companion object {
        private val enums: Array<StoreCategory> = StoreCategory.entries.toTypedArray()

        fun getStoreCategories(): List<StoreCategory> = enums.toList()
    }
}

enum class OrderProgress {
    PENDING, READY, SHIPPING, COMPLETED, RETURNED, DRAFT, CANCELLED
}

enum class OrderStatus {
    PAID, CREDIT, DRAFT
}

enum class SyncEntityType {
    PROFILE, STORE
}

enum class SyncOperation {
    INSERT, UPDATE, DELETE
}