package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ChargingWorkScheduler {
    private const val NAME = "ChargeAlert_PeriodicChargingWorker"
    private fun manager(context: Context): WorkManager? = try {
        WorkManager.getInstance(context)
    } catch (_: IllegalStateException) {
        // Startup providers may not have initialized yet; the next UI/service sync retries.
        null
    }
    fun schedule(context: Context) {
        manager(context)?.enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ChargingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiresCharging(true).build()).build())
    }
    fun cancel(context: Context) {
        manager(context)?.cancelUniqueWork(NAME)
    }
    fun migrateLegacyWork(context: Context) {
        manager(context)?.cancelUniqueWork("ChargeAlert_OneTimeChargingWorker")
        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? android.app.job.JobScheduler)?.cancel(8844)
    }
}
