package com.youth.habittracker.data.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManager @Inject constructor(
    private val context: Context
) {
    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false
    private var adsWatchedToday = 0
    private var lastAdWatchDate: Long = 0

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    val adState: StateFlow<AdState> = _adState

    init {
        MobileAds.initialize(context) { initializationStatus ->
            // AdMob initialized
        }
    }

    fun loadRewardedAd() {
        if (isAdLoading || rewardedAd != null) {
            return
        }

        if (!canWatchMoreAds()) {
            _adState.value = AdState.Error("Daily limit reached. Watch up to 10 ads per day.")
            return
        }

        isAdLoading = true
        _adState.value = AdState.Loading

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                    _adState.value = AdState.Ready
                    setupAdCallbacks()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isAdLoading = false
                    rewardedAd = null
                    _adState.value = AdState.Error("Failed to load ad: ${adError.message}")
                }
            }
        )
    }

    private fun setupAdCallbacks() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _adState.value = AdState.Idle
                // Preload next ad
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                _adState.value = AdState.Error("Failed to show ad: ${adError.message}")
            }

            override fun onAdShowedFullScreenContent() {
                _adState.value = AdState.Showing
            }
        }
    }

    fun showRewardedAd(activity: Activity, onAdCompleted: (Boolean, Int) -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            _adState.value = AdState.Error("Ad not ready. Please try again.")
            onAdCompleted(false, 0)
            return
        }

        if (!canWatchMoreAds()) {
            _adState.value = AdState.Error("Daily limit reached. Watch up to 10 ads per day.")
            onAdCompleted(false, 0)
            return
        }

        ad.show(activity) { rewardItem ->
            // User earned reward
            val coinsEarned = rewardItem.amount
            adsWatchedToday++
            lastAdWatchDate = System.currentTimeMillis()

            _adState.value = AdState.Completed(coinsEarned)
            onAdCompleted(true, coinsEarned)
        }
    }

    fun canWatchMoreAds(): Boolean {
        val today = System.currentTimeMillis()
        val daysSinceLastAd = (today - lastAdWatchDate) / (24 * 60 * 60 * 1000)

        // Reset counter if it's a new day
        if (daysSinceLastAd >= 1) {
            adsWatchedToday = 0
            lastAdWatchDate = 0
        }

        return adsWatchedToday < MAX_ADS_PER_DAY
    }

    fun getAdsRemainingToday(): Int {
        val today = System.currentTimeMillis()
        val daysSinceLastAd = (today - lastAdWatchDate) / (24 * 60 * 60 * 1000)

        if (daysSinceLastAd >= 1) {
            return MAX_ADS_PER_DAY
        }

        return MAX_ADS_PER_DAY - adsWatchedToday
    }

    fun getAdsWatchedToday(): Int {
        val today = System.currentTimeMillis()
        val daysSinceLastAd = (today - lastAdWatchDate) / (24 * 60 * 60 * 1000)

        if (daysSinceLastAd >= 1) {
            return 0
        }

        return adsWatchedToday
    }

    fun resetDailyCounter() {
        adsWatchedToday = 0
        lastAdWatchDate = 0
    }

    fun clearAdState() {
        _adState.value = AdState.Idle
    }

    companion object {
        // Replace with your actual ad unit IDs
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Test ad unit ID
        private const val MAX_ADS_PER_DAY = 10
    }
}

sealed class AdState {
    object Idle : AdState()
    object Loading : AdState()
    object Ready : AdState()
    object Showing : AdState()
    data class Completed(val coinsEarned: Int) : AdState()
    data class Error(val message: String) : AdState()
}