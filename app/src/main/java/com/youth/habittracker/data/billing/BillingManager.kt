package com.youth.habittracker.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : PurchasesUpdatedListener, BillingClientStateListener {

    private lateinit var billingClient: BillingClient

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Idle)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts.asStateFlow()

    private val _userSubscription = MutableStateFlow<UserSubscription?>(null)
    val userSubscription: StateFlow<UserSubscription?> = _userSubscription.asStateFlow()

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingResponseCode.OK) {
            // Billing client is ready
            loadProducts()
            loadUserSubscription()
        } else {
            _subscriptionState.value = SubscriptionState.Error("Billing setup failed: ${billingResult.debugMessage}")
        }
    }

    override fun onBillingServiceDisconnected() {
        // Try to reconnect
        setupBillingClient()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                _subscriptionState.value = SubscriptionState.Canceled
            }
            else -> {
                _subscriptionState.value = SubscriptionState.Error("Purchase failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun loadProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_monthly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_yearly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                _availableProducts.value = productDetailsList
            } else {
                _subscriptionState.value = SubscriptionState.Error("Failed to load products: ${billingResult.debugMessage}")
            }
        }
    }

    private fun loadUserSubscription() {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return
                    val user = snapshot?.toObject(com.youth.habittracker.data.model.User::class.java)
                    user?.let {
                        _userSubscription.value = UserSubscription(
                            status = it.subscriptionStatus,
                            productId = it.subscriptionId ?: "",
                            purchaseTime = it.subscriptionPurchaseTime?.toDate()?.time ?: 0,
                            expiryTime = it.subscriptionExpiryTime?.toDate()?.time ?: 0,
                            isTrialPeriod = it.subscriptionIsTrialPeriod ?: false,
                            autoRenewing = it.subscriptionAutoRenewing ?: false
                        )
                    }
                }
        }
    }

    suspend fun purchaseSubscription(activity: Activity, productId: String): Result<String> {
        return try {
            val productDetails = _availableProducts.value.find { it.productId == productId }
                ?: throw Exception("Product not found")

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()

            val billingResult = suspendCancellableCoroutine<BillingResult> { cont ->
                billingClient.launchBillingFlow(activity, billingFlowParams).run {
                    cont.resume(this)
                }
            }

            if (billingResult.responseCode == BillingResponseCode.OK) {
                Result.success(productId)
            } else {
                Result.failure(Exception("Purchase failed: ${billingResult.debugMessage}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            updateSubscriptionInFirestore(purchase)
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                // Purchase acknowledged successfully
            }
        }
    }

    private fun updateSubscriptionInFirestore(purchase: Purchase) {
        auth.currentUser?.uid?.let { userId ->
            val subscriptionData = mapOf(
                "subscriptionStatus" to "premium",
                "subscriptionId" to purchase.products.first(),
                "subscriptionPurchaseTime" to purchase.purchaseTime,
                "subscriptionExpiryTime" to getSubscriptionExpiryTime(purchase),
                "subscriptionIsTrialPeriod" to isTrialPeriod(purchase),
                "subscriptionAutoRenewing" to purchase.isAutoRenewing
            )

            firestore.collection("users")
                .document(userId)
                .update(subscriptionData)
        }
    }

    private fun getSubscriptionExpiryTime(purchase: Purchase): Long {
        // For subscriptions, calculate expiry based on purchase time and subscription type
        val purchaseTime = purchase.purchaseTime
        val productId = purchase.products.first()

        return when (productId) {
            "premium_monthly" -> purchaseTime + (30L * 24 * 60 * 60 * 1000) // 30 days
            "premium_yearly" -> purchaseTime + (365L * 24 * 60 * 60 * 1000) // 365 days
            else -> purchaseTime + (30L * 24 * 60 * 60 * 1000) // Default 30 days
        }
    }

    private fun isTrialPeriod(purchase: Purchase): Boolean {
        // Check if this is a trial purchase
        // Implementation depends on your trial configuration in Google Play Console
        return purchase.products.first().endsWith("_trial")
    }

    suspend fun checkSubscriptionStatus(): Result<UserSubscription> {
        return try {
            auth.currentUser?.uid?.let { userId ->
                val purchases = queryPurchases()
                val activePurchase = purchases.find { purchase ->
                    purchase.products.any { it.startsWith("premium") } &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            purchase.isAutoRenewing
                }

                if (activePurchase != null) {
                    val subscription = UserSubscription(
                        status = com.youth.habittracker.data.model.SubscriptionStatus.PREMIUM,
                        productId = activePurchase.products.first(),
                        purchaseTime = activePurchase.purchaseTime,
                        expiryTime = getSubscriptionExpiryTime(activePurchase),
                        isTrialPeriod = isTrialPeriod(activePurchase),
                        autoRenewing = activePurchase.isAutoRenewing
                    )
                    Result.success(subscription)
                } else {
                    Result.success(
                        UserSubscription(
                            status = com.youth.habittracker.data.model.SubscriptionStatus.FREE,
                            productId = "",
                            purchaseTime = 0,
                            expiryTime = 0,
                            isTrialPeriod = false,
                            autoRenewing = false
                        )
                    )
                }
            } ?: Result.failure(Exception("User not authenticated"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun queryPurchases(): List<Purchase> {
        return suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult, purchases ->
                cont.resume(purchases)
            }
        }
    }

    fun isPremiumUser(): Boolean {
        return _userSubscription.value?.status == com.youth.habittracker.data.model.SubscriptionStatus.PREMIUM
    }

    fun hasActiveSubscription(): Boolean {
        val subscription = _userSubscription.value
        return subscription != null &&
                subscription.status == com.youth.habittracker.data.model.SubscriptionStatus.PREMIUM &&
                subscription.expiryTime > System.currentTimeMillis()
    }

    fun getDaysUntilExpiry(): Long {
        val subscription = _userSubscription.value ?: return 0
        if (subscription.expiryTime == 0L) return Long.MAX_VALUE
        return (subscription.expiryTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
    }

    fun clearSubscriptionState() {
        _subscriptionState.value = SubscriptionState.Idle
    }

    companion object {
        // Product IDs (must match Google Play Console)
        const val PREMIUM_MONTHLY = "premium_monthly"
        const val PREMIUM_YEARLY = "premium_yearly"
        const val PREMIUM_MONTHLY_TRIAL = "premium_monthly_trial"
        const val PREMIUM_YEARLY_TRIAL = "premium_yearly_trial"
    }
}

data class UserSubscription(
    val status: com.youth.habittracker.data.model.SubscriptionStatus,
    val productId: String,
    val purchaseTime: Long,
    val expiryTime: Long,
    val isTrialPeriod: Boolean,
    val autoRenewing: Boolean
)

sealed class SubscriptionState {
    object Idle : SubscriptionState()
    object Loading : SubscriptionState()
    object Success : SubscriptionState()
    object Canceled : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}

data class SubscriptionProduct(
    val productId: String,
    val name: String,
    val description: String,
    val price: String,
    val trialPeriod: String?,
    val billingPeriod: String
)