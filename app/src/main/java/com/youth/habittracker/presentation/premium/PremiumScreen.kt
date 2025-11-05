package com.youth.habittracker.presentation.premium

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youth.habittracker.data.billing.UserSubscription
import com.youth.habittracker.data.billing.SubscriptionState
import com.youth.habittracker.data.model.SubscriptionStatus
import com.youth.habittracker.presentation.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val userSubscription by viewModel.userSubscription.collectAsState()
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()

    val isPremium = viewModel.isPremiumUser()
    val daysUntilExpiry = viewModel.getDaysUntilExpiry()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Premium",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { paddingValues ->
        if (isPremium) {
            ActiveSubscriptionView(
                subscription = userSubscription,
                daysUntilExpiry = daysUntilExpiry,
                onManageSubscription = { /* Open Google Play subscription management */ },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            FreeUserView(
                availableProducts = availableProducts,
                subscriptionState = subscriptionState,
                onPurchaseMonthly = { viewModel.purchaseMonthlySubscription() },
                onPurchaseYearly = { viewModel.purchaseYearlySubscription() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun FreeUserView(
    availableProducts: List<com.android.billingclient.api.ProductDetails>,
    subscriptionState: SubscriptionState,
    onPurchaseMonthly: () -> Unit,
    onPurchaseYearly: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PremiumHeaderCard()
        }

        item {
            Text(
                text = "Choose Your Plan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            PremiumPlansComparison(
                monthlyProduct = availableProducts.find { it.productId == "premium_monthly" },
                yearlyProduct = availableProducts.find { it.productId == "premium_yearly" },
                subscriptionState = subscriptionState,
                onPurchaseMonthly = onPurchaseMonthly,
                onPurchaseYearly = onPurchaseYearly
            )
        }

        item {
            PremiumFeaturesList()
        }

        item {
            TrialInfoCard()
        }

        item {
            TestimonialCard()
        }
    }
}

@Composable
private fun ActiveSubscriptionView(
    subscription: UserSubscription?,
    daysUntilExpiry: Long,
    onManageSubscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Premium Active",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (subscription?.isTrialPeriod == true) {
            Text(
                text = "Trial Period - $daysUntilExpiry days left",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            Text(
                text = "$daysUntilExpiry days remaining",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PremiumFeaturesList()

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onManageSubscription,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Subscription")
        }
    }
}

@Composable
private fun PremiumHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Upgrade to Premium",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Unlock all features and remove ads",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PremiumPlansComparison(
    monthlyProduct: com.android.billingclient.api.ProductDetails?,
    yearlyProduct: com.android.billingclient.api.ProductDetails?,
    subscriptionState: SubscriptionState,
    onPurchaseMonthly: () -> Unit,
    onPurchaseYearly: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Monthly Plan
        SubscriptionPlanCard(
            title = "Monthly",
            price = monthlyProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99",
            period = "per month",
            features = listOf(
                "Unlimited habits",
                "Remove all ads",
                "Advanced analytics",
                "Custom themes",
                "Priority support"
            ),
            isPopular = false,
            isLoading = subscriptionState is SubscriptionState.Loading,
            onSubscribe = onPurchaseMonthly,
            modifier = Modifier.weight(1f)
        )

        // Yearly Plan
        SubscriptionPlanCard(
            title = "Yearly",
            price = yearlyProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$39.99",
            period = "per year",
            badge = "Save 20%",
            features = listOf(
                "Everything in Monthly",
                "2 months free",
                "Best value",
                "All premium features"
            ),
            isPopular = true,
            isLoading = subscriptionState is SubscriptionState.Loading,
            onSubscribe = onPurchaseYearly,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SubscriptionPlanCard(
    title: String,
    price: String,
    period: String,
    features: List<String>,
    badge: String? = null,
    isPopular: Boolean,
    isLoading: Boolean,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = if (isPopular) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = if (isPopular) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Badge
            badge?.let {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Title
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price
            Text(
                text = price,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = period,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Features
            features.forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subscribe button
            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Subscribe")
                }
            }

            // Trial info
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "7-day free trial",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PremiumFeaturesList() {
    val features = listOf(
        Feature("Unlimited Habits", Icons.Default.Infinity),
        Feature("No Advertisements", Icons.Default.Block),
        Feature("Advanced Analytics", Icons.Default.Analytics),
        Feature("Custom Themes", Icons.Default.Palette),
        Feature("Priority Support", Icons.Default.SupportAgent),
        Feature("Data Export", Icons.Default.Download),
        Feature("Anonymous Mode", Icons.Default.VisibilityOff),
        Feature("Extra Reminders", Icons.Default.Notifications)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        features.forEach { feature ->
            PremiumFeatureItem(feature = feature)
        }
    }
}

@Composable
private fun PremiumFeatureItem(feature: Feature) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = feature.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TrialInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "7-Day Free Trial",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try Premium risk-free. Cancel anytime during the trial period.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TestimonialCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "What Users Say",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "\"Premium features have completely transformed how I track my habits. The analytics alone are worth it!\"",
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "- Sarah M., Premium User",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class Feature(
    val name: String,
    val icon: ImageVector
)