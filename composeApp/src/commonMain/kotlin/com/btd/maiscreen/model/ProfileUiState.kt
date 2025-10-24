package com.btd.maiscreen.model

import com.btd.maiscreen.common.UiState
import com.btd.maiscreen.viewmodel.SubscriptionPlan

data class ProfileUiState(
    var currentPlan: SubscriptionPlan? = SubscriptionPlan(
        name = "Free",
        price = "$0",
        priceSubtext = "Forever",
        minutes = 30,
        features = listOf(
            "Stream to only one device at a time",
            "Community Support",
            "720p and 1080p resolution streaming",
            "30 minutes of AI content submission"
        )
    ),
    var currentMinutes: Int = 0,
    var purchasedMinutes: Int = 0,
    var isLoading: Boolean = false,
    var errorMessage: String? = null,
    var successMessage: String? = null,
    var showChangePlanDialog: Boolean = false,
    var showPurchaseMinutes: Boolean = true,

) : UiState {
    var tester: Int = 3
        get() = 0

    // Available plans for upgrade
    //TODO: Add this as a remote configuration
    val availablePlans: List<SubscriptionPlan>
        get() = listOf(
            SubscriptionPlan(
                name = "Free",
                price = "$0",
                priceSubtext = "Forever",
                minutes = 30,
                features = listOf(
                    "Stream to only one device at a time",
                    "Community Support",
                    "720p and 1080p resolution streaming",
                    "30 minutes of AI content submission"
                )
            ),
            SubscriptionPlan(
                name = "Standard",
                price = "$3.99",
                priceSubtext = "per month",
                minutes = 75,
                features = listOf(
                    "Stream on up to 3 devices simultaneously",
                    "Community Support",
                    "1080p resolution streaming",
                    "1 hour 15 minutes of AI content submission",
                    "✨ Unused minutes roll over to the next month!"
                ),
                isPopular = true
            ),
            SubscriptionPlan(
                name = "Plus",
                price = "$8.99",
                priceSubtext = "per month",
                minutes = 150,
                features = listOf(
                    "Stream on up to 3 devices simultaneously",
                    "Community Support",
                    "4K resolution streaming",
                    "2 hours 30 minutes of AI content submission",
                    "✨ Unused minutes roll over to the next month!"
                )
            )
        )
}

/*
// Current minute balance
var currentMinutes by mutableStateOf(30)
    private set

// Purchased minutes (separate from subscription minutes)
var purchasedMinutes by mutableStateOf(0)
    private set

// Loading state
var isLoading by mutableStateOf(false)
    private set

// Error message
var errorMessage by mutableStateOf<String?>(null)
    private set

// Success message
var successMessage by mutableStateOf<String?>(null)
    private set

// Show change plan dialog
var showChangePlanDialog by mutableStateOf(false)
    private set

// Show purchase minutes screen
var showPurchaseMinutes by mutableStateOf(false)
    private set
    */