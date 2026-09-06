# Android Machine Coding Starter

A ready-to-code Android starter wired for **MVVM + Clean Architecture**. Everything
compiles and runs as-is; there is no business logic — you add the feature.

## Toolchain

| | Version |
|---|---|
| Gradle | 9.7.1 (wrapper, checksum-pinned) |
| Android Gradle Plugin | 9.4.0 (built-in Kotlin) |
| Kotlin | 2.3.21 (KGP + KSP pinned in root `build.gradle.kts`) |
| compileSdk / targetSdk / minSdk | 37 / 37 / 24 |
| JDK toolchain | 17 |
| UI | Jetpack Compose (Material 3, BOM 2026.08.00) + Navigation-Compose |
| DI | Hilt (KSP) |
| Async | Coroutines / Flow |
| Network | Retrofit + OkHttp + kotlinx.serialization |
| Images | Coil |

Dependency versions live in `gradle/libs.versions.toml`.

## Build & run

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew installDebug           # install on a running device/emulator
./gradlew testDebugUnitTest      # unit tests
./gradlew lintDebug              # static analysis
```

Open the folder in Android Studio and let it sync — no extra setup.

## Architecture

```
presentation  ──▶  domain  ◀──  data
   (UI)            (pure)      (impl)
```

**Dependency rule:** `domain` depends on nothing Android. `data` and `presentation`
depend on `domain`, never on each other.

```
app/src/main/java/com/example/app/
├─ StarterApp.kt              @HiltAndroidApp
├─ MainActivity.kt            single activity → Compose + AppNavHost
│
├─ core/                      cross-cutting building blocks
│  ├─ common/Result.kt        sealed Result<T> (Success/Error/Loading) + operators
│  ├─ common/DispatcherProvider.kt   testable coroutine dispatchers
│  ├─ network/                HTTP helpers (Retrofit itself is provided in di/)
│  └─ ui/theme/               StarterTheme (Color / Type / Theme)
│
├─ di/                        Hilt modules
│  ├─ NetworkModule.kt        Json, OkHttp, Retrofit  (set BASE_URL here)
│  ├─ DispatcherModule.kt     binds DispatcherProvider
│  └─ RepositoryModule.kt     @Binds repository interface → impl (example in comments)
│
├─ data/                      the "how"
│  ├─ remote/api/             Retrofit service interfaces
│  ├─ remote/dto/             @Serializable network models
│  ├─ local/                  Room / DataStore / caches
│  ├─ mapper/                 DTO/Entity ⇄ domain model
│  └─ repository/             *RepositoryImpl : domain repository
│
├─ domain/                    the "what" — pure Kotlin
│  ├─ model/                  domain models
│  ├─ repository/             repository interfaces
│  └─ usecase/UseCase.kt      FlowUseCase / SuspendUseCase base classes
│
└─ presentation/              the "show"
   ├─ navigation/             type-safe Destinations + AppNavHost
   ├─ common/                 UiState, BaseViewModel (MVI-lite: state + onEvent)
   └─ home/                   placeholder screen (proves DI + Compose + nav)
```

Empty packages carry a short `README.md` describing what goes there.

## Adding a feature (typical flow)

1. `domain/model/` — define the model(s).
2. `domain/repository/` — define the repository interface.
3. `domain/usecase/` — add a use case extending `FlowUseCase` / `SuspendUseCase`.
4. `data/remote/` — Retrofit API + DTOs; `data/mapper/` — DTO → domain.
5. `data/repository/` — implement the interface; bind it in `di/RepositoryModule.kt`.
6. `presentation/<feature>/` — `XxxContract.kt` (state + events), `XxxViewModel`
   (`@HiltViewModel`, extends `BaseViewModel`), `XxxScreen` (Composable,
   `collectAsStateWithLifecycle`).
7. Register the screen in `presentation/navigation/`.

## Removing the placeholder

`presentation/home/` is only there so the app launches. Delete the package and
point `startDestination` in `AppNavHost.kt` at your own screen.

## Tests

- `src/test/` — JVM unit tests (JUnit4, MockK, Turbine, coroutines-test).
  `util/TestDispatcherProvider` swaps in a test dispatcher.
- `src/androidTest/` — instrumented tests run through `HiltTestRunner`.
