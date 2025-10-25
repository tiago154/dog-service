# syntax=docker/dockerfile:1

# Stage 1: resolve dependencies once for better layer caching
FROM clojure:temurin-21-tools-deps-alpine AS deps
WORKDIR /app

# Copy only dependency manifests so `clojure -P` can be cached
COPY deps.edn ./
COPY .clj-kondo/config.edn .clj-kondo/config.edn
COPY .kaocha.edn .kaocha.edn
RUN clojure -P -M:run || true

# Stage 2: copy sources (still using a CLI image to keep tooling available)
FROM clojure:temurin-21-tools-deps-alpine AS build
WORKDIR /app
COPY --from=deps /root/.m2 /root/.m2
COPY . ./
RUN clojure -P -M:run || true

# Stage 3: runtime image (still tools-deps for simplicity, but running as non-root)
FROM clojure:temurin-21-tools-deps-alpine AS runtime
WORKDIR /app

# Create a non-root user up front so we can use --chown on COPY
RUN addgroup -S app \
	&& adduser -S -G app -h /home/app -s /bin/ash app

# Copy resolved dependencies and project sources with correct ownership
COPY --chown=app:app --from=build /root/.m2 /home/app/.m2
COPY --chown=app:app --from=build /app /app

USER app

ENV HOME=/home/app
ENV PORT=3000
EXPOSE 3000

# Lean entrypoint prints helpful URLs before starting Jetty
CMD ["clojure", "-M:run"]
