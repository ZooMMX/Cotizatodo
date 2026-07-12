# Cotizatodo — versión Quarkus

Port de la aplicación **Cotizatodo** (originalmente Spring Boot 1.2.1 / Java 8)
a **Quarkus 3.20 / Java 21**. Genera cotizaciones rápidas y las exporta a PDF
con JasperReports.

La app original vive en la raíz del repositorio (`../src`); esta reconstrucción
vive en `cotizatodo-quarkus/` y **reutiliza** los recursos estáticos, las
plantillas de reporte `.jrxml` y los bundles i18n de la app original (se copian
en tiempo de build vía `maven-resources-plugin`, no se duplican en git).

## Stack

| Capa           | Antes (Spring)                       | Ahora (Quarkus)                          |
| -------------- | ------------------------------------ | ---------------------------------------- |
| Framework web  | Spring MVC (`@Controller`)           | JAX-RS / RESTEasy Reactive               |
| Vistas         | Thymeleaf + `layout:decorator`       | Qute (`{#include layout}`)               |
| Persistencia   | Spring Data JPA (`CrudRepository`)   | Hibernate ORM con Panache                |
| Seguridad      | Spring Security (formLogin)          | Quarkus Security — form auth + BCrypt    |
| Reportes       | `JasperReportsPdfView`               | `ReportService` + JasperReports 6.21     |
| i18n           | `MessageSource` + interceptor        | `MessageCatalog` + `LocaleFilter`        |
| Build          | Maven + spring-boot-maven-plugin     | Maven + quarkus-maven-plugin             |

## Correcciones de seguridad respecto al original

Durante el port se cerraron tres fallas presentes en la app Spring:

1. **IDOR en edición de perfil** — `/user/update` ahora opera siempre sobre el
   usuario autenticado (`SecurityIdentity`), no sobre el `username` enviado en
   el formulario.
2. **Soft-delete de cotizaciones ajenas** — la acción grupal `softdelete` de
   `/quotes/JSON` valida que cada cotización pertenezca al usuario activo.
3. **Mass assignment en el registro** — `/register` enlaza solo los campos
   permitidos (nunca roles/enabled) y valida disponibilidad del username.

Además: la vista y el logo de una cotización (`/quote/{id}`,
`/quotes/viewLogo/{id}`) devuelven **403** para cotizaciones de otro usuario.

## Ejecutar en desarrollo

Modo dev con base H2 en memoria y usuario semilla `admin` / `admin`:

```bash
cd cotizatodo-quarkus
mvn quarkus:dev
```

App en <http://localhost:8080>. El idioma se cambia con `?lang=es` / `?lang=en`.

## Pruebas

```bash
mvn test
```

`SmokeTest` cubre: páginas públicas, redirección a login de rutas protegidas,
login + guardado de cotización + generación de PDF, y las correcciones de IDOR.

## Empaquetar y ejecutar en producción (MySQL)

```bash
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

Configuración por variables de entorno (ver `application.properties`):

| Variable                 | Descripción                                    |
| ------------------------ | ---------------------------------------------- |
| `DB_URL`                 | JDBC URL de la base (`jdbc:mysql://host/cotizatodo`) |
| `DB_USER` / `DB_PASSWORD`| Credenciales de la base                        |
| `DB_KIND`                | Motor: `mysql` (por defecto) o `postgresql`. **Se fija al construir**, no en runtime (ver Despliegue). |
| `PORT`                   | Puerto HTTP (por defecto 8080). Varios PaaS lo inyectan automáticamente. |
| `COOKIE_ENCRYPTION_KEY`  | **Obligatoria en prod**: clave (≥16 bytes) para cifrar la cookie de sesión |

El esquema (`user`, `user_role`, `quote`) es compatible con el de la app
original; `startup.sql` de la raíz sigue sirviendo para inicializar MySQL.

## Despliegue en un PaaS (contenedor Docker)

En la **raíz del repo** hay un `Dockerfile` (imagen JVM) que produce un contenedor
autocontenido. El contexto de build es la raíz porque el módulo reutiliza los
recursos de la app legada en `src/main/resources`.

```bash
# Desde la raíz del repositorio
docker build -t cotizatodo .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host/cotizatodo" \
  -e DB_USER=cotizatodo -e DB_PASSWORD=secreto \
  -e COOKIE_ENCRYPTION_KEY="$(openssl rand -hex 16)" \
  cotizatodo
```

**PostgreSQL en vez de MySQL**: el motor se fija al construir (es una propiedad
build-time de Quarkus). Construye la imagen con el build-arg y apunta el `DB_URL`
a Postgres en runtime:

```bash
docker build --build-arg DB_KIND=postgresql -t cotizatodo .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host:5432/cotizatodo" \
  -e DB_USER=... -e DB_PASSWORD=... \
  -e COOKIE_ENCRYPTION_KEY="$(openssl rand -hex 16)" \
  cotizatodo
```

### Opciones económicas (≈ un par de USD/mes)

| Plataforma | Base de datos | Notas |
|---|---|---|
| **Railway** | MySQL gestionado | La más simple: agrega el plugin MySQL, conecta el repo, despliega por Dockerfile. Inyecta `PORT` y variables de la BD. ~5 USD/mes de crédito hobby. |
| **Fly.io** | Postgres (o BD externa) | La más barata con escala a cero. Usa `cotizatodo-quarkus/fly.toml` (cópialo a la raíz). Construye con `--build-arg DB_KIND=postgresql` si usas Postgres. |
| **Koyeb** | Neon Postgres (gratis) | Instancia nano barata; BD externa gratuita en Neon. |
| **Clever Cloud** | MySQL gestionado | Soporte nativo de Quarkus; mantiene MySQL sin cambios. |

**Pasos comunes en cualquier plataforma:**

1. Provisiona la base de datos (MySQL o Postgres) y anota su URL/usuario/contraseña.
2. Carga `startup.sql` (o deja que Hibernate cree el esquema con `ddl-auto=update`;
   crea al menos un usuario con `INSERT` + hash BCrypt para poder entrar).
3. Configura los secretos: `DB_URL`, `DB_USER`, `DB_PASSWORD`, y **`COOKIE_ENCRYPTION_KEY`**
   (genérala con `openssl rand -hex 16`; si cambia, se invalidan las sesiones).
4. Apunta la plataforma al `Dockerfile` de la raíz (build-arg `DB_KIND=postgresql`
   solo si usas Postgres) y despliega.

> **Ejemplo Fly.io:** `cp cotizatodo-quarkus/fly.toml ./fly.toml`, edita el nombre
> de la app, luego `fly launch --no-deploy`, `fly secrets set DB_URL=... DB_USER=... DB_PASSWORD=... COOKIE_ENCRYPTION_KEY=$(openssl rand -hex 16)` y `fly deploy`.

Recomendación de RAM: **512 MB** para ir holgado (JVM + JasperReports). Funciona en
256 MB pero queda justo. El despliegue es en **modo JVM** a propósito: la imagen
nativa usaría menos memoria, pero JasperReports + AWT complica el build nativo y no
compensa para ahorrar un dólar.
