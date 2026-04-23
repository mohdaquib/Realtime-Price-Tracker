# Realtime Price Tracker 📊

A modern Android application built with **Clean Architecture** and **Jetpack Compose** for real-time stock price tracking and updates.

## 🎯 Overview

Realtime Price Tracker is a demonstration of best practices in Android development, featuring:
- **Clean Architecture** with clear separation of concerns
- **MVVM** pattern for UI state management
- **StateFlow** for reactive state management
- **Modern Compose UI** with dark theme support
- **WebSocket integration** for real-time price updates
- **Comprehensive error handling** with Result wrappers

## 🏗️ Architecture

### Layered Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER (UI)                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  PriceTrackerScreen (Composables)                   │   │
│  │  ├─ TopBar (with connection status)                 │   │
│  │  ├─ StockRow (individual price items)               │   │
│  │  └─ Theming (Light/Dark mode)                       │   │
│  └──────────────────────────────────────────────────────┘   │
│            ▲ StateFlow<PriceTrackerUiState>                 │
└────────────┼─────────────────────────────────────────────────┘
             │
┌────────────┼─────────────────────────────────────────────────┐
│            │       PRESENTATION LAYER (ViewModel)            │
│  ┌─────────▼──────────────────────────────────────────────┐  │
│  │  PriceTrackerViewModel                               │  │
│  │  ├─ Manages UI state (StockList, Connection)         │  │
│  │  ├─ Orchestrates use cases                           │  │
│  │  ├─ Handles lifecycle                                │  │
│  │  └─ Toggles dark mode                                │  │
│  └────────────────────────────────────────────────────────┘  │
│                 ▲ Calls                                      │
└─────────────────┼──────────────────────────────────────────────┘
                  │
┌─────────────────┼──────────────────────────────────────────────┐
│                 │        DOMAIN LAYER                         │
│  ┌──────────────▼────────────────────────────────────────┐   │
│  │  Use Cases                                           │   │
│  │  ├─ GetInitialStocksUseCase                          │   │
│  │  ├─ SubscribeToPriceUpdatesUseCase                  │   │
│  │  ├─ SendPriceUpdateUseCase                          │   │
│  │  └─ ManageConnectionUseCase                         │   │
│  └────────────────┬────────────────────────────────────┘   │
│                   │                                          │
│  ┌────────────────▼────────────────────────────────────┐   │
│  │  Domain Models & Repositories (Interfaces)         │   │
│  │  ├─ Stock (Entity)                                 │   │
│  │  ├─ PriceRepository (Interface)                    │   │
│  │  ├─ ConnectionRepository (Interface)               │   │
│  │  └─ DomainConstants                                │   │
│  └────────────────┬────────────────────────────────────┘   │
└────────────────────┼──────────────────────────────────────────┘
                     │
┌────────────────────┼──────────────────────────────────────────┐
│                    │      DATA LAYER                          │
│  ┌─────────────────▼───────────────────────────────────┐    │
│  │  Repositories (Implementations)                    │    │
│  │  ├─ PriceRepositoryImpl                            │    │
│  │  └─ ConnectionRepositoryImpl                       │    │
│  └─────────────────┬───────────────────────────────────┘    │
│                    │                                         │
│  ┌─────────────────▼───────────────────────────────────┐    │
│  │  Data Sources                                      │    │
│  │  └─ WebSocketDataSource (OkHttp WebSocket)        │    │
│  └─────────────────┬───────────────────────────────────┘    │
│                    │                                         │
│  ┌─────────────────▼───────────────────────────────────┐    │
│  │  DTOs & Mappers                                    │    │
│  │  ├─ PriceUpdateDto                                │    │
│  │  └─ toDomain() mapping                            │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### 🎭 **Presentation Layer** (`presentation/`)
- **Purpose**: Handle UI rendering and user interaction
- **Components**:
  - `PriceTrackerScreen`: Main composable screen
  - `PriceTrackerViewModel`: State management and business logic coordination
  - `PriceTrackerUiState`: UI state holder
  - `StockUiModel`: UI representation of stock data

