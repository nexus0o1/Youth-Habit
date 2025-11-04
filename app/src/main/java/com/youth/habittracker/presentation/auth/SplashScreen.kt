package com.youth.habittracker.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youth.habittracker.R
import com.youth.habittracker.data.model.User
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    currentUser: User?
) {
    LaunchedEffect(currentUser) {
        delay(2000) // 2 second splash delay
        if (currentUser != null) {
            onNavigateToMain()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo/icon
            Image(
                painter = painterResource(id = R.drawable.ic_splash),
                contentDescription = "Youth Habit Tracker Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Youth Habit Tracker",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Build healthy habits, achieve your goals",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}