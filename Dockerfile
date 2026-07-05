# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 – Builder
# Uses the official Maven image bundled with Eclipse Temurin JDK 17 on Alpine
# to produce a slim build environment.
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

# Set the working directory inside the container for all subsequent commands.
WORKDIR /build

# ── Dependency cache layer ────────────────────────────────────────────────────
# Copy ONLY pom.xml first. Docker caches this layer; Maven dependencies are
# re-downloaded only when pom.xml changes, not on every source code change.
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# ── Source build ─────────────────────────────────────────────────────────────
# Now copy the full source tree and build the executable JAR.
# -DskipTests: tests are run in CI, not during Docker image builds.
# -B (batch mode): suppresses interactive Maven output for cleaner logs.
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress


# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 – Runtime
# Eclipse Temurin 17 JRE on Alpine is ~90 MB vs ~500 MB for the full JDK image.
# Only the JRE (not the compiler / Maven) is needed at runtime.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# ── Security: non-root user ───────────────────────────────────────────────────
# Running as root inside a container is a security risk. Creating a dedicated
# user/group limits the blast radius of any container escape vulnerability.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory where the JAR will live.
WORKDIR /app

# ── Copy artifact from builder ────────────────────────────────────────────────
# Only the final fat JAR is copied — the Maven cache, source files, and JDK
# are all left behind in the builder stage, keeping this image minimal.
COPY --from=builder /build/target/restaurant-service-*.jar app.jar

# Transfer JAR ownership to the non-root user so it can be read at runtime.
RUN chown appuser:appgroup app.jar

# Drop root privileges. All subsequent RUN / CMD / ENTRYPOINT run as appuser.
USER appuser

# ── Port declaration ──────────────────────────────────────────────────────────
# Documents that the container listens on 8082. ECS task definitions and
# docker run -p flags map the host port to this container port.
EXPOSE 8082

# ── JVM tuning for containers ─────────────────────────────────────────────────
# UseContainerSupport  – respect Linux cgroup CPU/memory limits (default on
#                        Java 11+, but explicit for clarity).
# MaxRAMPercentage     – cap heap at 75 % of container memory; leaves headroom
#                        for Metaspace, threads, and OS overhead.
# egd                  – substitute /dev/urandom for /dev/random so Tomcat
#                        startup is not blocked waiting for entropy.
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

# ── Graceful shutdown signal ──────────────────────────────────────────────────
# ECS sends SIGTERM before forcibly stopping a task. Spring Boot's graceful
# shutdown (server.shutdown=graceful) intercepts SIGTERM, drains in-flight
# requests, then exits cleanly within the configured 30 s window.
STOPSIGNAL SIGTERM

# ── ECS health check ──────────────────────────────────────────────────────────
# ECS uses this Docker HEALTHCHECK to decide whether a task is healthy.
# --start-period=60s  gives the JVM and Spring context time to fully start
#                     before the first check fires (avoids false early failures).
# wget is used instead of curl because Alpine's base image includes wget.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8082/actuator/health || exit 1

# ── Entrypoint ────────────────────────────────────────────────────────────────
# Shell form (sh -c) is required so the $JAVA_OPTS environment variable is
# expanded at container start. Exec form would treat $JAVA_OPTS as a literal
# string. The JVM receives SIGTERM directly because the JAR is PID 1's child.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
