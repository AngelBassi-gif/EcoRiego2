package com.example.ecoriego.Data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.FirebaseDatabase

class SecurePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    // 🔥 CORREGIDO: ahora usa la misma referencia que ConfigScreen
    private val firebaseRef = FirebaseDatabase.getInstance().getReference("config")

    // -----------------------------
    //  SECCIÓN: SESIÓN (Firebase)
    // -----------------------------
    fun saveLoginState(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("logged_in", isLoggedIn).apply()
        firebaseRef.child("logged_in").setValue(isLoggedIn)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("logged_in", false)
    }

    fun logout() {
        saveLoginState(false)
    }

    // -----------------------------
    //  SECCIÓN: CONFIGURACIÓN SISTEMA
    // -----------------------------
    fun saveRiegoAutomatico(enabled: Boolean) {
        prefs.edit().putBoolean("riego_automatico", enabled).apply()
        firebaseRef.child("riego_automatico").setValue(enabled)
    }

    fun isRiegoAutomaticoEnabled(): Boolean {
        return prefs.getBoolean("riego_automatico", false)
    }

    fun saveHumedadMinima(humedad: Int) {
        prefs.edit().putInt("humedad_minima", humedad).apply()
        firebaseRef.child("humedad_minima").setValue(humedad)
    }

    fun getHumedadMinima(): Int {
        return prefs.getInt("humedad_minima", 30)
    }

    fun saveIntervaloRiego(minutes: Int) {
        prefs.edit().putInt("intervalo_riego", minutes).apply()
        firebaseRef.child("intervalo_riego").setValue(minutes)
    }

    fun getIntervaloRiego(): Int {
        return prefs.getInt("intervalo_riego", 10)
    }

    fun saveIntervaloActualizacionSegundos(seconds: Int) {
        prefs.edit().putInt("intervalo_actualizacion", seconds).apply()
        firebaseRef.child("intervalo_actualizacion").setValue(seconds)
    }

    fun getIntervaloActualizacionSegundos(): Int {
        return prefs.getInt("intervalo_actualizacion", 5)
    }

    // -----------------------------
    //  SECCIÓN: MONITOREO DE ESTABILIDAD
    // -----------------------------
    fun saveStabilityMonitoring(enabled: Boolean) {
        prefs.edit().putBoolean("stability_monitoring", enabled).apply()
        firebaseRef.child("stability_monitoring").setValue(enabled)
    }

    fun isStabilityMonitoringEnabled(): Boolean {
        return prefs.getBoolean("stability_monitoring", false)
    }

    fun saveLatencyThreshold(ms: Int) {
        prefs.edit().putInt("latency_threshold", ms).apply()
        firebaseRef.child("latency_threshold").setValue(ms)
    }

    fun getLatencyThreshold(): Int {
        return prefs.getInt("latency_threshold", 500)
    }

    // -----------------------------
    //  RESET GENERAL
    // -----------------------------
    fun clearAll() {
        prefs.edit().clear().apply()
        firebaseRef.removeValue()
    }
}
