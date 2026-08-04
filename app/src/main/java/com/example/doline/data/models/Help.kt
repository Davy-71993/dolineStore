package com.example.doline.data.models

import com.example.doline.R

data class Faq(
    val qn: String,
    val ans: String,
    val category: String
)

data class FaqCategory(
    val name: String,
    val description: String,
    val icon: Int
)

val faqCategories = listOf(
    FaqCategory(
        "inventory",
        "Learn how to track SKU levels, manage stock alerts, and integrate with your warehouse management system directly through the dashboard.",
        icon = R.drawable.inventory
    ),
    FaqCategory(
        "analytics",
        "Deep dive into our predictive analytics models, seasonality adjustments, and how to export regional performance reports in PDF or CSV formats.",
        R.drawable.reports
    ),
    FaqCategory(
        "billing & account",
        "Update payment methods, view past invoices, or modify your subscription tier for additional retail outlets.",
        R.drawable.credit_card
    ),
    FaqCategory(
        "hardware integration",
        "Step-by-step guides for connecting IoT sensors, shelf cameras, and POS systems to the analytics engine.",
        R.drawable.hardware
    ),
    FaqCategory(
        "product pricing",
        "Understand the vast pricing options available for you products.",
        R.drawable.pricing
    ),
    FaqCategory(
        "delivery & refunds",
        "Learn how to set delivery zones, set refund policies and track your deliveries.",
        R.drawable.logistics
    )
)

val faqs = listOf(
    // Inventory
    Faq(
        "How do I add a new item to my inventory?",
        "Go to stock tab, then tap the plus floating button on the bottom left corner of your screen. Fill the new item form and save.",
        "inventory"
    ),
    Faq(
        "How can I set low stock alerts?",
        "Select any product in your inventory list, click on 'Edit', scroll down to the 'Stock Alerts' section, set your minimum threshold value, and save. You will receive push notifications when stock falls below this number.",
        "inventory"
    ),
    Faq(
        "Can I bulk import inventory via CSV?",
        "Yes, from the Inventory tab, tap the three dots in the top right corner and select 'Import CSV'. Download the template, populate it, and upload it back.",
        "inventory"
    ),

    // Understanding Analytics
    Faq(
        "How is the daily sales forecast calculated?",
        "Our predictive analytics model uses a combination of historical sales data, day-of-the-week trends, and seasonality adjustments to predict your sales for the next 7 to 30 days.",
        "analytics"
    ),
    Faq(
        "How do I export regional performance reports?",
        "Go to the Analytics tab, select the date range, tap 'Export' in the top right, and choose between PDF or CSV formats. The report will be saved to your device and can be shared directly.",
        "analytics"
    ),

    // Billing & Account
    Faq(
        "How do I change my subscription tier?",
        "Navigate to Settings > Billing & Account. Tap on 'Manage Subscription' to upgrade, downgrade, or update payment methods. Changes are prorated automatically.",
        "billing & account"
    ),
    Faq(
        "Where can I find my past invoices?",
        "In the Billing & Account section under Settings, scroll to 'Invoices' to view, download, or email invoices for any past billing cycles.",
        "billing & account"
    ),

    // Hardware Integration
    Faq(
        "How do I connect a new IoT shelf sensor?",
        "Ensure your device is turned on and in pairing mode. Go to Settings > Hardware Integration, tap 'Add New Device', select 'IoT Sensor', and follow the on-screen Bluetooth pairing guide.",
        "hardware integration"
    ),
    Faq(
        "Is my existing POS system compatible?",
        "We support direct API integration with most major POS systems. Go to Settings > Hardware Integration > API Keys to link your POS or contact our support team for a guided setup.",
        "hardware integration"
    ),

    // Product Pricing
    Faq(
        "How do I configure dynamic pricing?",
        "Open a product's details page, toggle on 'Dynamic Pricing', and define your rules (e.g., clearance pricing for near-expiry stock or peak-hour demand markup).",
        "product pricing"
    ),
    Faq(
        "Can I set custom promotional pricing?",
        "Yes. Go to the Product Details screen, click 'Add Promotion', specify the promotional price, and select the start and end dates for the discount.",
        "product pricing"
    ),

    // Delivery & Refunds
    Faq(
        "How do I set up custom delivery zones?",
        "Go to Logistics > Delivery Zones. You can draw a radius or select specific sectors on the map to define custom delivery charges and expected delivery times for each zone.",
        "delivery & refunds"
    ),
    Faq(
        "How do I process a customer refund?",
        "Locate the order in the Sales/Orders tab, click 'Process Refund', select the items being returned, and choose the refund method (cash, mobile money, or store credit).",
        "delivery & refunds"
    )
)

val feedbackCategories = listOf(
    "bug" to "Bug Report",
    "feature" to "Feature Suggestion",
    "general" to "General Feedback",
    "other" to "Other"
)