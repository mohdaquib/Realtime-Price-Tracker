package com.realtimepricetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.realtimepricetracker.presentation.ui.PriceTrackerScreen
import com.realtimepricetracker.presentation.viewmodel.PriceTrackerViewModel
import com.realtimepricetracker.presentation.viewmodel.PriceTrackerViewModelFactory

/**
 * Main activity for the Realtime Price Tracker application.
 * Initializes the dependency graph through ServiceLocator and sets up the UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PriceTrackerViewModel = viewModel(
                factory = PriceTrackerViewModelFactory()
            )
            PriceTrackerScreen(viewModel = viewModel)
        }
    }
}