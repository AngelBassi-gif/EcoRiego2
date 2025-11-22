package com.example.ecoriego.Screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ecoriego.Composable.BottomBar
import com.example.ecoriego.Data.SecurePreferences
import com.example.ecoriego.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
private fun EcoCard(
    title: String? = null,
    icon: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = Color(0xFF2F5D34)
                        )
                    }
                    Text(
                        title,
                        color = Color(0xFF2F5D34),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(navController: NavHostController) {

    val context = LocalContext.current
    val prefs = SecurePreferences(context)

    // Firebase
    val database = FirebaseDatabase.getInstance()
    val configRef = database.getReference("config")

    var humedadMin by remember { mutableIntStateOf(prefs.getHumedadMinima()) }
    var autoRiego by remember { mutableStateOf(prefs.isRiegoAutomaticoEnabled()) }
    var intervaloRiego by remember { mutableIntStateOf(prefs.getIntervaloRiego()) }
    var intervaloActualizacion by remember { mutableIntStateOf(prefs.getIntervaloActualizacionSegundos()) }

    val ecoGreen = Color(0xFF4CAF50)
    val ecoDark = Color(0xFF2F5D34)
    val ecoLight = Color(0xFFE8F5E9)
    val ecoYellow = Color(0xFFFFA726)
    val ecoRed = Color(0xFFE53935)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Configuración") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ecoLight
                )
            )
        },
        bottomBar = { BottomBar(navController) },
        containerColor = ecoLight
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // RIEGO AUTOMÁTICO
            EcoCard(title = "Riego automático", icon = R.drawable.ic_water) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Activar riego automático", color = ecoDark)
                        Text(
                            "El sistema regará cuando sea necesario",
                            style = MaterialTheme.typography.bodySmall,
                            color = ecoDark.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = autoRiego,
                        onCheckedChange = {
                            autoRiego = it
                            prefs.saveRiegoAutomatico(it)

                            Toast.makeText(
                                context,
                                if (it) "Riego automático activado ✓" else "Riego automático desactivado ✓",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ecoGreen,
                            checkedTrackColor = ecoGreen.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // HUMEDAD MÍNIMA
            EcoCard(title = "Humedad mínima", icon = R.drawable.ic_humidity) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Valor actual:", color = ecoDark)
                    Text(
                        "$humedadMin%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = when {
                            humedadMin < 30 -> ecoRed
                            humedadMin < 50 -> ecoYellow
                            else -> ecoGreen
                        }
                    )
                }

                Slider(
                    value = humedadMin.toFloat(),
                    onValueChange = { humedadMin = it.toInt() },
                    onValueChangeFinished = {
                        prefs.saveHumedadMinima(humedadMin)

                        Toast.makeText(
                            context,
                            "Humedad mínima: $humedadMin% ✓",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    valueRange = 20f..80f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = ecoGreen,
                        activeTrackColor = ecoGreen
                    )
                )
            }

            // INTERVALO DE RIEGO
            EcoCard(title = "Intervalo de riego", icon = R.drawable.ic_timer) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Frecuencia:", color = ecoDark)
                    Text(
                        "Cada $intervaloRiego min",
                        style = MaterialTheme.typography.titleMedium,
                        color = ecoGreen
                    )
                }

                Slider(
                    value = intervaloRiego.toFloat(),
                    onValueChange = { intervaloRiego = it.toInt() },
                    onValueChangeFinished = {
                        prefs.saveIntervaloRiego(intervaloRiego)

                        Toast.makeText(
                            context,
                            "Intervalo: $intervaloRiego min ✓",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    valueRange = 15f..180f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = ecoGreen,
                        activeTrackColor = ecoGreen
                    )
                )
            }

            // INTERVALO DE ACTUALIZACIÓN
            EcoCard(title = "Actualización del sensor", icon = R.drawable.ic_sync) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Frecuencia:", color = ecoDark)
                    Text(
                        "Cada $intervaloActualizacion seg",
                        style = MaterialTheme.typography.titleMedium,
                        color = ecoGreen
                    )
                }

                Slider(
                    value = intervaloActualizacion.toFloat(),
                    onValueChange = { intervaloActualizacion = it.toInt() },
                    onValueChangeFinished = {
                        prefs.saveIntervaloActualizacionSegundos(intervaloActualizacion)

                        Toast.makeText(
                            context,
                            "Actualización: cada $intervaloActualizacion seg ✓",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    valueRange = 1f..30f,
                    colors = SliderDefaults.colors(
                        thumbColor = ecoGreen,
                        activeTrackColor = ecoGreen
                    )
                )

                if (intervaloActualizacion < 5) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_warning),
                            contentDescription = null,
                            tint = ecoYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Intervalos cortos consumen más batería",
                            style = MaterialTheme.typography.bodySmall,
                            color = ecoYellow
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // CERRAR SESIÓN
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    prefs.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ecoRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