#### 🎯 **Domain Layer** (`domain/`)
- **Purpose**: Contain core business logic and entities (framework-independent)
- **Components**:
  - **Entities**: `Stock` - core business model
  - **Repositories (Interfaces)**: `PriceRepository`, `ConnectionRepository`
  - **Use Cases**: Single-responsibility classes orchestrating domain logic
    - `GetInitialStocksUseCase`: Fetch stocks
    - `SubscribeToPriceUpdatesUseCase`: Subscribe to real-time updates
    - `SendPriceUpdateUseCase`: Send updates
    - `ManageConnectionUseCase`: Handle connection lifecycle
  - **Constants**: `DomainConstants`

#### 💾 **Data Layer** (`data/`)
- **Purpose**: Implement repository interfaces and manage external data sources
- **Components**:
  - **Repositories (Implementations)**: Concrete implementations of domain repositories
  - **Data Sources**: Low-level network operations (`WebSocketDataSource`)
  - **DTOs**: Data transfer objects with mapping functions to domain entities
  - **Constants**: Data layer configuration

#### 🔌 **Dependency Injection** (`di/`)
- **ServiceLocator**: Singleton pattern for dependency management
- Provides instances of repositories, use cases, and data sources

## 📦 Package Structure

```
app/src/main/java/com/realtimepricetracker/
├── di/
│   └── ServiceLocator.kt              # Dependency container
├── domain/
│   ├── config/
│   │   └── Constants.kt               # Domain-level constants
│   ├── entities/
│   │   └── Stock.kt                   # Core business entity
│   ├── repositories/
│   │   ├── PriceRepository.kt        # Price operations interface
│   │   └── ConnectionRepository.kt    # Connection lifecycle interface
│   └── usecases/
│       ├── GetInitialStocksUseCase.kt
│       ├── SubscribeToPriceUpdatesUseCase.kt
│       ├── SendPriceUpdateUseCase.kt
│       └── ManageConnectionUseCase.kt
├── data/
│   ├── config/
│   │   └── Constants.kt               # Data layer config (WS_URL)
│   ├── datasource/
│   │   └── WebSocketDataSource.kt     # WebSocket handling
│   ├── dto/
│   │   └── PriceUpdateDto.kt         # Network DTO + mappers
│   └── repositories/
│       ├── PriceRepositoryImpl.kt
│       └── ConnectionRepositoryImpl.kt
├── presentation/
│   ├── state/
│   │   └── PriceTrackerUiState.kt    # UI state models
│   ├── ui/
│   │   └── PriceTrackerScreen.kt     # Composable screens
│   └── viewmodel/
│       ├── PriceTrackerViewModel.kt  # ViewModel
│       └── PriceTrackerViewModelFactory.kt
├── ui/
│   └── theme/
│       ├── Theme.kt                   # Theme with dark mode
│       ├── Color.kt                   # Color palette
│       └── Type.kt                    # Typography
├── MainActivity.kt
└── Constants.kt                       # App-level constants
```

## 🚀 Key Features

### 1. **Clean Architecture**
- Domain layer contains no Android dependencies
- Data layer isolated from presentation layer
- Easy to test each layer independently

### 2. **MVVM with StateFlow**
```kotlin
val uiState: StateFlow<PriceTrackerUiState> = _uiState.asStateFlow()
// Reactive state updates trigger UI recomposition
```

### 3. **Real-time WebSocket Integration**
- Connects to WebSocket for price updates
- Handles connection state transitions
- Graceful error handling with Result wrappers

### 4. **Dark Theme Support**
- Toggle between light and dark modes
- Smooth transitions
- Material You design on Android 12+

### 5. **Comprehensive Error Handling**
```kotlin
Result<T> // Type-safe error handling throughout the app
```

