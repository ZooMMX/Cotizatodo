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
| `DB_URL`                 | JDBC URL de MySQL (`jdbc:mysql://host/cotizatodo`) |
| `DB_USER` / `DB_PASSWORD`| Credenciales de la base                        |
| `COOKIE_ENCRYPTION_KEY`  | **Obligatoria en prod**: clave (≥16 bytes) para cifrar la cookie de sesión |

El esquema (`user`, `user_role`, `quote`) es compatible con el de la app
original; `startup.sql` de la raíz sigue sirviendo para inicializar MySQL.
