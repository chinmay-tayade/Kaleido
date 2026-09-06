# Kaleido

**An offline-first product catalog built once in Kotlin and shipped to Android, iOS and the web.**

Kaleido started from the classic "Product Catalog" brief — list the [Fake Store API](https://fakestoreapi.com)
products, open a detail screen — and takes it as far as the data allows: one
Compose Multiplatform codebase, a real offline-first data layer, and a set of
features the API's `price` / `rating` fields quietly make possible (value
scoring, side-by-side compare, category analytics).

| Platform | Entry point | Status |
|---|---|---|
| **Android** | `composeApp/src/androidMain` · `MainActivity` | ✅ builds & runs (`assembleDebug`) |
| **iOS** | `iosApp/` (SwiftUI shell) + `KaleidoKit.framework` | ✅ framework builds; open `iosApp` in Xcode to run |
| **Web** | `composeApp/src/wasmJsMain` (Kotlin/Wasm + Skia) | ✅ builds (`wasmJsBrowserDistribution`) |

---

## What it does

### Core (from the brief)
- **Product list** — adaptive Compose grid, image + title + price, pull-to-refresh,
  infinite scroll (the API caps at 20, so paging steps 10 → 20).
- **Product detail** — image, title, price, description, category, rating, plus
  "more in this category".
- **Search / filter / sort** — debounced text search across title, category and
  description; category chips; sort by value, price ↑/↓, rating or name.
- **Loading / empty / error / retry** everywhere, no crashes on a dead network.

### Beyond the brief
- **Offline-first.** The last good catalog is persisted locally and shown
  instantly on a cold start. A failed refresh keeps the cached data on screen and
  flips a visible *"showing your saved copy"* banner instead of erroring.
- **Wishlist & Cart.** Fully on-device — favourite anything, adjust cart
  quantities, see a running subtotal. Survives process death and airplane mode;
  no account, no backend.
- **Compare.** Pick 2–3 products and line them up: price, rating, review count
  and deal score, with the best value in each row highlighted.
- **Deal Score & "For You".** A single explainable 0–100 number per product that
  rewards a good rating (shrunk toward the mean when there are few reviews) and
  punishes a high price. It drives the "best value" badges, the default sort, and
  a "Picked for you" rail that shows the top pick from *each* category so it stays
  diverse.
- **Category insights.** Average price and rating, price ranges, a rating
  histogram and a per-category "best value" — small bar charts drawn in Compose.
- **Recently viewed** history, badge counts on the nav bar, light/dark theming.

---

## Architecture

```
composeApp/src/commonMain/kotlin/com/kaleido/app/
├─ domain/            pure Kotlin — no Compose, no Ktor, no platform types
│  ├─ Product.kt          model + dealScore()
│  ├─ Catalog.kt          CatalogEngine: filter / sort / "for you"
│  ├─ Insights.kt         InsightsEngine: category aggregates, histograms
│  ├─ Cart.kt             CartSummary totals
│  └─ CatalogRepository.kt  interface + DataStatus
│
├─ data/
│  ├─ remote/          Ktor client + hand-rolled JSON mapping for the Fake Store API
│  ├─ local/           KeyValueStore (SharedPreferences / NSUserDefaults / localStorage)
│  │                   + CatalogCache (offline snapshot) + ShelfStore (wishlist/cart/…)
│  └─ CatalogRepositoryImpl.kt   cache-then-network, status tracking
│
├─ di/                Koin modules (expect/actual platform module for engine + storage)
│
└─ ui/                Compose Multiplatform — one UI for all three platforms
   ├─ theme/          Kaleido colour system (violet / coral / mint) + spectrum for charts
   ├─ components/     ProductCard, RatingStars, DealScoreBadge, shimmer, state views
   ├─ catalog/ detail/ store/ compare/ insights/   screen + ViewModel per feature
   └─ nav/            string route table
```

- **MVVM**, unidirectional state: every screen exposes a single
  `StateFlow<XxxUiState>` from an `androidx.lifecycle.ViewModel` (the multiplatform
  one) and renders it with `collectAsStateWithLifecycle`.
- **Dependency rule:** `ui` → `domain` ← `data`. The domain layer is plain Kotlin
  and is where all the interesting logic lives, which is why it's the part that's
  unit-tested.
- **DI:** Koin, with an `expect val platformModule` supplying the HTTP engine
  (OkHttp / Darwin / JS) and the key-value store per platform.

## Tech

| | |
|---|---|
| Language | Kotlin 2.4.10, coroutines / `Flow` |
| UI | Compose Multiplatform 1.11 (Material 3) + Navigation Compose |
| Networking | Ktor 3 client, `kotlinx.serialization` JSON tree parsing |
| DI | Koin 4 |
| Images | Coil 3 (multiplatform) |
| Persistence | platform key-value stores behind one interface |
| Build | Gradle 9, AGP 9, version catalog |
| Tests | `kotlin-test`, `kotlinx-coroutines-test`, Turbine, Ktor `MockEngine` |

## Build & run

```bash
# Android — APK or straight onto a device/emulator
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# Web — static bundle in composeApp/build/dist/wasmJs/productionExecutable
./gradlew :composeApp:wasmJsBrowserDistribution
./gradlew :composeApp:wasmJsBrowserDevelopmentRun     # live dev server

# iOS — build the shared framework, then open the Xcode project
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
open iosApp/iosApp.xcodeproj      # pick a simulator and Run

# Tests (domain + repository + stores)
./gradlew :composeApp:testDebugUnitTest
```

> The Fake Store API is plain HTTP-friendly and needs no key. `NSAppTransportSecurity`
> is relaxed in the iOS `Info.plist` for that reason.

## Notes & trade-offs

- **`@Serializable` is avoided** in shared code: the kotlinx.serialization compiler
  plugin currently crashes on the Kotlin/Wasm target, so DTOs are mapped by hand
  from the JSON tree and navigation uses string routes. Small cost, keeps web alive.
- **iOS targets Apple Silicon** (`iosArm64` + `iosSimulatorArm64`); Compose
  Multiplatform no longer publishes `iosX64`.
- The API only serves 20 products, so pagination and "load more" are real but
  necessarily short.
