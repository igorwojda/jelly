# Jelly Mobile

Kotlin Multiplatform app (Android + iOS) with a shared Compose Multiplatform UI. Browses movies from the
[Jelly backend](../jelly-backend) and keeps the user's favorites.

## Quick start

Requirements: Android Studio (latest stable), JDK 21 (Gradle downloads it when missing). iOS: Xcode, iOS 18.2+.

1. Start the backend ([jelly-backend](../jelly-backend)). Optional: without it the app uses TMDB directly.
2. The backend address defaults to `http://10.0.2.2:8080/api/v1/` (host machine as seen from the Android emulator,
   [gradle.properties](./gradle.properties)). Override it in `local.properties`:
   - Physical device: host machine LAN IP (device and host on the same network), e.g.
     `jelly.backendBaseUrl=http://192.168.1.5:8080/api/v1/`
   - iOS simulator: `jelly.backendBaseUrl=http://localhost:8080/api/v1/`
3. Open the `jelly-mobile` project in Android Studio and 
   1. run the Android app
   2. run the iOS app

The Android debug build's network security config
([androidApp/src/debug/res/xml/network_security_config.xml](./androidApp/src/debug/res/xml/network_security_config.xml))
allows plain HTTP to any host, so the app can reach the local backend by LAN IP.

## Design Decisions

### Modules

| Module | Content |
|---|---|
| `:feature:base` | FlowMVI `StoreViewModel`, Koin base module, Ktor `HttpClient`s, remote source fallback chain, Room database helpers, shared UI (theme, icons, LCE content, movie card, connection banner) |
| `:feature:movie` | Movie list (category filter, infinite scroll) and movie details |
| `:feature:favorites` | Favorites tab, favorites repository used by the other features |
| `:shared` | Composition root: app shell, bottom navigation, Koin start-up, `Shared` iOS framework |
| `:androidApp` | Android entry point |
| `build-logic` | Convention plugins: `jelly.kmp.library`, `jelly.feature`, `jelly.room`, `jelly.ios.framework`, `jelly.android.application` |

Module build scripts declare only their own dependencies; everything else lives in the convention plugins.

### Architecture

Clean Architecture per feature, with `data` / `domain` / `presentation` packages:

- **Data**: `*RepositoryImpl` combines remote data sources (Ktor, `*ResponseModel` DTOs mapped to domain models) with a
  local Room database. See [Offline and fallback](#offline-and-fallback).
- **Domain**: repository interfaces, models and use cases.
- **Presentation**: MVI with [FlowMVI](https://github.com/respawn-app/FlowMVI). Each ViewModel owns a store with a
  sealed `State` (`Loading` / `Content` / `Error`), `Intent`s from the UI and one-off `Action`s (snackbar messages).

### UI state with FlowMVI

Covers unidirectional data flow, one sealed `State` per screen, plugins instead of boilerplate, and thread-safe `updateState`. 

### Dependency injection

Use `Koin`. Follow one Koin module per Gradle module.

### Offline and fallback

The app doesn't hard-depend on the backend. Each repository reads from up to three data sources:

| Repository | 1. Jelly backend | 2. TMDB directly | 3. Room (on device) |
|---|---|---|---|
| `MovieRepositoryImpl` | `BackendMovieRemoteDataSource` | `TmdbMovieRemoteDataSource` | `MovieLocalDataSource`: cache of every page and details loaded |
| `FavoriteRepositoryImpl` | `BackendFavoriteRemoteDataSource` | none (see below) | `FavoriteDatabase`: source of truth, with a queue of pending changes |

- **Swappable remote sources**: implementations of one interface (`MovieRemoteDataSource`) in a priority-ordered
  `RemoteDataSourceChain`, defined in the feature's Koin module. A request goes to the first source that serves it;
  any failure, including a 404 from an incomplete backend, moves on to the next one. Adding, removing or reordering
  sources doesn't touch the repositories.
- **Fast fallback**: a source that failed is skipped for 30 s (`RemoteSourceHealthTracker`), so an unreachable backend
  doesn't add its 5 s connect timeout to every request.
- **Cache**: every successful response is saved to Room; when every remote source fails, the cached copy is returned
  (`fetchWithCacheFallback`). A 404 is never answered from the cache.
- **Favorites offline-first**: changes are saved to Room immediately and marked pending, then sent to the backend
  oldest first, right away when possible, otherwise on the next sync (opening a screen). Changes the backend rejects
  (4xx) are reverted with a snackbar. TMDB has no fallback for favorites: they belong to a TMDB account, which would
  need the user to sign in to TMDB.
- **Degraded state indication**: a non-blocking banner above the bottom navigation, driven by the outcome of the
  latest requests: "Jelly server unavailable" (data from TMDB) or "No connection" (data from the device only).

### Favorites

- Every screen observes the favorites from Room through the singleton `FavoriteRepositoryImpl`, so the movie list,
  details and favorites screens stay in sync.

### UI

Shared Compose Multiplatform UI instead of native SwiftUI. A native SwiftUI app would be preferred for iOS; the shared UI keeps the scope within the time limit.

### Gradle

Utilize `convention plugins` to share module configuration.
Enable `type-safe project accessors`.
