package com.realtimepricetracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.realtimepricetracker.data.notification.NotificationHelper
import com.realtimepricetracker.data.worker.AlertCheckWorker
import com.realtimepricetracker.di.AppFactory
import com.realtimepricetracker.presentation.ui.PriceTrackerScreen
import com.realtimepricetracker.presentation.viewmodel.PriceTrackerViewModel
import com.realtimepricetracker.presentation.viewmodel.PriceTrackerViewModelFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppFactory.init(applicationContext)

        NotificationHelper(applicationContext).createChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        scheduleAlertWorker()

        enableEdgeToEdge()
        setContent {
            val viewModel: PriceTrackerViewModel = viewModel(
                factory = PriceTrackerViewModelFactory()
            )
            PriceTrackerScreen(viewModel = viewModel)
        }
    }

    private fun scheduleAlertWorker() {
        val request = PeriodicWorkRequestBuilder<AlertCheckWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "price_alert_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
