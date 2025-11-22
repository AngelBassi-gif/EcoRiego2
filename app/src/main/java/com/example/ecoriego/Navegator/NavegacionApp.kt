package com.example.ecoriego.Navegator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecoriego.Screens.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavegacionApp() {

    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    // 🔥 AUTLOGIN REAL CON FIREBASE
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            // si está logeado → pasar a principal
            navController.navigate("principal") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") { SplashScreen(navController) }

        composable("login") { LoginScreen(navController) }

        composable("principal") { PrincipalScreen(navController) }

        composable("segunda") { SegundaScreen(navController) }

        composable("config") { ConfigScreen(navController) }

        // 🔥 AGREGA ESTA
        composable("createAccount") {
            CreateAccountScreen(navController)
        }
    }}
