# Real-Time Price Tracker

Android app using Jetpack Compose and WebSockets for mock stock prices.

## How to Run
- Open in Android Studio.
- Build and run on device/emulator (API 24+).

## Architecture
MVVM with MVI for state handling, using StateFlow and Coroutines.

## Assumptions
- Used OkHttp for WebSockets.
- Mock prices randomized; no real data.
- JSON via Gson.

## Tests
Run `./gradlew test` for unit tests

## Bonus
Implemented flash, themes, tests.

## Screenshot
<img width="1080" height="2340" alt="price_tracker" src="https://github.com/user-attachments/assets/0ca9f6b5-fee8-4417-aab4-f1af4c7d3b67" />
