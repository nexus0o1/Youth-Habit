package com.youth.habittracker.presentation.coins

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youth.habittracker.data.ads.AdMobManager
import com.youth.habittracker.data.ads.AdState
import com.youth.habittracker.data.monetization.CoinManager
import com.youth.habittracker.data.monetization.CoinTransaction
import com.youth.habittracker.data.monetization.CoinSource
import com.youth.habittracker.data.monetization.PremiumFeature
import com.youth.habittracker.data.monetization.PurchaseStatus
import com.youth.habittracker.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinsViewModel @Inject constructor(
    private val coinManager: CoinManager,
    private val adMobManager: AdMobManager
) : ViewModel() {

    val userCoins = coinManager.userCoins
    val transactionHistory = coinManager.transactionHistory
    val adState = adMobManager.adState

    private val _purchaseState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val purchaseState: StateFlow<UiState<Unit>> = _purchaseState.asStateFlow()

    init {
        // Preload an ad when the screen opens
        loadRewardedAd()
    }

    fun loadRewardedAd() {
        adMobManager.loadRewardedAd()
    }

    fun showRewardedAd(activity: ComponentActivity) {
        adMobManager.showRewardedAd(activity) { success, coinsEarned ->
            if (success) {
                viewModelScope.launch {
                    coinManager.addCoins(
                        amount = coinsEarned,
                        source = CoinSource.AD_WATCH,
                        description = "Watched rewarded video ad"
                    )
                }
            }
        }
    }

    fun purchaseFeature(feature: PremiumFeature) {
        if (coinManager.canAfford(feature.cost)) {
            viewModelScope.launch {
                _purchaseState.value = UiState.Loading

                coinManager.purchasePremiumFeature(feature, feature.cost)
                    .onSuccess {
                        _purchaseState.value = UiState.Success(Unit)
                    }
                    .onFailure { exception ->
                        _purchaseState.value = UiState.Error(exception.message ?: "Purchase failed")
                    }
            }
        }
    }

    fun sendCheer(toUserId: String, message: String? = null) {
        viewModelScope.launch {
            coinManager.sendCheer(
                fromUserId = "", // Current user ID would come from auth
                toUserId = toUserId,
                message = message
            )
        }
    }

    fun grantDailyStreakBonus(streakDays: Int) {
        viewModelScope.launch {
            coinManager.grantDailyStreakBonus(streakDays)
        }
    }

    fun grantWeeklyGoalBonus(weeklyCompletions: Int) {
        viewModelScope.launch {
            coinManager.grantWeeklyGoalBonus(weeklyCompletions)
        }
    }

    fun grantAchievementBonus(achievementId: String, coins: Int) {
        viewModelScope.launch {
            coinManager.grantAchievementBonus(achievementId, coins)
        }
    }

    fun getAdsWatchedToday(): Int {
        return adMobManager.getAdsWatchedToday()
    }

    fun getAdsRemainingToday(): Int {
        return adMobManager.getAdsRemainingToday()
    }

    fun canWatchMoreAds(): Boolean {
        return adMobManager.canWatchMoreAds()
    }

    fun getPurchaseStatus(feature: PremiumFeature): PurchaseStatus {
        return coinManager.getPurchaseStatus(feature)
    }

    fun clearPurchaseState() {
        _purchaseState.value = UiState.Idle
    }

    fun clearAdState() {
        adMobManager.clearAdState()
    }
}