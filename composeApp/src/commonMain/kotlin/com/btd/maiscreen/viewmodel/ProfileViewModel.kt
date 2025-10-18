package com.btd.maiscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel for managing profile screen UI state
 */
class ProfileViewModel : ViewModel() {
    
    // User's current subscription plan
    var currentPlan by mutableStateOf<SubscriptionPlan?>(
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
        )
    )
        private set
    
    // Available plans for upgrade
    val availablePlans = listOf(
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
    
    // Functions
    fun showChangePlanDialog() {
        showChangePlanDialog = true
    }
    
    fun hideChangePlanDialog() {
        showChangePlanDialog = false
    }
    
    fun showPurchaseMinutesScreen() {
        showPurchaseMinutes = true
    }
    
    fun hidePurchaseMinutesScreen() {
        showPurchaseMinutes = false
    }
    
    fun changePlan(plan: SubscriptionPlan) {
        isLoading = true
        errorMessage = null
        
        // TODO: Call backend API to change plan
        // For now, simulate the change
        currentPlan = plan
        isLoading = false
        successMessage = "Plan changed to ${plan.name} successfully!"
        hideChangePlanDialog()
    }
    
    fun purchaseMinutes(minutes: Int) {
        // TODO: Call backend API to purchase minutes
        // For now, simulate the purchase
        purchasedMinutes += minutes
        successMessage = "Successfully purchased $minutes minutes!"
        hidePurchaseMinutesScreen()
    }
    
    fun showError(message: String) {
        errorMessage = message
    }
    
    fun clearError() {
        errorMessage = null
    }
    
    fun clearSuccess() {
        successMessage = null
    }
    
    fun setProfileLoading(loading: Boolean) {
        isLoading = loading
    }
}

data class SubscriptionPlan(
    val name: String,
    val price: String,
    val priceSubtext: String,
    val minutes: Int,
    val features: List<String>,
    val isPopular: Boolean = false
)

