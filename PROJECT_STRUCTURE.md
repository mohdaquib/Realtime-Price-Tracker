# Project Structure Overview

This file shows the complete project structure after refactoring.

## Full Directory Tree

```
Realtime-Price-Tracker/
│
├── 📚 DOCUMENTATION (START HERE!)
│   ├── DOCUMENTATION_INDEX.md         👈 Start here for navigation
│   ├── QUICK_START.md                 👈 5-min quick overview
│   ├── README.md                      👈 Comprehensive guide
│   ├── ARCHITECTURE.md                👈 Technical deep dive
│   ├── REFACTORING_SUMMARY.md         👈 What changed
│   ├── CLEANUP_GUIDE.md               👈 Optional file cleanup
│   └── DELIVERABLES.md                👈 This refactoring deliverables
│
├── 🏗️ SOURCE CODE
│   └── app/src/main/java/com/realtimepricetracker/
│       │
│       ├── 🎯 DOMAIN LAYER (Framework-Independent Business Logic)
│       │   └── domain/
│       │       ├── config/
│       │       │   └── Constants.kt                    # Stock symbols
│       │       ├── entities/
│       │       │   └── Stock.kt                        # Business entity
│       │       ├── repositories/
│       │       │   ├── PriceRepository.kt             # Interface
│       │       │   └── ConnectionRepository.kt        # Interface
│       │       └── usecases/
│       │           ├── GetInitialStocksUseCase.kt
│       │           ├── SubscribeToPriceUpdatesUseCase.kt
│       │           ├── SendPriceUpdateUseCase.kt
│       │           └── ManageConnectionUseCase.kt
│       │
│       ├── 💾 DATA LAYER (External Data Sources)
│       │   └── data/
│       │       ├── config/
│       │       │   └── Constants.kt                    # WS_URL config
│       │       ├── datasource/
│       │       │   └── WebSocketDataSource.kt          # Network ops
│       │       ├── dto/
│       │       │   └── PriceUpdateDto.kt              # DTO + mappers
│       │       └── repositories/
│       │           ├── PriceRepositoryImpl.kt
│       │           └── ConnectionRepositoryImpl.kt
│       │
│       ├── 🎨 PRESENTATION LAYER (UI & State)
│       │   └── presentation/
│       │       ├── state/
│       │       │   └── PriceTrackerUiState.kt         # UI state model
│       │       ├── ui/
│       │       │   └── PriceTrackerScreen.kt          # Composables
│       │       └── viewmodel/
│       │           ├── PriceTrackerViewModel.kt       # MVVM ViewModel
│       │           └── PriceTrackerViewModelFactory.kt
│       │
│       ├── 🔌 DEPENDENCY INJECTION
│       │   └── di/
│       │       └── ServiceLocator.kt                  # DI Container
│       │
│       ├── 🎭 THEME & UI (Shared)
│       │   └── ui/theme/
│       │       ├── Theme.kt                           # Dark mode support
│       │       ├── Color.kt                           # Color palette
│       │       └── Type.kt                            # Typography
│       │
│       ├── 📱 APP ENTRY
│       │   └── MainActivity.kt                        # Main activity
│       │
│       └── ⚙️ OLD/LEGACY (Optional to remove)
│           ├── Constants.kt                           # Can stay
│           ├── network/WebsocketManager.kt            # Deprecated
│           ├── data/PriceUpdate.kt                    # Moved to DTO
│           ├── utils/JsonUtils.kt                     # Moved to DTO
│           └── ui/*.kt (old files)                    # Moved to presentation/
│
├── 🧪 TESTS
│   └── app/src/test/java/com/realtimepricetracker/
│       └── ui/
│           └── PriceTrackerViewModelTest.kt
│
├── ⚙️ BUILD CONFIG
│   ├── build.gradle.kts
│   ├── app/build.gradle.kts
│   ├── gradle.properties
│   ├── settings.gradle.kts
│   ├── local.properties
│   └── gradle/
│       ├── libs.versions.toml
│       └── wrapper/
│
├── 📦 GRADLE WRAPPER
│   ├── gradlew
│   ├── gradlew.bat
│   └── gradle/wrapper/
│
└── 📄 ROOT FILES
    ├── README.md                        # Project README
    ├── ARCHITECTURE.md                  # Architecture guide
    ├── QUICK_START.md                   # Quick start
    ├── DOCUMENTATION_INDEX.md           # Doc index
    ├── REFACTORING_SUMMARY.md           # Changes summary
    ├── CLEANUP_GUIDE.md                 # Cleanup guide
    └── DELIVERABLES.md                  # Deliverables
```

## Layer Dependencies

