package com.realtimepricetracker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PriceTrackerScreen(viewModel: PriceTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { TopBar(uiState, viewModel::toggleFeed) },
    ) { padding ->
        PriceTrackScreenContent(uiState = uiState, modifier = Modifier.padding(padding))
    }
}

@Composable
fun PriceTrackScreenContent(
    uiState: UiState,
    modifier: Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(uiState.symbols, key = { it.symbol }) { stock ->
            StockRow(stock)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    uiState: UiState,
    onToggle: () -> Unit,
) {
    TopAppBar(
        title = { Text("Price Tracker") },
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Connection",
                tint = if (uiState.isConnected) Color.Green else Color.Red,
            )
        },
        actions = {
            Button(onClick = onToggle) {
                Text(if (uiState.isRunning) "Stop" else "Start")
            }
        },
    )
}

@Composable
fun StockRow(stock: StockData) {
    val backgroundColor by animateColorAsState(stock.flashColor ?: Color.Transparent)
    Row(
        modifier = Modifier.fillMaxWidth().background(backgroundColor).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stock.symbol)
        Text("%.2f".format(stock.price))
        Icon(
            imageVector = if (stock.change > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            contentDescription = "Change",
            tint = if (stock.change > 0) Color.Green else Color.Red,
        )
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    TopBar(
        uiState =
            UiState(
                symbols = emptyList(),
                isConnected = true,
                isRunning = true,
            ),
        onToggle = {},
    )
}

@Preview
@Composable
private fun StockRowPreview() {
    StockRow(
        stock =
            StockData(
                symbol = "AAPL",
                price = 100.00,
                change = 0.5,
                flashColor = Color.Green,
            ),
    )
}

@Preview
@Composable
private fun PriceTrackerScreenContentPreview() {
    PriceTrackScreenContent(
        uiState =
            UiState(
                symbols =
                    listOf(
                        StockData(symbol = "AAPL", price = 100.00, change = 0.5, flashColor = Color.Green),
                        StockData(symbol = "GOOG", price = 105.00, change = -0.5, flashColor = Color.Red),
                        StockData(symbol = "TSLA", price = 110.00, change = 4.5, flashColor = Color.Green),
                        StockData(symbol = "AMZN", price = 120.00, change = -3.5, flashColor = Color.Red),
                    ),
                isConnected = true,
                isRunning = true,
            ),
        modifier = Modifier.fillMaxSize(),
    )
}
