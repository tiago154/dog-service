# 🐶 Dog Service

Small Clojure service that proxies the public [Dog CEO API](https://dog.ceo/dog-api/) to expose random dog images and breed information with Swagger documentation.

## 🗂 Project Structure

| Path | Description |
| --- | --- |
| `src/dog_service/core.clj` | Jetty entry point that wires the HTTP server to the Compojure API routes. |
| `src/dog_service/routes.clj` | Route definitions plus Swagger metadata and schemas (`DogImage`, `ErrorResponse`). |
| `src/dog_service/handlers.clj` | HTTP handlers that translate requests into service calls and HTTP responses (JSON or image bytes). |
| `src/dog_service/dog.clj` | Pure functions responsible for calling the Dog CEO API and shaping the service data model. |
| `test/dog_service/*_test.clj` | Unit tests for service and handler layers using `clojure.test` with `with-redefs` mocks. |
| `.clj-kondo/` | Lint configuration and custom hooks so Compojure macros get first-class linting. |
| `.kaocha.edn` | Kaocha test runner configuration with Cloverage plugin options. |
| `Dockerfile` | Multi-stage build that caches deps and runs the service as a non-root user. |

## 🌐 HTTP API

The server listens on `http://localhost:3000` by default.

| Method & Path | Description | Response |
| --- | --- | --- |
| `GET /` | Health check | `{"status":"UP"}` |
| `GET /favicon.ico` | Stub to avoid 404 spam | Always `404` JSON |
| `GET /api/dog` | Fetch random dog image metadata | `DogImage` JSON |
| `GET /api/dogs` | List all known breeds | JSON array of strings |
| `GET /api/dogs/:breed?showImage=true` | Fetch an image by breed. When `showImage=true` returns the raw JPEG; otherwise returns JSON metadata. | `DogImage` JSON, JPEG, or `ErrorResponse` |
| `ANY *` | Catch-all route | `404` JSON |

Swagger UI is available at `http://localhost:3000/docs` when the server is running. The OpenAPI spec is served at `/swagger.json`.

## 🛠 Tooling & Commands

All commands assume the [Clojure CLI](https://clojure.org/guides/getting_started) is installed.

| Goal | Command |
| --- | --- |
| Start the server | `clj -M:run` |
| Run the test suite | `clj -X:test` |
| Run tests with coverage (writes HTML to `target/coverage/index.html`) | `clj -X:coverage` |
| Build Docker image | `docker build -t dog-service .` |
| Run using Docker | `docker run --rm -p 3000:3000 dog-service` |

Tips:

- `-M` executes aliases configured as a traditional `-main` entry point.
- `-X` runs aliases that declare an `:exec-fn`. Kaocha receives an optional `:plugins` map via `:exec-args`.
- To avoid typing the long commands repeatedly, consider tiny wrapper scripts (for example `scripts\test.cmd`) or tools like Babashka tasks or Makefiles.
- When building the container the multi-stage Dockerfile caches dependencies first, so rebuilding after code changes is typically fast.
- The Dockerfile uses the Alpine variant of the Clojure CLI image; install extra system packages with `apk add --no-cache <pkg>` when necessary.

## 📦 Key Dependencies

| Library | Why it is here |
| --- | --- |
| `metosin/compojure-api` | Declarative routing with integrated Swagger documentation and request coercion. |
| `metosin/ring-http-response` | Convenience helpers to build standard Ring responses. |
| `ring/ring-jetty-adapter` | Embedded Jetty server used by `dog-service.core`. |
| `clj-http/clj-http` | HTTP client used to talk to Dog CEO API and to download images when `showImage=true`. |
| `prismatic/schema` & `metosin/schema-tools` | Define response schemas for Swagger and documentation tooling. |
| `cheshire/cheshire` | JSON encoding/decoding (implicitly used by compojure-api). |
| `lambdaisland/kaocha` | Test runner that wraps `clojure.test` with rich reporting. |
| `lambdaisland/kaocha-cloverage` | Coverage plugin that instruments namespaces and produces HTML reports. |
| `org.slf4j/slf4j-nop` | Disables noisy logging from Jetty/HTTP clients during tests. |
| `clojure:temurin-21-tools-deps-alpine` (Docker base) | Official Alpine image with Clojure CLI + Temurin JDK used for dependency caching, build, and runtime layers. |

## 🧭 Development Notes

- Tests rely on `with-redefs` to stub HTTP calls; when adding new external integrations prefer exposing small pure functions like the ones in `dog.clj` to keep tests isolated.
- The Compojure macros create locals at runtime; the project ships with custom clj-kondo hooks (`.clj-kondo/hooks/compojure/api/sweet.clj`) so editors get proper linting without inline ignore annotations.
- Coverage skips the `dog-service.core` namespace because it only wire-ups Jetty; add more exclusions in `.kaocha.edn` or `deps.edn` if you introduce other thin entry points.
- When adding routes, place shared Swagger schemas near their usage inside `routes.clj` so the documentation stays centralised.

## 📡 API Samples

```bash
curl http://localhost:3000/api/dog
# => {"image":"https://images.dog.ceo/breeds/...","breed":"random","source":"Dog API"}

curl http://localhost:3000/api/dogs
# => ["affenpinscher","african","airedale",...]

curl http://localhost:3000/api/dogs/pug
# => {"image":"https://images.dog.ceo/breeds/pug/n02110958_11264.jpg","breed":"pug","source":"Dog API"}

curl "http://localhost:3000/api/dogs/pug?showImage=true" --output pug.jpg
# downloads the JPEG instead of JSON
```

## 🧰 Troubleshooting

- **Encoding issues during coverage**: the `:coverage` alias runs the JVM with `-Dfile.encoding=UTF-8`. If you create new aliases that instrument code, remember to add the same JVM option to preserve accented strings.
- **Lint errors about unresolved symbols in routes**: ensure your editor respects the `.clj-kondo/config.edn` hooks or run `clj-kondo --lint src` manually after installing `clj-kondo`.

Enjoy building on top of the Dog Service! ajustes and contributions are welcome.