```
┌─────────────────────────────────────┐
│   PRESENTATION (UI/ViewModel)       │
│   - presentation/ui/                │
│   - presentation/viewmodel/         │
│   - presentation/state/             │
│   - ui/theme/ (shared)              │
└──────────────┬──────────────────────┘
               │ depends on (interfaces)
               ↓
┌─────────────────────────────────────┐
│   DOMAIN (Business Logic)           │
│   - domain/entities/                │
│   - domain/repositories/ (I/F)      │
│   - domain/usecases/                │
│   - domain/config/                  │
└──────────────┬──────────────────────┘
               │ implemented by
               ↓
┌─────────────────────────────────────┐
│   DATA (External Sources)           │
│   - data/repositories/ (impl)       │
│   - data/datasource/                │
│   - data/dto/                       │
│   - data/config/                    │
└─────────────────────────────────────┘
               ↓ uses
         ┴─ OkHttp, Gson, etc
```

## New vs Old File Locations

```
OLD LOCATION                          →  NEW LOCATION

ui/PriceTrackerScreen.kt             →  presentation/ui/PriceTrackerScreen.kt
ui/PriceTrackerViewModel.kt          →  presentation/viewmodel/PriceTrackerViewModel.kt
data/PriceUpdate.kt                  →  data/dto/PriceUpdateDto.kt
utils/JsonUtils.kt                   →  data/dto/PriceUpdateDto.kt (mappers)
network/WebsocketManager.kt          →  data/datasource/WebSocketDataSource.kt

(NEW) -                              →  domain/entities/Stock.kt
(NEW) -                              →  domain/repositories/PriceRepository.kt
(NEW) -                              →  domain/repositories/ConnectionRepository.kt
(NEW) -                              →  domain/usecases/*.kt
(NEW) -                              →  data/repositories/PriceRepositoryImpl.kt
(NEW) -                              →  data/repositories/ConnectionRepositoryImpl.kt
(NEW) -                              →  presentation/state/PriceTrackerUiState.kt
(NEW) -                              →  presentation/viewmodel/PriceTrackerViewModelFactory.kt
(NEW) -                              →  di/ServiceLocator.kt
```

## Key Files by Function

### Business Logic Files
- **domain/usecases/*.kt** - Core business operations
- **domain/entities/Stock.kt** - Core data models
- **domain/repositories/*.kt** - Data access contracts

### Data Layer Files
- **data/datasource/WebSocketDataSource.kt** - WebSocket communication
- **data/repositories/**impl.kt - Repository implementations
- **data/dto/PriceUpdateDto.kt** - Network data mapping

### Presentation Files
- **presentation/viewmodel/PriceTrackerViewModel.kt** - State management
- **presentation/ui/PriceTrackerScreen.kt** - UI components
- **presentation/state/PriceTrackerUiState.kt** - UI state definition

### Infrastructure Files
- **di/ServiceLocator.kt** - Dependency management
- **ui/theme/Theme.kt** - Theming & dark mode
- **MainActivity.kt** - App entry point

## Documentation Files

```
DOCUMENTATION_INDEX.md    - Guide to all documentation (START HERE)
├── QUICK_START.md       - 5-minute overview
├── README.md            - Complete project guide
├── ARCHITECTURE.md      - Technical architecture details
├── REFACTORING_SUMMARY.md - What changed and why
├── CLEANUP_GUIDE.md     - How to remove old files
└── DELIVERABLES.md      - Refactoring deliverables
```

## Build & Test Files

```
app/
├── build.gradle.kts                 - App-level build config
├── proguard-rules.pro               - ProGuard configuration
├── src/
│   ├── main/                        - Source code (see above)
│   ├── test/                        - Unit tests
│   └── androidTest/                 - Instrumented tests
```

## How to Navigate

### Looking for Business Logic?
→ Start in `domain/usecases/`

### Looking for Network Code?
→ Check `data/datasource/WebSocketDataSource.kt`

### Looking for UI Code?
→ Check `presentation/ui/`

### Looking for State Management?
→ Check `presentation/viewmodel/PriceTrackerViewModel.kt`

### Looking for Dependencies?
→ Check `di/ServiceLocator.kt`

### Looking for How Things Work?
→ Read `README.md` or `ARCHITECTURE.md`

## Statistics

- **Total Layers**: 4 (Domain, Data, Presentation, DI)
- **Package Structure**: 9+ organized packages
- **New Files**: 23 files created
- **Documentation**: 7 comprehensive guides
- **Use Cases**: 4 (business operations)
- **Repositories**: 2 interfaces + 2 implementations
- **UI Models**: 2 (main state + items)

## File Types

```
.kt files       - Kotlin source code (29 files)
.md files       - Documentation (7 files)
.xml files      - Resources, manifest
.gradle         - Build configuration
.properties     - Configuration
.gradle.kts     - Kotlin DSL build scripts
```

## Getting Around

```
To find files:     Use Android Studio's "Ctrl+N" (Find Class)
To understand:     Read DOCUMENTATION_INDEX.md
To learn:          Read README.md then ARCHITECTURE.md
To code:           Check presentation/viewmodel/ first
To test:           Use mockk with domain/repositories/
To debug:          Set breakpoints in domain/usecases/
```

---

**This structure makes the project:**
- ✅ Organized and navigable
- ✅ Easy to understand
- ✅ Easy to test
- ✅ Easy to extend
- ✅ Production-ready

**Start with DOCUMENTATION_INDEX.md to begin! 📖**

