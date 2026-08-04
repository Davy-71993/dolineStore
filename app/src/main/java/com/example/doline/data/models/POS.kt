package com.example.doline.data.models

data class ReceiptPrinter (
    val name: String,
    val isActive: Boolean = true,
)

data class CashDrawer (
    val name: String,
    val isActive: Boolean = true,
    val isOpen: Boolean = false,
    val autoOpenOnSale: Boolean = true
)

data class WeighingScale (
    val name: String,
    val isActive: Boolean = true,
)

data class BarcodeScanner (
    val name: String,
    val isActive: Boolean = true,
)