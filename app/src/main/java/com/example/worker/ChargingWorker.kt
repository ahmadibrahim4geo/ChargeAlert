package com.example.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.util.MonitoringController

/** Inexact recovery only. Presentation belongs exclusively to the monitoring service. */
class ChargingWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        MonitoringController.sync(applicationContext)
        return Result.success()
    }
}
