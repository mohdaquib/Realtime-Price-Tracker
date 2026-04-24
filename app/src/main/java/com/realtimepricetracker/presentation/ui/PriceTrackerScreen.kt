package com.realtimepricetracker.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.realtimepricetracker.presentation.state.AppTab
import com.realtimepricetracker.presentation.state.PriceTrackerUiState
import com.realtimepricetracker.presentation.state.StockUiModel
import com.realtimepricetracker.presentation.viewmodel.PriceTrackerViewModel
import com.realtimepricetracker.ui.theme.ErrorRed
import com.realtimepricetracker.ui.theme.RealtimePriceTrackerTheme
import com.realtimepricetracker.ui.theme.SuccessGreen
import com.realtimepricetracker.ui.theme.WarningOrange

@Composable
fun PriceTrackerScreen(viewModel: PriceTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    RealtimePriceTrackerTheme(darkTheme = uiState.isDarkMode) {
        Scaffold(
            topBar = {
                TopBar(
                    uiState = uiState,
                    onToggleFeed = { viewModel.toggleFeed() },
                    onToggleDarkMode = { viewModel.toggleDarkMode() }
                )
            },
        ) { padding ->
            PriceTrackerScreenContent(
                uiState = uiState,
                onStockSelected = { viewModel.selectStock(it) },
                onChartDismiss = { viewModel.selectStock(null) },
                onWatchlistToggle = { viewModel.toggleWatchlist(it) },
                onTabSelected = { viewModel.setActiveTab(it) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun PriceTrackerScreenContent(
    uiState: PriceTrackerUiState,
    onStockSelected: (String) -> Unit = {},
    onChartDismiss: () -> Unit = {},
    onWatchlistToggle: (String) -> Unit = {},
    onTabSelected: (AppTab) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = uiState.activeTab.ordinal) {
            Tab(
                selected = uiState.activeTab == AppTab.MARKETS,
                onClick = { onTabSelected(AppTab.MARKETS) },
                text = { Text("Markets") }
            )
            Tab(
                selected = uiState.activeTab == AppTab.WATCHLIST,
                onClick = { onTabSelected(AppTab.WATCHLIST) },
                text = { Text("Watchlist") }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.loading && uiState.stocks.isEmpty() -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = "Error: ${uiState.error}", color = ErrorRed)
                }
                else -> {
                    val displayedStocks = when (uiState.activeTab) {
                        AppTab.MARKETS -> uiState.stocks
                        AppTab.WATCHLIST -> uiState.stocks.filter { it.symbol in uiState.watchlist }
                    }

                    if (displayedStocks.isEmpty() && uiState.activeTab == AppTab.WATCHLIST) {
                        WatchlistEmptyState()
                    } else if (displayedStocks.isEmpty()) {
                        Text(text = "No data available", color = Color.Gray)
                    } else {
                        PriceList(
                            stocks = displayedStocks,
                            watchlist = uiState.watchlist,
                            selectedSymbol = uiState.selectedSymbol,
                            onStockClick = onStockSelected,
                            onWatchlistToggle = onWatchlistToggle
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.selectedSymbol != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            val selectedStock = uiState.stocks.find { it.symbol == uiState.selectedSymbol }
            if (selectedStock != null) {
                StockChartPanel(stock = selectedStock, onDismiss = onChartDismiss)
            }
        }
    }
}

@Composable
private fun WatchlistEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Your watchlist is empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap ★ next to any stock to track it",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PriceList(
    stocks: List<StockUiModel>,
    watchlist: Set<String>,
    selectedSymbol: String?,
    onStockClick: (String) -> Unit,
    onWatchlistToggle: (String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(stocks, key = { it.symbol }) { stock ->
            StockRow(
                stock = stock,
                isSelected = stock.symbol == selectedSymbol,
                isWatched = stock.symbol in watchlist,
                onClick = { onStockClick(stock.symbol) },
                onWatchlistToggle = { onWatchlistToggle(stock.symbol) }
            )
        }
    }
}

@Composable
private fun StockChartPanel(stock: StockUiModel, onDismiss: () -> Unit) {
    val lineColor = if (stock.change >= 0) SuccessGreen else ErrorRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${"%.2f".format(stock.price)}  ${if (stock.change >= 0) "+" else ""}${"%.2f".format(stock.change)} (${"%.2f".format(stock.changePercentage)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = lineColor
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close chart",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (stock.priceHistory.size >= 2) {
                PriceLineChart(
                    prices = stock.priceHistory,
                    lineColor = lineColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Waiting for price data…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    uiState: PriceTrackerUiState,
    onToggleFeed: () -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    TopAppBar(
        title = { Text("Price Tracker") },
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Connection status",
                tint = if (uiState.isConnected) SuccessGreen else ErrorRed,
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        actions = {
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle dark mode"
                )
            }
            Button(onClick = onToggleFeed) {
                Text(if (uiState.isRunning) "Stop" else "Start")
            }
        },
    )
}

@Composable
fun StockRow(
    stock: StockUiModel,
    isSelected: Boolean = false,
    isWatched: Boolean = false,
    onClick: () -> Unit = {},
    onWatchlistToggle: () -> Unit = {},
) {
    val targetColor = when {
        stock.flashColor != null -> stock.flashColor
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else -> Color.Transparent
    }
    val backgroundColor by animateColorAsState(targetColor, label = "row_bg")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stock.symbol, modifier = Modifier.weight(1f))
        Text(text = "%.2f".format(stock.price), modifier = Modifier.weight(1f))
        Text(
            text = "${if (stock.change > 0) "+" else ""}%.2f".format(stock.change),
            color = if (stock.change > 0) SuccessGreen else ErrorRed,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (stock.change > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            contentDescription = "Price trend",
            tint = if (stock.change > 0) SuccessGreen else ErrorRed,
        )
        IconButton(
            onClick = onWatchlistToggle,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isWatched) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (isWatched) "Remove from watchlist" else "Add to watchlist",
                tint = if (isWatched) WarningOrange else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Previews
@Preview
@Composable
private fun TopBarPreview() {
    RealtimePriceTrackerTheme {
        TopBar(
            uiState = PriceTrackerUiState(isConnected = true, isRunning = true),
            onToggleFeed = {},
            onToggleDarkMode = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StockRowPreview() {
    RealtimePriceTrackerTheme {
        Column {
            StockRow(
                stock = StockUiModel("AAPL", 182.50, 0.5, 0.27),
                isWatched = true
            )
            StockRow(
                stock = StockUiModel("TSLA", 245.10, -3.2, -1.29),
                isWatched = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WatchlistEmptyStatePreview() {
    RealtimePriceTrackerTheme {
        WatchlistEmptyState()
    }
}

@Preview(showBackground = true)
@Composable
private fun MarketsTabPreview() {
    val mockHistory = listOf(98f, 99f, 100f, 101f, 100.5f, 102f, 101.5f, 103f)
    RealtimePriceTrackerTheme {
        PriceTrackerScreenContent(
            uiState = PriceTrackerUiState(
                stocks = listOf(
                    StockUiModel("AAPL", 182.50, 0.5, 0.27, priceHistory = mockHistory),
                    StockUiModel("GOOG", 175.30, -1.2, -0.68),
                    StockUiModel("TSLA", 245.10, 4.5, 1.87),
                ),
                watchlist = setOf("AAPL"),
                activeTab = AppTab.MARKETS,
                isConnected = true,
                isRunning = true,
                selectedSymbol = "AAPL"
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WatchlistTabPreview() {
    RealtimePriceTrackerTheme {
        PriceTrackerScreenContent(
            uiState = PriceTrackerUiState(
                stocks = listOf(
                    StockUiModel("AAPL", 182.50, 0.5, 0.27),
                    StockUiModel("GOOG", 175.30, -1.2, -0.68),
                ),
                watchlist = setOf("AAPL"),
                activeTab = AppTab.WATCHLIST,
                isConnected = true,
            ),
        )
    }
}
