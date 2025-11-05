package com.youth.habittracker.presentation.coins

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youth.habittracker.data.ads.AdState
import com.youth.habittracker.data.monetization.CoinTransaction
import com.youth.habittracker.data.monetization.PremiumFeature
import com.youth.habittracker.data.monetization.PurchaseStatus
import com.youth.habittracker.presentation.common.UiState
import com.youth.habittracker.presentation.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinsScreen(
    viewModel: CoinsViewModel = hiltViewModel()
) {
    val userCoins by viewModel.userCoins.collectAsState()
    val adState by viewModel.adState.collectAsState()
    val transactionHistory by viewModel.transactionHistory.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRewardedAd()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Coins & Rewards",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Coin balance card
            item {
                CoinBalanceCard(
                    coins = userCoins,
                    adsWatchedToday = viewModel.getAdsWatchedToday(),
                    adsRemainingToday = viewModel.getAdsRemainingToday()
                )
            }

            // Watch ads section
            item {
                WatchAdsSection(
                    adState = adState,
                    onWatchAd = { viewModel.showRewardedAd() },
                    onLoadAd = { viewModel.loadRewardedAd() }
                )
            }

            // Premium features
            item {
                Text(
                    text = "Premium Features",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(PremiumFeature.getAvailableFeatures()) { feature ->
                PremiumFeatureCard(
                    feature = feature,
                    userCoins = userCoins,
                    purchaseStatus = viewModel.getPurchaseStatus(feature),
                    onPurchase = { viewModel.purchaseFeature(feature) }
                )
            }

            // Transaction history
            item {
                Text(
                    text = "Recent Transactions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (transactionHistory.isEmpty()) {
                item {
                    Text(
                        text = "No transactions yet. Watch ads or complete achievements to earn coins!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else {
                items(transactionHistory.take(10)) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun CoinBalanceCard(
    coins: Int,
    adsWatchedToday: Int,
    adsRemainingToday: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Coin icon and balance
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = coins.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " Coins",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ad progress
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Ads Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = adsWatchedToday.toFloat() / 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$adsWatchedToday / 10 ads watched today",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchAdsSection(
    adState: AdState,
    onWatchAd: () -> Unit,
    onLoadAd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Earn Coins",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Watch rewarded ads to earn coins",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (adState) {
                is AdState.Ready -> {
                    Button(
                        onClick = onWatchAd,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Watch Ad (+1 Coin)")
                    }
                }
                is AdState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading ad...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                is AdState.Completed -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Earned ${adState.coinsEarned} coin${if (adState.coinsEarned > 1) "s" else ""}!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Green
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onLoadAd
                    ) {
                        Text("Watch Another Ad")
                    }
                }
                is AdState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = adState.message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onLoadAd
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onLoadAd
                    ) {
                        Text("Load Ad")
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureCard(
    feature: PremiumFeature,
    userCoins: Int,
    purchaseStatus: PurchaseStatus,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${feature.cost}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))

                when (purchaseStatus) {
                    PurchaseStatus.AFFORDABLE -> {
                        Button(onClick = onPurchase) {
                            Text("Buy")
                        }
                    }
                    PurchaseStatus.NEARLY_AFFORDABLE -> {
                        OutlinedButton(onClick = onPurchase) {
                            Text("Need ${(feature.cost - userCoins)} more")
                        }
                    }
                    PurchaseStatus.NOT_AFFORDABLE -> {
                        OutlinedButton(
                            onClick = { /* Show coins screen */ },
                            enabled = false
                        ) {
                            Text("Insufficient coins")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(transaction: CoinTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (transaction.amount > 0) {
                        Icons.Default.AddCircle
                    } else {
                        Icons.Default.RemoveCircle
                    },
                    contentDescription = null,
                    tint = if (transaction.amount > 0) {
                        Color.Green
                    } else {
                        Color.Red
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transaction.source.name.replace("_", " "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (transaction.amount > 0) "+" else ""}${transaction.amount}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount > 0) {
                    Color.Green
                } else {
                    Color.Red
                }
            )
        }
    }
}

private fun PremiumFeature.getAvailableFeatures(): List<PremiumFeature> {
    return listOf(
        CUSTOM_THEME_PACK,
        ADVANCED_ANALYTICS,
        UNLIMITED_REMINDERS,
        DATA_EXPORT,
        EXTRA_HABIT_SLOTS,
        ANONYMOUS_MODE
    )
}