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
        // Validate plan input
        if (plan.name.isBlank() || plan.minutes < 0) {
            setState {
                copy(
                    errorMessage = "Invalid plan selected",
                    isLoading = false
                )
            }
            return
        }

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
        // Validate minutes input
        if (minutes <= 0 || minutes > 10000) {
            setState {
                copy(errorMessage = "Invalid minutes amount")
            }
            return
        }

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

