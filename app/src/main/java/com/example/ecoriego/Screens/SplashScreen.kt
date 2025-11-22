package com.example.ecoriego.Screens

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.ecoriego.Data.SecurePreferences
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current
    val prefs = SecurePreferences(context)

    LaunchedEffect(Unit) {
        delay(1500)

        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val localLogged = prefs.isLoggedIn()

        if (firebaseUser != null && localLogged) {
            navController.navigate("principal") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F2EA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🌿💧", fontSize = 64.sp)
            Text(
                "EcoRiego",
                fontSize = 36.sp,
                color = Color(0xFF2E3B2F)
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = Color(0xFF4CAF50),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}