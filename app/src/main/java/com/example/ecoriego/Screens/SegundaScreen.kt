package com.example.ecoriego.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ecoriego.Composable.BottomBar
import com.example.ecoriego.Data.SecurePreferences
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegundaScreen(navController: NavHostController) {

    val context = LocalContext.current
    val prefs = SecurePreferences(context)

    var intervaloActualizacion by remember { mutableIntStateOf(prefs.getIntervaloActualizacionSegundos()) }
    val humedadPorHora = remember { mutableStateListOf<Pair<Int, Int>>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance().getReference("humedad_24h")

        while (isActive) {
            try {
                val snapshot = db.get().await()
                humedadPorHora.clear()

                for (i in 0..23) {
                    val hum = snapshot.child(i.toString()).getValue(Int::class.java) ?: 0
                    humedadPorHora.add(i to hum)
                }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }

            intervaloActualizacion = prefs.getIntervaloActualizacionSegundos()
            kotlinx.coroutines.delay(intervaloActualizacion * 1000L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Historial de Humedad") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE6F2EA)
                )
            )
        },
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

            if (isLoading) {
                Spacer(modifier = Modifier.height(40.dp))
                CircularProgressIndicator(color = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando datos...", color = Color(0xFF2E3B2F))
            } else {

                // ESTADÍSTICAS
                val promedio = if (humedadPorHora.isNotEmpty()) {
                    humedadPorHora.map { it.second }.average().toInt()
                } else 0

                val maxHumedad = humedadPorHora.maxByOrNull { it.second }
                val minHumedad = humedadPorHora.minByOrNull { it.second }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Resumen últimas 24 horas",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E3B2F)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Mínimo", style = MaterialTheme.typography.bodySmall)
                                Text("${minHumedad?.second ?: 0}%",
                                    fontSize = 20.sp,
                                    color = Color(0xFFE57373)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Promedio", style = MaterialTheme.typography.bodySmall)
                                Text("$promedio%",
                                    fontSize = 20.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Máximo", style = MaterialTheme.typography.bodySmall)
                                Text("${maxHumedad?.second ?: 0}%",
                                    fontSize = 20.sp,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GRÁFICO DE BARRAS
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    humedadPorHora.forEach { (hora, hum) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "%02d:00".format(hora),
                                fontSize = 12.sp,
                                modifier = Modifier.width(50.dp),
                                color = Color(0xFF2E3B2F)
                            )

                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width((hum * 2.5).dp.coerceAtLeast(2.dp))
                                    .background(
                                        when {
                                            hum < 40 -> Color(0xFFE57373)
                                            hum < 70 -> Color(0xFF4CAF50)
                                            else -> Color(0xFF2196F3)
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$hum%", fontSize = 12.sp, color = Color(0xFF2E3B2F))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTÓN COMPARTIR
                Button(
                    onClick = {
                        val report = buildString {
                            append("📊 REPORTE DE HUMEDAD - EcoRiego\n")
                            append("================================\n\n")
                            append("Promedio: $promedio%\n")
                            append("Mínimo: ${minHumedad?.second}% (${minHumedad?.first}:00)\n")
                            append("Máximo: ${maxHumedad?.second}% (${maxHumedad?.first}:00)\n\n")
                            append("Datos por hora:\n")
                            humedadPorHora.forEach { (hora, hum) ->
                                append("%02d:00 → %d%%\n".format(hora, hum))
                            }
                        }

                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, report)
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Reporte EcoRiego")
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Compartir registro"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
                ) {
                    Text("📤 Compartir registro", color = Color.White)
                }
            }
        }
    }
}