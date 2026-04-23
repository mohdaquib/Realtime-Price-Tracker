# Migration Cleanup Guide

## Files to Remove (Old Structure)

The following old files are now superseded by the new clean architecture implementation. They can be safely removed:

### Old UI Files
- `app/src/main/java/com/realtimepricetracker/ui/PriceTrackerScreen.kt` → Replaced by `presentation/ui/PriceTrackerScreen.kt`
- `app/src/main/java/com/realtimepricetracker/ui/PriceTrackerViewModel.kt` → Replaced by `presentation/viewmodel/PriceTrackerViewModel.kt`

### Old Data Files
- `app/src/main/java/com/realtimepricetracker/data/PriceUpdate.kt` → Replaced by `data/dto/PriceUpdateDto.kt`
- `app/src/main/java/com/realtimepricetracker/utils/JsonUtils.kt` → Functionality moved to `data/dto/PriceUpdateDto.kt` with mappers

### Old Network Files (Optional - can be removed after verification)
- `app/src/main/java/com/realtimepricetracker/network/WebsocketManager.kt` → Replaced by `data/datasource/WebSocketDataSource.kt`

## Why These Files Can Be Removed

1. **Functionality Preserved**: All functionality from old files has been migrated to new architecture
2. **No Duplicate Dependencies**: New files properly implement the functionality
3. **Better Organization**: New structure follows clean architecture principles
4. **Single Source of Truth**: No conflicting implementations

## Migration Verification Checklist

Before removing old files, verify:

- [x] MainActivity imports new `PriceTrackerViewModelFactory` ✅
- [x] MainActivity no longer imports from old `ui` package ✅
- [x] New `PriceTrackerViewModel` in `presentation/viewmodel/` ✅
- [x] New `PriceTrackerScreen` in `presentation/ui/` ✅
- [x] New data layer with repositories ✅
- [x] New domain layer with use cases ✅
- [x] ServiceLocator manages all dependencies ✅
- [x] Theme files updated with dark mode ✅

## Files to Keep (Still in Use)

✅ Keep these files - they're still used or contain shared configuration:
- `MainActivity.kt` (refactored)
- `Constants.kt` (app-level constants)
- `ui/theme/Theme.kt` (updated)
- `ui/theme/Color.kt` (extended)
- `ui/theme/Type.kt` (shared typography)
- All files in `data/` directory (new implementations)
- All files in `domain/` directory (new implementations)
- All files in `presentation/` directory (new implementations)
- `di/ServiceLocator.kt` (dependency management)

## Cleanup Steps

### Option 1: Manual Cleanup (Recommended for first time)
1. Open Android Studio
2. Delete old files from the project navigator
3. Android Studio will show build errors if something is broken
4. Fix any remaining imports
5. Run the project to verify everything works

### Option 2: Automated (If familiar with git)
```bash
# If using version control, you can:
# 1. Review changes
git diff

# 2. Stage deletions
git add -A

# 3. Commit
git commit -m "Remove old implementation files after clean architecture refactoring"
```

## Files Structure After Cleanup

```
app/src/main/java/com/realtimepricetracker/
├── di/
│   └── ServiceLocator.kt              ✅ Keep
├── domain/                            ✅ Keep (NEW)
│   ├── config/
│   │   └── Constants.kt
│   ├── entities/
│   │   └── Stock.kt
│   ├── repositories/
│   └── usecases/
├── data/                              ✅ Keep (NEW)
│   ├── config/
│   │   └── Constants.kt
│   ├── datasource/
│   │   └── WebSocketDataSource.kt
│   ├── dto/
│   │   └── PriceUpdateDto.kt
│   └── repositories/
├── presentation/                      ✅ Keep (NEW)
│   ├── state/
│   │   └── PriceTrackerUiState.kt
│   ├── ui/
│   │   └── PriceTrackerScreen.kt
│   └── viewmodel/
│       ├── PriceTrackerViewModel.kt
│       └── PriceTrackerViewModelFactory.kt
├── ui/                                ✅ Keep (UPDATED)
│   └── theme/
│       ├── Theme.kt                   (Enhanced)
│       ├── Color.kt                   (Extended)
│       └── Type.kt                    (No changes)
├── MainActivity.kt                    ✅ Keep (REFACTORED)
└── Constants.kt                       ✅ Keep

OLD FILES TO DELETE:
├── data/
│   └── PriceUpdate.kt                 ❌ Delete
├── utils/
│   └── JsonUtils.kt                   ❌ Delete (moved to dto/)
├── ui/
│   ├── PriceTrackerScreen.kt         ❌ Delete (moved to presentation/)
│   └── PriceTrackerViewModel.kt       ❌ Delete (moved to presentation/)
└── network/
    └── WebsocketManager.kt            ❌ Delete (moved to data/datasource/)
```

## Testing After Cleanup

After removing old files:

1. **Build the project**
   ```bash
   ./gradlew build
   ```

2. **Run the app**
   - Verify UI loads correctly
   - Test Start/Stop button functionality
   - Test dark mode toggle
   - Verify WebSocket connection works

3. **Run unit tests**
   ```bash
   ./gradlew test
   ```

4. **Check for any remaining issues**
   - If errors occur, refer to ARCHITECTURE.md for migration details

## Troubleshooting

### Error: "Unresolved reference: PriceTrackerViewModel"
- Check if old import statements still exist
- Update imports to use `presentation.viewmodel.PriceTrackerViewModel`

### Error: "Cannot access constructor for WebsocketManager"
- Remove any direct WebsocketManager instantiation
- Use ServiceLocator instead

### Error: "Type mismatch: UiState vs PriceTrackerUiState"
- Update UI composables to use new `PriceTrackerUiState` from `presentation/state/`

---

**Note**: This cleanup is optional but recommended for a clean, uncluttered codebase. The app works fine with both old and new files present, but having duplicates makes maintenance harder.