## 🏃 Getting Started

### Prerequisites
- Android Studio Giraffe or later
- Android SDK 24+
- Kotlin 2.0+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/Realtime-Price-Tracker.git
cd Realtime-Price-Tracker
```

2. Open in Android Studio and let Gradle sync dependencies

3. Run on emulator or device:
```bash
./gradlew installDebug
```

## 📚 Data Flow

### UI Update Flow
1. User interacts with UI (e.g., clicks Start button)
2. ViewModel receives action and calls use case
3. Use case invokes repository method
4. Repository calls data source
5. Data source connects to WebSocket or retrieves data
6. Result flows back up through Repository → Use Case → ViewModel
7. ViewModel updates StateFlow
8. Compose recomposes with new state

### Example: Starting Price Feed
```
User Tap "Start"
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
State updated: _uiState.update { it.copy(isRunning = true) }
    ↓
UI recomposes with new state
```

## 🧪 Testing Strategy

The architecture supports comprehensive testing:

- **Unit Tests**: Test use cases with mocked repositories
- **Repository Tests**: Mock data sources to test business logic
- **UI Tests**: Use Compose testing APIs with mocked ViewModel
- **Integration Tests**: Full flow testing with real WebSocket

Example:
```kotlin
@Test
fun testGetInitialStocks() = runTest {
    val mockRepository = mockk<PriceRepository>()
    coEvery { mockRepository.getStocks(any()) } returns Result.success(listOf(mockStock))
    
    val useCase = GetInitialStocksUseCase(mockRepository)
    val result = useCase()
    
    assertTrue(result.isSuccess)
}
```

## 🛣️ Roadmap

### Phase 1: Foundation ✅
- [x] Clean architecture setup
- [x] MVVM with StateFlow
- [x] WebSocket integration
- [x] Dark theme support
- [x] Initial UI implementation

### Phase 2: Enhancements (Next)
- [ ] Add Hilt dependency injection
- [ ] Implement comprehensive error messages
- [ ] Add caching layer for offline support
- [ ] Real stock price API integration
- [ ] User preferences persistence

### Phase 3: Advanced Features
- [ ] Portfolio management
- [ ] Price alerts and notifications
- [ ] Historical price charts
- [ ] Multiple watchlists
- [ ] Push notifications

### Phase 4: Optimization
- [ ] Performance profiling
- [ ] Memory optimization
- [ ] Battery usage optimization
- [ ] Instrumented tests expansion
- [ ] Compose optimization

## 🤝 Best Practices Demonstrated

1. **Single Responsibility Principle**: Each class has one reason to change
2. **Dependency Inversion**: High-level modules don't depend on low-level modules
3. **Immutability**: Data classes and StateFlow for predictable state
4. **Type Safety**: Result wrappers instead of exceptions for flow control
5. **Composability**: Combine use cases and repositories easily
6. **Testability**: Interfaces enable mock implementations

## 📄 Dependencies

- **Jetpack Compose**: Modern UI framework
- **AndroidX Lifecycle**: ViewModel and StateFlow
- **Material3**: Material Design components
- **OkHttp**: HTTP client for WebSocket
- **Gson**: JSON serialization
- **Kotlin Coroutines**: Async operations

See `gradle/libs.versions.toml` for version details.

## 🔧 Configuration

### WebSocket URL
Located in `data/config/Constants.kt`:
```kotlin
const val WS_URL = "wss://ws.postman-echo.com/raw"
```

### Stock Symbols
Located in `domain/config/Constants.kt`:
```kotlin
val STOCK_SYMBOLS = listOf("AAPL", "GOOG", "TSLA", ...)
```

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙋 Support

For questions or issues, please:
1. Check existing issues on GitHub
2. Create a new issue with detailed description
3. Follow the template for bug reports

## 👨‍💻 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**Built with ❤️ using Clean Architecture principles**
