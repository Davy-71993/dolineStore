package com.example.doline.data

data class UnitPriceFormError(
    val price: String? = null,
    val units: String? = null,
    val conversionFactor: String? = null,
    val general: String? = null,
){
    fun hasErrors(): Boolean {
        return !this.units.isNullOrEmpty()
                || !this.price.isNullOrEmpty()
                || !this.conversionFactor.isNullOrEmpty()
                || !this.general.isNullOrEmpty()
    }
}

data class RecurringPriceFormError(
    val price: String? = null,
    val period: String? = null,
    val general: String? = null,
){
    fun hasErrors(): Boolean {
        return !this.period.isNullOrEmpty()
                || !this.price.isNullOrEmpty()
                || !this.general.isNullOrEmpty()
    }
}

data class RestockFormError(
    val qty: String? = null,
    val units: String? = null,
    val prices: String? = null,
    val general: String? = null,
){
    fun hasErrors(): Boolean {
        return !this.units.isNullOrEmpty()
                || !this.qty.isNullOrEmpty()
                || !this.units.isNullOrEmpty()
                || !this.prices.isNullOrEmpty()
                || !this.general.isNullOrEmpty()
    }
}

data class PriceRangeFormError(
    val qty: String? = null,
    val specs: String? = null,
    val price: String? = null,
    val general: String? = null,
){
    fun hasErrors(): Boolean {
        return !this.specs.isNullOrEmpty()
                || !this.qty.isNullOrEmpty()
                || !this.qty.isNullOrEmpty()
                || !this.price.isNullOrEmpty()
                || !this.general.isNullOrEmpty()
    }
}