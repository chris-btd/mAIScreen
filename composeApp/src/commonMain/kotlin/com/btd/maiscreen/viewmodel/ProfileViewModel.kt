package com.btd.maiscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.btd.maiscreen.common.BaseViewModel
import com.btd.maiscreen.model.ProfileUiState

/**
 * ViewModel for managing profile screen UI state
 */
class ProfileViewModel : BaseViewModel<ProfileUiState>(ProfileUiState()) {
    

    // Functions
    fun showChangePlanDialog() {
        setState {  copy(showChangePlanDialog = true) }
    }
    
    fun hideChangePlanDialog() {
        setState {  copy(showChangePlanDialog = false) }
    }
    
    fun showPurchaseMinutesScreen() {
        setState {  copy(showPurchaseMinutes = true) }
    }
    
    fun hidePurchaseMinutesScreen() {
        setState {  copy(showPurchaseMinutes = false) }
    }
    
    fun changePlan(plan: SubscriptionPlan) {
        setState { copy(isLoading = true, errorMessage = null)}
        // TODO: Call backend API to change plan
        // For now, simulate the change
        setState {
            copy(
                isLoading = false,
                errorMessage = null,
                successMessage = "Plan changed to ${plan.name} successfully!",
                currentPlan = plan
            )
        }
        hideChangePlanDialog()
    }
    
    fun purchaseMinutes(minutes: Int) {
        // TODO: Call backend API to purchase minutes
        // For now, simulate the purchase
        setState {
            copy(
                successMessage = "Successfully purchased $minutes minutes!",
            )
        }
        hidePurchaseMinutesScreen()
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

