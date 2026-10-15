# Jelly Backend

Spring Boot service the mobile app talks to. Proxies TMDB, returns only the fields the app needs, and stores favorites in PostgreSQL.

## Quick start

Prerequisite: Docker. `TMDB_API_TOKEN` is preconfigured in the committed [`.env`](.env) file (for convenience), so no setup is needed.

```sh
docker compose up --build
```

Starts PostgreSQL and the backend. The first build takes a few minutes (Gradle downloads dependencies inside the image).

- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Endpoints

| Method | Path | Success | Errors |
|---|---|---|---|
| `GET` | `/api/v1/movies?category={upcoming\|top_rated\|now_playing}&page={1..500}` | 200 | 400, 502 |
| `GET` | `/api/v1/movies/{movieId}` | 200 | 400, 404, 502 |
| `GET` | `/api/v1/users/{userId}/favorites` | 200 | 400 |
| `PUT` | `/api/v1/users/{userId}/favorites/{movieId}` | 201 created, 200 already a favorite | 400, 404, 502 |
| `DELETE` | `/api/v1/users/{userId}/favorites/{movieId}` | 204 | 400, 404 |

Errors use [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details (`application/problem+json`). 502 means TMDB is unavailable.

## Postman

[`postman/jelly-backend.postman_collection.json`](postman/jelly-backend.postman_collection.json): import it into Postman and run the collection against the running stack.

- **Happy path** (run in order): list movies, open the first one, add it to favorites twice (201, then 200), list favorites, remove it.
- **Errors**: 400 for invalid category/page/movie ID/user ID, 404 for unknown movie and for removing a movie that isn't a favorite.

Command line: `npx newman run postman/jelly-backend.postman_collection.json` (`--env-var baseUrl=...` for another host).

## Run from the IDE

Requires JDK 21.

1. `docker compose up -d db` (Postgres on `localhost:5432`; override with `DB_PORT` in `.env`).
2. `./gradlew bootRun`, or run `JellybackendApplication`. Local runs read `.env` too.

## Design decisions

- **Backend-for-frontend (BFF)**. The backend is shaped for one client, the mobile apps. The TMDB token stays on the server and never ships in the APK.
- **Abstracted TMDB** - TMDB models stay inside the tmdb package and get mapped to our own DTOs. If TMDB changes or we switch provider, the mobile API doesn't break.
- **Package-by-feature** (movie, favorite, tmdb, api) instead of layered packages (controller, service, repository). Each feature sits in one place.
- **Stateless service** - No sessions and all state in Postgres, so it scales horizontally behind a load balancer.
- **Versioned API (/api/v1)** - The mobile release cycle can't force an instant API migration.
- **Idempotent add** - `PUT` returns 201 the first time and 200 after that, so the mobile app can safely retry queued changes.
- **Favorites store a title/poster snapshot** - Listing favorites needs no TMDB calls. Adding one checks the movie exists in TMDB (404 for unknown IDs).
- **Schema via Flyway** - Hibernate only validates the schema (`ddl-auto=validate`).
- **Config** - Everything comes from env vars with local defaults, so the same image runs locally and in prod. The app fails at startup when `TMDB_API_TOKEN` is missing.
- **OpenAPI is the contract**, generated from the code and guarded by `OpenApiDocsTest`. It could be used to generate the KMP client.
- **No clean architecture layers**. It would be overkill for 2 features; package-by-feature is enough.
- **No auth yet**. The client-generated `userId` (an install UUID) is a placeholder. Migration path: anonymous token, then JWT.

## Tests

`./gradlew test` (Docker must be running: persistence tests use Testcontainers PostgreSQL).

- Web layer: `MovieControllerTest`, `FavoriteControllerTest` (`@WebMvcTest`)
- Persistence: `FavoriteMovieRepositoryTest` (`@DataJpaTest` + PostgreSQL)
- TMDB client: `TmdbClientTest` (`MockRestServiceServer`)
- Mapping: `MovieMapperTest`
- API docs: `OpenApiDocsTest`
