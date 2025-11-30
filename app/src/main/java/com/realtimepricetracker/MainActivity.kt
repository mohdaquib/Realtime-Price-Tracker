package com.realtimepricetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.realtimepricetracker.network.WebsocketManager
import com.realtimepricetracker.ui.PriceTrackerScreen
import com.realtimepricetracker.ui.PriceTrackerViewModel
import com.realtimepricetracker.ui.theme.RealtimePriceTrackerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val websocketManager = WebsocketManager(CoroutineScope(SupervisorJob()))
        setContent {
            RealtimePriceTrackerTheme {
                val viewModel: PriceTrackerViewModel = viewModel { PriceTrackerViewModel(websocketManager) }
                PriceTrackerScreen(viewModel = viewModel)
            }
        }
    }
}