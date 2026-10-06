# AGENTS.md

Guidance for AI agents working in this repository.

## Project

**Ev Stok** — a fully offline Android app (Kotlin + Jetpack Compose + Material 3 + Room) for tracking household consumables. Single user, no network permission, no accounts, no sync. All user-facing text is **Turkish and hardcoded in composables** — `res/values/strings.xml` only contains `app_name`. This is intentional; do not introduce string resources or i18n unless asked.

## Commands

```bash
./gradlew assembleDebug        # debug APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # the only test suite (plain JUnit4, app/src/test)
./gradlew assembleRelease      # release build; minify disabled, no signing configured
```

- Requires JDK 17 and Android SDK with compileSdk 35 (minSdk 26).
- CI (`.github/workflows/android.yml`) runs `assembleDebug testDebugUnitTest`, uploads the debug APK as an artifact, and creates a GitHub Release on `v*` tags. There is no other release pipeline.
- No ktlint/detekt/spotless — `kotlin.code.style=official` is the only style setting.
- Single module `:app`; dependency versions live in `gradle/libs.versions.toml` (use `libs.*` aliases, never hardcode versions in `app/build.gradle.kts`).

## Architecture

Single-activity app. `MainActivity` hosts a `NavHost` + bottom `NavigationBar` with three destinations defined in `Routes` (Turkish route strings: `"stok"`, `"market"`, `"katalog"`).

```
EvStokApp (Application)
  └─ AppContainer → StockRepository        (manual DI, no framework)
data/  Model.kt (entities + enums) · Daos.kt · AppDatabase.kt · SeedCatalog.kt · StockRepository.kt
ui/    home/ · shopping/ · catalog/ (each: Screen + ViewModel) · components/ · dialogs/ · theme/
```

- **MVVM with Room Flows as source of truth.** ViewModels expose a `StateFlow<UiState>` built with `combine(...)`/`flatMapLatest` + `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), default)`. Screens collect with `collectAsStateWithLifecycle()`.
- **ViewModel creation pattern:** each screen does `viewModel { XViewModel((context.applicationContext as EvStokApp).container.repository) }`. Copy this pattern for new screens; there is no factory/Hilt.
- **List rendering pattern:** ViewModels map entities into a sealed interface of rows (`Header` / `Item`), each with a stable `key` string used by `LazyColumn`. Filters are separate `MutableStateFlow`s combined into `uiState`.
- Two tables: `catalog` (the product pool, ~90 seeded entries) and `stock` (items the user tracks, one row per catalog entry). `addCustom` find-or-creates a catalog row (match on name+category) before inserting the stock row.
- Catalog seeding runs in `StockRepository.init` on `Dispatchers.IO`, guarded by a SharedPreferences flag (`"evstok"` prefs, key `"seeded_v1"`) plus a `count() == 0` check — not via a Room callback.

## Gotchas

- **Enums are persisted as their `.name` strings** via Room TypeConverters (`Category`, `ItemStatus` → e.g. `"BITTI"`, `"GIDA"`). Renaming an enum constant silently corrupts existing rows (unknown values fall back to `DIGER`/`VAR` in `fromName`). Adding constants is safe, but `ordinal` is used for in-memory sort order, so insertion position changes grouping order.
- **`AppDatabase` is version 1 with `exportSchema = false`.** Any entity change needs a manual version bump + `Migration`; there are no schema files to diff against.
- **Duplicates are handled by swallowing exceptions, not by pre-checks.** `catalog` has a unique index on `(name, category)` and `stock` on `catalogId`; `StockRepository.addFromCatalog`/`addCustom` wrap inserts in try/catch and return `Boolean` (`false` = duplicate/failed). Keep this contract.
- **Sorting happens twice, deliberately.** DAO queries do `ORDER BY category, name` (SQL collation, not Turkish-correct); ViewModels then re-sort with `Collator.getInstance(Locale("tr"))` and group by `Category.entries` order. Don't rely on SQL order.
- **`StockRepository.deleteCatalogItem` performs two DAO calls (stock rows, then catalog row) with no wrapping transaction** — a pre-existing atomicity gap to be aware of when touching deletion logic.
- **AMOLED-only theme.** `EvStokTheme` has exactly one hard-coded dark scheme (pure black background, mint/lavender accents from `ui/theme/Color.kt`); no light theme, no dynamic color. Status colors (`StatusGreen/StatusAmber/StatusRed`) are mapped via the `ItemStatus.statusColor` extension in `ui/components/Common.kt`.
- Screens receive the Scaffold `PaddingValues` as a `padding` parameter and apply it manually (edge-to-edge is enabled).
- Experimental APIs are opted in at file level: `@file:OptIn(ExperimentalMaterial3Api::class, ...)` at the top of screen/dialog files.
- Core domain rule: tapping a stock item cycles status `VAR → AZ → BİTTİ → VAR` (`ItemStatus.next()`); the Market screen shows everything with status != VAR; marking purchased resets to VAR. `ShoppingViewModel.undo` supports only a single-step undo held in memory.
- UI state that isn't in Room (dialog visibility, selected item) lives in screen-local `remember { mutableStateOf(...) }`, not in the ViewModel.

## Testing

- `app/src/test/java/com/evstok/app/StatusLogicTest.kt` — plain JUnit4 unit tests covering enum cycling, `fromName` fallbacks, and Turkish chip labels. No Compose UI tests, no instrumented tests, no Robolectric.
- Room DAOs, repository, and ViewModels are untested; run `./gradlew testDebugUnitTest` after touching logic in `data/` or the enums.
- README (Turkish `README.md`, English `README.en.md`) documents user-facing behavior — keep the two in sync when behavior changes.
