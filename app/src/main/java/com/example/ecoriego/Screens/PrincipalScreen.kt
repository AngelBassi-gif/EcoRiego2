package com.example.ecoriego.Screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ecoriego.Composable.BottomBar
import com.example.ecoriego.Data.SecurePreferences
import com.example.ecoriego.NotificationHelper   // 🔥 IMPORTANTE
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.isActive

suspend fun checkFirebaseConnection(): Boolean {
    return try {
        val db = FirebaseDatabase.getInstance().getReference("status/check")
        db.setValue("ping").await()
        val value = db.get().await()
        value.exists()
    } catch (e: Exception) {
        false
    }
}

@Composable
fun PrincipalScreen(navController: NavHostController) {

    val context = LocalContext.current
    val prefs = SecurePreferences(context)

    var dispositivoConectado by remember { mutableStateOf(true) }
    var humedad by remember { mutableIntStateOf(0) }
    var riegoEncendido by remember { mutableStateOf(false) }

    // 🔥 NUEVO: HUMEDAD MÍNIMA
    var humedadMinima by remember { mutableIntStateOf(30) }

    // 🔥 NUEVO: evitar notificación repetida
    var notificacionEnviada by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val dropAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // 🔥 VERIFICAR CONEXIÓN
    LaunchedEffect(Unit) {
        while (isActive) {
            dispositivoConectado = checkFirebaseConnection()
            delay(3000)
        }
    }

    // 🔥 LEER HUMEDAD MÍNIMA DESDE FIREBASE
    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance().getReference("sensor")

        while (isActive) {
            try {
                val snapshot = db.child("humedad_minima").get().await()
                humedadMinima = snapshot.getValue(Int::class.java) ?: humedadMinima
            } catch (_: Exception) {}

            delay(2000)
        }
    }

    // 🔥 LEER HUMEDAD DESDE FIREBASE + ENVIAR NOTIFICACIÓN
    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance().getReference("sensor")

        while (isActive) {
            try {
                val snapshot = db.child("humedad").get().await()
                humedad = snapshot.getValue(Int::class.java) ?: 0
            } catch (_: Exception) {}

            // 🔥 NUEVO: ENVIAR NOTIFICACIÓN CUANDO BAJE LA HUMEDAD
            if (humedad < humedadMinima && !notificacionEnviada) {
                NotificationHelper.sendLowHumidityNotification(
                    context = context,
                    humedad = humedad
                )
                notificacionEnviada = true
            }

            // 🔥 Reestablecer cuando vuelva a subir la humedad
            if (humedad >= humedadMinima && notificacionEnviada) {
                notificacionEnviada = false
            }

            val intervalo = prefs.getIntervaloActualizacionSegundos()
            delay(intervalo * 1000L)
        }
    }

    // 🔥 LEER ESTADO DEL RIEGO DESDE FIREBASE
    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance().getReference("riego")

        while (isActive) {
            try {
                val snapshot = db.child("estado").get().await()
                val estado = snapshot.getValue(String::class.java) ?: "OFF"
                riegoEncendido = (estado == "ON")
            } catch (_: Exception) {}

            delay(2000)
        }
    }

    val colorHumedad = when {
        humedad < 40 -> Color(0xFFE57373)
        humedad < 70 -> Color(0xFF4CAF50)
        else -> Color(0xFF2196F3)
    }

    Scaffold(
        bottomBar = { BottomBar(navController) },
        containerColor = Color(0xFFE6F2EA)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (dispositivoConectado)
                    "✅ Dispositivo Conectado"
                else
                    "❌ Dispositivo Desconectado",
                fontSize = 16.sp,
                color = if (dispositivoConectado) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Humedad del suelo", fontSize = 20.sp, color = Color(0xFF2E3B2F))
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color(0xFFC8E6C9), RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(humedad / 100f)
                        .height(24.dp)
                        .background(colorHumedad, RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("$humedad%", fontSize = 18.sp, color = Color(0xFF2E3B2F))

            Spacer(modifier = Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                if (riegoEncendido) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .alpha(dropAlpha)
                            .background(Color(0xFF4FC3F7), CircleShape)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("💧 Riego activo", color = Color(0xFF0277BD))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 BOTÓN ON/OFF CON ACTUALIZACIÓN A FIREBASE
                Button(
                    onClick = {
                        val nuevoEstado = !riegoEncendido
                        riegoEncendido = nuevoEstado

                        val db = FirebaseDatabase.getInstance().getReference("riego")
                        db.child("estado").setValue(if (nuevoEstado) "ON" else "OFF")
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    if (nuevoEstado) "Riego activado" else "Riego desactivado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT)
                                    .show()
                                riegoEncendido = !nuevoEstado
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (riegoEncendido) Color(0xFF4CAF50) else Color(0xFFE57373)
                    )
                ) {
                    Text(
                        if (riegoEncendido) "💧 Riego ON" else "✋ Riego OFF",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("config") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
            ) {
                Text("⚙️ Configurar", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Bienvenido a EcoRiego", fontSize = 18.sp, color = Color(0xFF2E3B2F))
        }
    }
}
