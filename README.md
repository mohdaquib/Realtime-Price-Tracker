# Real-Time Price Tracker

An Android application built with Jetpack Compose and WebSockets to simulate real-time stock prices. On the first launch, the app displays the top 25 stocks, each initialized with a base price of 100.0 plus a randomly generated value between 0.0 and 200.0. When the Start button is pressed, the app connects to the Postman Echo WebSocket server, sends the generated prices, and receives the same values in response. If a stock price decreases, it flashes red; if it increases, it flashes green.

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
<img width="580" height="1200" alt="price_tracker" src="https://github.com/user-attachments/assets/0ca9f6b5-fee8-4417-aab4-f1af4c7d3b67" />
