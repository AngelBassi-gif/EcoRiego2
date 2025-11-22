package com.example.ecoriego.Composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.res.painterResource
import com.example.ecoriego.R

@Composable
fun BottomBar(navController: NavHostController) {

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF4CAF50)
    ) {
        NavigationBarItem(
            selected = currentRoute == "principal",
            onClick = {
                if (currentRoute != "principal") {
                    navController.navigate("principal") {
                        popUpTo("principal") { inclusive = true }
                    }
                }
            },
            icon = {
                Icon(
                    Icons.Default.Home,
                    "Principal",
                    tint = if (currentRoute == "principal") Color.White else Color.White.copy(alpha = 0.6f)
                )
            },
            label = {
                Text(
                    "Principal",
                    color = if (currentRoute == "principal") Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        )

        // 🔥 AQUÍ VA TU ICONO XML
        NavigationBarItem(
            selected = currentRoute == "segunda",
            onClick = {
                if (currentRoute != "segunda") {
                    navController.navigate("segunda")
                }
            },
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_chart_bar),
                    contentDescription = "Historial",
                    tint = if (currentRoute == "segunda") Color.White else Color.White.copy(alpha = 0.6f)
                )
            },
            label = {
                Text(
                    "Historial",
                    color = if (currentRoute == "segunda") Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        )

        NavigationBarItem(
            selected = currentRoute == "config",
            onClick = {
                if (currentRoute != "config") {
                    navController.navigate("config")
                }
            },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    "Config",
                    tint = if (currentRoute == "config") Color.White else Color.White.copy(alpha = 0.6f)
                )
            },
            label = {
                Text(
                    "Config",
                    color = if (currentRoute == "config") Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        )
    }
}
