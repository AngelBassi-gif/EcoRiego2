package com.example.ecoriego

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ecoriego.Navegator.NavegacionApp
import com.example.ecoriego.ui.theme.EcoRiegoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            EcoRiegoTheme {
                NavegacionApp()
            }
        }
    }
}
