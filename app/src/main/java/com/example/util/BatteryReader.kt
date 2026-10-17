package com.example.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.model.BatteryInfo
import kotlin.math.round

object BatteryReader {
    fun percentage(level: Int, scale: Int): Int? =
        if (scale > 0 && level in 0..scale) (level.toLong() * 100 / scale).toInt() else null

    fun currentMilliamps(microamps: Int): Int? =
        if (microamps == Int.MIN_VALUE) null else microamps / 1000

    fun read(context: Context, target: Int): BatteryInfo? =
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let { fromIntent(context, it, target) }

    fun fromIntent(context: Context, intent: Intent, target: Int): BatteryInfo? {
        val level = percentage(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
            intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)) ?: return null
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val connected = plugged > 0
        val charging = connected && status == BatteryManager.BATTERY_STATUS_CHARGING
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val current = try { manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)?.let(::currentMilliamps) }
            catch (_: Exception) { null }
        // Net battery power, not adapter output. Negative current is discharge even while plugged in.
        val watts = if (charging && current != null && current > 0 && voltage > 0)
            round(voltage / 1000f * (current / 1000f) * 10) / 10 else 0f
        val fullTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && charging)
            try { manager?.computeChargeTimeRemaining() ?: -1 } catch (_: Exception) { -1 } else -1
        val minutes = if (fullTime > 0 && level < target && level < 100)
            (fullTime / 60000.0 * (target - level) / (100 - level)).toInt().coerceAtLeast(1) else -1
        return BatteryInfo(
            level = level, isCharging = charging, isPowerConnected = connected,
            plugType = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                8 -> "Dock"
                else -> "Not Charging"
            },
            voltage = voltage,
            temperature = if (intent.hasExtra(BatteryManager.EXTRA_TEMPERATURE))
                intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f else Float.NaN,
            health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good Health"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            },
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown",
            currentNowMa = current ?: 0, hasCurrentReading = current != null,
            wattage = watts, estimatedMinutesRemaining = minutes,
            chargingSpeedCategory = when {
                status == BatteryManager.BATTERY_STATUS_FULL && connected -> "Full"
                charging -> "Charging"
                connected -> "Charging Paused"
                else -> "Discharging"
            }
        )
    }
}
