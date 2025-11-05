package com.youth.habittracker.presentation.premium

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youth.habittracker.data.billing.BillingManager
import com.youth.habittracker.data.billing.SubscriptionState
import com.youth.habittracker.data.billing.UserSubscription
import com.youth.habittracker.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    val userSubscription = billingManager.userSubscription
    val availableProducts = billingManager.availableProducts
    val subscriptionState = billingManager.subscriptionState

    private val _purchaseState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val purchaseState: StateFlow<UiState<String>> = _purchaseState.asStateFlow()

    fun purchaseMonthlySubscription(activity: ComponentActivity) {
        viewModelScope.launch {
            _purchaseState.value = UiState.Loading

            billingManager.purchaseSubscription(activity, BillingManager.PREMIUM_MONTHLY)
                .onSuccess { productId ->
                    _purchaseState.value = UiState.Success(productId)
                }
                .onFailure { exception ->
                    _purchaseState.value = UiState.Error(exception.message ?: "Purchase failed")
                }
        }
    }

    fun purchaseYearlySubscription(activity: ComponentActivity) {
        viewModelScope.launch {
            _purchaseState.value = UiState.Loading

            billingManager.purchaseSubscription(activity, BillingManager.PREMIUM_YEARLY)
                .onSuccess { productId ->
                    _purchaseState.value = UiState.Success(productId)
                }
                .onFailure { exception ->
                    _purchaseState.value = UiState.Error(exception.message ?: "Purchase failed")
                }
        }
    }

    suspend fun checkSubscriptionStatus(): Result<UserSubscription> {
        return billingManager.checkSubscriptionStatus()
    }

    fun isPremiumUser(): Boolean {
        return billingManager.isPremiumUser()
    }

    fun hasActiveSubscription(): Boolean {
        return billingManager.hasActiveSubscription()
    }

    fun getDaysUntilExpiry(): Long {
        return billingManager.getDaysUntilExpiry()
    }

    fun clearPurchaseState() {
        _purchaseState.value = UiState.Idle
    }

    fun clearSubscriptionState() {
        billingManager.clearSubscriptionState()
    }
}