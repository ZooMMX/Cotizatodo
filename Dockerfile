# ============================================================
# Cotizatodo (Quarkus) — imagen JVM lista para PaaS
#
# El contexto de build es la RAÍZ del repo, porque el módulo Quarkus reutiliza
# en tiempo de build los recursos de la app legada (src/main/resources: estáticos,
# plantillas .jrxml e i18n) vía maven-resources-plugin.
#
# Motor de BD: se fija en build (propiedad build-time de Quarkus). MySQL por
# defecto; para PostgreSQL construye con  --build-arg DB_KIND=postgresql
# ============================================================

# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
ARG DB_KIND=mysql
ENV DB_KIND=${DB_KIND}
WORKDIR /build

# Recursos legados que el módulo Quarkus copia en build-time (../src)
COPY src/main/resources ./src/main/resources
# Módulo Quarkus
COPY cotizatodo-quarkus ./cotizatodo-quarkus

WORKDIR /build/cotizatodo-quarkus
# Descarga de dependencias en capa aparte para aprovechar la caché
RUN mvn -q -B -DskipTests dependency:go-offline || true
RUN mvn -q -B -DskipTests package

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Fuentes del sistema: JasperReports renderiza texto con AWT (headless)
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# Artefacto Quarkus fast-jar (self-contained: ya incluye estáticos, .jrxml e i18n)
COPY --from=build /build/cotizatodo-quarkus/target/quarkus-app/lib/ ./lib/
COPY --from=build /build/cotizatodo-quarkus/target/quarkus-app/*.jar ./
COPY --from=build /build/cotizatodo-quarkus/target/quarkus-app/app/ ./app/
COPY --from=build /build/cotizatodo-quarkus/target/quarkus-app/quarkus/ ./quarkus/

ENV JAVA_OPTS_APPEND="-Djava.awt.headless=true"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
