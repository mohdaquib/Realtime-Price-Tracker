# Clean Architecture Implementation Guide

## Overview

This document describes the clean architecture implementation in the Realtime Price Tracker app, providing detailed explanations for each layer and their interactions.

## Table of Contents

1. [Architecture Principles](#architecture-principles)
2. [Layer Details](#layer-details)
3. [Dependency Flow](#dependency-flow)
4. [Adding New Features](#adding-new-features)
5. [Testing Guide](#testing-guide)

## Architecture Principles

### Separation of Concerns
Each layer has a specific responsibility:
- **Domain**: Business logic
- **Data**: Data sources and repository implementations
- **Presentation**: UI logic and state management

### Dependency Rule
- Inner layers (Domain) never depend on outer layers (Data, Presentation)
- Outer layers depend on inner layers through interfaces
- This creates a unidirectional dependency graph

### Testability
Each layer can be tested independently:
- Mock repositories for use case testing
- Mock data sources for repository testing
- Compose testing for UI testing

## Layer Details

### Domain Layer

**Location**: `domain/`

**Responsibility**: Pure business logic and domain models

**Key Components**:

#### Entities
```kotlin
// domain/entities/Stock.kt
data class Stock(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercentage: Double = 0.0
)
```

- Represent core business concepts
- Must be independent of Android/UI frameworks
- Immutable data classes

#### Repositories (Interfaces)
```kotlin
// domain/repositories/PriceRepository.kt
interface PriceRepository {
    suspend fun getStocks(symbols: List<String>): Result<List<Stock>>
    fun subscribeToPriceUpdates(): Flow<Result<Stock>>
    suspend fun sendPriceUpdate(stock: Stock): Result<Unit>
}
```

- Define contracts for data operations
- Implementation details hidden from domain
- Enable easy mocking for tests

#### Use Cases
```kotlin
// domain/usecases/GetInitialStocksUseCase.kt
class GetInitialStocksUseCase(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(): Result<List<Stock>> {
        return priceRepository.getStocks(DomainConstants.STOCK_SYMBOLS)
    }
}
```

- Single responsibility principle
- Orchestrate repository methods
- Handle domain-specific business logic
- Can be composed together

#### Constants
```kotlin
// domain/config/Constants.kt
object DomainConstants {
    val STOCK_SYMBOLS = listOf("AAPL", "GOOG", "TSLA", ...)
}
```

- Domain-level configuration
- Independent of implementation details

### Data Layer

**Location**: `data/`

**Responsibility**: Implement repositories and manage data sources

**Key Components**:

#### Data Sources
```kotlin
// data/datasource/WebSocketDataSource.kt
class WebSocketDataSource(private val scope: CoroutineScope) {
    fun connect() { ... }
    fun send(message: String) { ... }
    fun disconnect() { ... }
    val connectionState: StateFlow<Boolean> = ...
    val receivedMessages: SharedFlow<String> = ...
}
```

- Low-level network/database operations
- Handle platform-specific concerns
- Provide reactive streams (Flow, StateFlow)

#### DTOs (Data Transfer Objects)
```kotlin
// data/dto/PriceUpdateDto.kt
data class PriceUpdateDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: Double,
    @SerializedName("change") val change: Double
) {
    fun toDomain(): Stock = Stock(...)
}
```

- Deserialize external data
- Provide mapper functions to domain entities
- Handle serialization/deserialization

#### Repository Implementations
```kotlin
// data/repositories/PriceRepositoryImpl.kt
class PriceRepositoryImpl(
    private val webSocketDataSource: WebSocketDataSource,
    private val gson: Gson
) : PriceRepository {
    
    override suspend fun getStocks(symbols: List<String>): Result<List<Stock>> {
        return try {
            val stocks = symbols.map { ... }
            Result.success(stocks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun subscribeToPriceUpdates(): Flow<Result<Stock>> {
        return webSocketDataSource.receivedMessages.map { message ->
            try {
                val dto = gson.fromJson(message, PriceUpdateDto::class.java)
                Result.success(dto.toDomain())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

- Implement domain repository interfaces
- Coordinate multiple data sources
- Handle error mapping
- Transform DTOs to domain entities

### Presentation Layer

**Location**: `presentation/`

**Responsibility**: UI state management and user interaction

**Key Components**:

#### UI State Models
```kotlin
// presentation/state/PriceTrackerUiState.kt
@Stable
data class PriceTrackerUiState(
    val stocks: List<StockUiModel> = emptyList(),
    val isConnected: Boolean = false,
    val isRunning: Boolean = false,
    val isDarkMode: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)
```

- Immutable data classes (use `@Stable` for Compose optimization)
- Represents complete UI state
- Single source of truth for UI

#### ViewModel
```kotlin
// presentation/viewmodel/PriceTrackerViewModel.kt
class PriceTrackerViewModel(
    private val getInitialStocksUseCase: GetInitialStocksUseCase,
    private val subscribeToPriceUpdatesUseCase: SubscribeToPriceUpdatesUseCase,
    private val sendPriceUpdateUseCase: SendPriceUpdateUseCase,
    private val manageConnectionUseCase: ManageConnectionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PriceTrackerUiState())
    val uiState: StateFlow<PriceTrackerUiState> = _uiState.asStateFlow()
    
    fun toggleFeed() { ... }
    fun toggleDarkMode() { ... }
}
```

- Hold reactive UI state with StateFlow
- Call use cases based on user actions
- Transform domain models to UI models
- Manage lifecycle

#### Composable Screens
```kotlin
// presentation/ui/PriceTrackerScreen.kt
@Composable
fun PriceTrackerScreen(viewModel: PriceTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    RealtimePriceTrackerTheme(darkTheme = uiState.isDarkMode) {
        Scaffold(
            topBar = { TopBar(uiState, viewModel::toggleFeed, viewModel::toggleDarkMode) }
        ) { padding ->
            PriceTrackerScreenContent(uiState = uiState, modifier = Modifier.padding(padding))
        }
    }
}
```

- Compose functions for UI rendering
- Read from StateFlow
- Trigger ViewModel actions on user events

## Dependency Flow

### Initialization Chain

```
MainActivity
    ↓
ViewModelFactory.create()
    ↓
ServiceLocator.getGetInitialStocksUseCase()
ServiceLocator.getSubscribeToPriceUpdatesUseCase()
ServiceLocator.getSendPriceUpdateUseCase()
ServiceLocator.getManageConnectionUseCase()
    ↓
Use Cases (receive repositories from ServiceLocator)
    ↓
Repositories (receive data sources from ServiceLocator)
    ↓
Data Sources (initialized with CoroutineScope)
```

### Runtime Data Flow (Example: Toggling Feed)

```
UI Event: User clicks "Start" button
    ↓
ViewModel.toggleFeed()
    ↓
ManageConnectionUseCase.connect()
    ↓
ConnectionRepositoryImpl.connect()
    ↓
WebSocketDataSource.connect()
    ↓
OkHttpClient.newWebSocket()
    ↓
WebSocket connected, _connectionState.value = true
    ↓
StateFlow emits new value
    ↓
ViewModel collects: _uiState.update { it.copy(isConnected = true) }
    ↓
UI observes new state via collectAsState()
    ↓
Compose recomposes with updated UI
```

## Adding New Features

### Step-by-Step Guide

#### 1. Define Domain Entity
```kotlin
// domain/entities/Portfolio.kt
data class Portfolio(
    val id: String,
    val name: String,
    val totalValue: Double,
    val stocks: List<Stock>
)
```

#### 2. Create Repository Interface
```kotlin
// domain/repositories/PortfolioRepository.kt
interface PortfolioRepository {
    suspend fun getPortfolios(): Result<List<Portfolio>>
    suspend fun createPortfolio(name: String): Result<Portfolio>
    suspend fun addStockToPortfolio(portfolioId: String, stock: Stock): Result<Unit>
}
```

#### 3. Create Use Cases
```kotlin
// domain/usecases/GetPortfoliosUseCase.kt
class GetPortfoliosUseCase(private val repository: PortfolioRepository) {
    suspend operator fun invoke(): Result<List<Portfolio>> {
        return repository.getPortfolios()
    }
}
```

#### 4. Create DTOs (if needed)
```kotlin
// data/dto/PortfolioDto.kt
data class PortfolioDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("totalValue") val totalValue: Double
) {
    fun toDomain(): Portfolio = Portfolio(...)
}
```

#### 5. Implement Repository
```kotlin
// data/repositories/PortfolioRepositoryImpl.kt
class PortfolioRepositoryImpl(
    private val dataSource: PortfolioDataSource
) : PortfolioRepository {
    override suspend fun getPortfolios(): Result<List<Portfolio>> {
        return try {
            val dtos = dataSource.fetchPortfolios()
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 6. Update ViewModel
```kotlin
// presentation/viewmodel/PortfolioViewModel.kt
class PortfolioViewModel(
    private val getPortfoliosUseCase: GetPortfoliosUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()
    
    fun loadPortfolios() {
        viewModelScope.launch {
            val result = getPortfoliosUseCase()
            result.onSuccess { portfolios ->
                _uiState.update { it.copy(portfolios = portfolios) }
            }
        }
    }
}
```

#### 7. Create UI
```kotlin
// presentation/ui/PortfolioScreen.kt
@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // Render UI with state
}
```

#### 8. Update ServiceLocator
```kotlin
// di/ServiceLocator.kt
private val portfolioRepository: PortfolioRepository by lazy {
    PortfolioRepositoryImpl(portfolioDataSource)
}

private val getPortfoliosUseCase by lazy {
    GetPortfoliosUseCase(portfolioRepository)
}
```

## Testing Guide

### Unit Testing a Use Case

```kotlin
@Test
fun testGetInitialStocks() = runTest {
    // Arrange
    val mockRepository = mockk<PriceRepository>()
    val expectedStocks = listOf(
        Stock(symbol = "AAPL", price = 100.0, change = 0.0),
        Stock(symbol = "GOOG", price = 105.0, change = 1.0)
    )
    coEvery { 
        mockRepository.getStocks(any()) 
    } returns Result.success(expectedStocks)
    
    val useCase = GetInitialStocksUseCase(mockRepository)
    
    // Act
    val result = useCase()
    
    // Assert
    assertTrue(result.isSuccess)
    assertEquals(2, result.getOrNull()?.size)
}
```

### Unit Testing a Repository

```kotlin
@Test
fun testPriceRepositoryMapsDtoToDomain() = runTest {
    // Arrange
    val mockDataSource = mockk<WebSocketDataSource>()
    val gson = Gson()
    val testMessage = """{"symbol":"AAPL","price":150.0,"change":5.0}"""
    
    val flow = flowOf(testMessage)
    every { mockDataSource.receivedMessages } returns flow
    
    val repository = PriceRepositoryImpl(mockDataSource, gson)
    
    // Act
    val result = mutableListOf<Result<Stock>>()
    repository.subscribeToPriceUpdates().collect { result.add(it) }
    
    // Assert
    assertEquals(1, result.size)
    assertTrue(result[0].isSuccess)
    assertEquals("AAPL", result[0].getOrNull()?.symbol)
}
```

### UI Testing with Compose

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testPriceTrackerScreenDisplaysStocks() {
    val mockViewModel = mockk<PriceTrackerViewModel>()
    val uiState = PriceTrackerUiState(
        stocks = listOf(
            StockUiModel("AAPL", 100.0, 0.5),
            StockUiModel("GOOG", 105.0, -0.5)
        )
    )
    every { mockViewModel.uiState } returns MutableStateFlow(uiState)
    
    composeTestRule.setContent {
        PriceTrackerScreen(mockViewModel)
    }
    
    composeTestRule.onNodeWithText("AAPL").assertIsDisplayed()
    composeTestRule.onNodeWithText("100.00").assertIsDisplayed()
}
```

## Best Practices

1. **Keep Domain Layer Pure**
   - No Android imports
   - No external libraries except Kotlin stdlib
   - No coroutines (use interfaces for async operations)

2. **Use Result<T> for Error Handling**
   - Type-safe error handling
   - Avoids checked exceptions
   - Composable error paths

3. **Make Use Cases Reusable**
   - Single responsibility
   - Composable with other use cases
   - Testable in isolation

4. **Use @Stable for UI Models**
   - Helps Compose optimization
   - Prevents unnecessary recompositions

5. **Inject Dependencies**
   - Never use singletons directly
   - Pass dependencies through constructors
   - Enable easy mocking

6. **Document Public APIs**
   - KDoc for use cases and repositories
   - Comments explaining complex logic

## Migration Path

### From Old Structure to New

**Old Structure**:
```
MainActivity
  ├─ WebsocketManager (hardcoded)
  └─ PriceTrackerViewModel
      └─ Direct WebSocket calls
```

**New Structure**:
```
MainActivity
  ├─ ViewModelFactory
  └─ ServiceLocator
      ├─ Use Cases
      │   └─ Repositories (interfaces)
      │       └─ Data Sources
      └─ ViewModel (receives dependencies)
```

### Gradual Migration

1. Keep old code working while introducing new structure
2. Create domain layer first (doesn't depend on anything)
3. Create data layer implementations
4. Update ViewModel to use use cases
5. Update UI to use new ViewModel
6. Remove old code

## Conclusion

This architecture provides:
- ✅ Clear separation of concerns
- ✅ Easy to test (each layer independently)
- ✅ Easy to maintain (changes isolated to layers)
- ✅ Easy to extend (add new features without modifying existing)
- ✅ Team-friendly (clear contracts between layers)

For questions or issues, refer to the main README.md or create an issue on GitHub.

