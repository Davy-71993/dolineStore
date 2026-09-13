package com.example.doline.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.doline.R
import java.time.LocalTime


data class SettingType (
    val name: String,
    val icon: Int,
    val description: String,
    val route: String
)

data class  NotificationType(
    val type: String,
    val enabled: Boolean
)

data class PaymentMethod(
    val name: String,
    val abbr: String
)

data class Currency(
    val name: String,
    val abbr: String,
    val symbol: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
data class WorkingDay (
    val day: String,
    val status: DayStatus = DayStatus.OPEN,
    val open24Hours: Boolean = true,
    val open: LocalTime = LocalTime.of(0, 0),
    val close: LocalTime = LocalTime.of(0, 0)
)

data class DeliveryZone(
    val distance: Float,
    val price: Float,
    val distanceUnits: String = "KM",
    val deliveryTime: Int,
    val timeUnits: String = "Mins"
)

enum class DayStatus {
    OPEN, CLOSED
}

val settings = listOf(
    SettingType(
        "Working hours",
        R.drawable.clock,
        description = "Setup the working hours of your store",
        route = "general"
    ),
    SettingType(
        "Notifications",
        R.drawable.bell,
        description = "Manage alerts and notifications",
        route = "notifications"
    ),
    SettingType(
        "Inventory",
        R.drawable.inventory,
        description = "Inventory management options",
        route = "inventory"
    ),
    SettingType(
        "Reports & Analytics",
        R.drawable.reports,
        description = "Reporting and email schedules",
        route = "reports_&_analytics"
    ),
    SettingType(
        "Payments & Tenders",
        R.drawable.credit_card,
        description = "Payment methods and tender settings",
        route = "payments_&_tenders"
    ),
    SettingType(
        "Shipping & Deliveries",
        R.drawable.logistics,
        description = "Delivery options and pricing",
        route = "shipping_&_deliveries"
    ),
    SettingType(
        "Taxation",
        R.drawable.calculator,
        description = "Tax and compliance settings",
        route = "taxation"
    ),
    SettingType(
        "StaffEntity & Security",
        R.drawable.lock,
        description = "StaffEntity access and security settings",
        route = "staff_&_security"
    ),
    SettingType(
        "POS Hardware",
        R.drawable.pos,
        description = "Manage POS hardware devices",
        route = "pos_hardware"
    )
)

@RequiresApi(Build.VERSION_CODES.O)
val workingDays = mutableListOf(
    WorkingDay("Monday"),
    WorkingDay("Tuesday"),
    WorkingDay("Wednesday"),
    WorkingDay("Thursday"),
    WorkingDay("Friday"),
    WorkingDay("Saturday", open24Hours = false),
    WorkingDay("Sunday", status = DayStatus.CLOSED)
)

val notificationTypes  = listOf(
    NotificationType(
        "New orders", true
    ),
    NotificationType(
        "Low inventory", true
    ),
    NotificationType(
        "Customer review", false
    ),
    NotificationType(
        "Promotional updated", true
    ),
    NotificationType(
        "Lilli suggestions", false
    )
)

val paymentMethods = mutableListOf(
    PaymentMethod(
        "Cash",
        "cash"
    ),
    PaymentMethod(
        "Mobile Money",
        "mm"
    ),
    PaymentMethod(
        "Card",
        "card"
    )
)

val currencies = mutableListOf(
    Currency(
        "Uganda Shillings",
        "UGX"
    ),
    Currency(
        "US Dollars",
        "USD",
        "$"
    ),
    Currency(
        "Kenya Shillings",
        "KES"
    ),
    Currency(
        "Tanzania Shillings",
        "TZS"
    )
)
