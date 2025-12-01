package com.realtimepricetracker.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.graphics.Color
import com.realtimepricetracker.MainDispatcherRule
import com.realtimepricetracker.data.PriceUpdate
import com.realtimepricetracker.network.WebsocketManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PriceTrackerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var viewmodel: PriceTrackerViewModel
    private lateinit var mockWsManager: WebsocketManager

    @Before
    fun setUp() {
        mockWsManager = mockk(relaxed = true)
        every { mockWsManager.receivedMessages } returns MutableSharedFlow()
        every { mockWsManager.connectionState } returns MutableStateFlow(false)
        viewmodel = PriceTrackerViewModel(websocketManager = mockWsManager)
    }

    @Test
    fun `initial state has symbols sorted by price descending`() = runTest {
        val state = viewmodel.uiState.value
        val prices = state.symbols.map { it.price }

        assertEquals(prices.sortedDescending(), prices)
        assertEquals(25, state.symbols.size)
        assertFalse(state.isConnected)
        assertFalse(state.isRunning)
    }

    @Test
    fun `toggleFeed starts feed and connects WebSocket`() = runTest {
        viewmodel.toggleFeed()

        val state = viewmodel.uiState.value
        verify { mockWsManager.connect() }
        assertTrue(state.isRunning)
    }

    @Test
    fun `updateStockData updates price, change, flash, and resorts list`() = runTest {
        val update = PriceUpdate("AAPL", 200.0, 10.0)
        viewmodel.updateStockData(update)

        var state = viewmodel.uiState.value
        var aaplStock = state.symbols.find { it.symbol == "AAPL" }
        assertEquals(Color.Green.value, aaplStock?.flashColor?.value)

        advanceTimeBy(1000)
        advanceUntilIdle()

        state = viewmodel.uiState.value
        aaplStock = state.symbols.find { it.symbol == "AAPL" }
        assertNull(aaplStock?.flashColor)
    }

    @Test
    fun `negative change sets red flash`() = runTest {
        val update = PriceUpdate("GOOG", 150.0, -5.0)
        viewmodel.updateStockData(update)

        val state = viewmodel.uiState.value
        val googStock = state.symbols.find { it.symbol == "GOOG" }

        assertEquals(Color.Red.value, googStock?.flashColor?.value)
    }
}
