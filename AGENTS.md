# AGENTS.md — Ratonean2 (app Android)

Contexto para sesiones futuras. Última actualización: 2026-10-21.

## Repos relacionados

- **App Android (este repo):** `/Users/mamorales/AndroidStudioProjects/ratonean2app`
- **Backend Ktor + ETL SEPA:** `/Users/mamorales/AndroidStudioProjects/ratonean2`
- **Scripts Python viejos (referencia, ya no son la fuente de verdad):**
  - `/Users/mamorales/PycharmProjects/sepa_loader`
  - `/Users/mamorales/PycharmProjects/sepa_image_searcher`

La documentación cruzada está en `docs/PROJECT_CONTEXT.md`.

## Estado actual (2026-10-21)

- **API:** `https://v2202609396147529037.quicksrv.de`
- **Swagger:** `https://v2202609396147529037.quicksrv.de/swagger-ui`
- **Server:** `159.195.19.151` (Debian 13, 2 vCPU, 3.8 GB RAM, Docker, Postgres 16).
- **Base Ratonean:** `ratonean` en `ratonean_postgres`, accesible solo en `127.0.0.1:5434` del server.
- **Datos actuales:** snapshot SEPA **2026-10-20** con 13.739.113 productos (13.569.223 con EAN).
- **Histórico:** 7 días (2026-10-14 a 2026-10-20) en `precio_hist_dia` y `precio_hist_comercio`.
- **App:** apunta al server por `local.properties` y por el default de release en `app/build.gradle.kts`.
- **Imágenes:** pipeline Open Food Facts + DuckDuckGo; la app muestra placeholder determinístico si no hay foto real.

## Comandos útiles

```bash
# Compilar app debug
./gradlew :app:compileDebugKotlin

# Correr tests unitarios
./gradlew :app:testDebugUnitTest

# Generar APK debug
./gradlew :app:assembleDebug

# Build release (requiere config de firma)
./gradlew :app:assembleRelease
```

## Reglas importantes

- **No commitear secretos.** `local.properties` está en `.gitignore` y contiene las URLs del server.
- El server tiene secretos en `/opt/ratonean/.env` (root, permisos 600). No copiarlos al repo.
- Para deploy/SSH usar la clave `~/.ssh/ratonean_deploy`; no usar password de root en scripts.
- **No tocar `compra2`**: es otro stack en el mismo server (`compra2_api`, `compra2_postgres`, `compra2_s3mock`).
- El Postgres de compra2 sigue expuesto en `5432`; no es parte de Ratonean2.
- `productos_ean` **no es el código de barras**: es un flag 1/0. El EAN real es `id_producto`.
- La tabla `productos` del server es un snapshot (se reemplaza en cada import); el histórico vive en las tablas `precio_hist_*`.
- SEPA son 7 recursos diarios que se sobrescriben semanalmente; no hay histórico en la fuente.

## Cambios hechos en la app

- `ProductEntity` usa `productKey = commerceId|flagId|branchId|productId` como PK y `branchKey = commerceId|flagId|branchId`.
- `BranchEntity` usa `branchKey` como PK; `CommerceEntity` usa `commerceKey = id|flagId`. Antes `branchId`/`id` no eran únicos y la caché pisaba sucursales/comercios.
  - Datos 2026-10-21: 2.661 sucursales vs 1.463 IDs únicos; 28 comercios vs 16 IDs únicos. Por eso faltaban en la app.
- `ProductDao` tiene `getByEan`, `getByBranchKey` e índices por `ean`/`branchKey`; `enforceRowCap` usa `productKey`.
- `CatalogDatabase` versión 5 con `fallbackToDestructiveMigration` (es caché).
- Mapa, navegación y detalle de sucursal usan `branchKey`; el detalle llama a `/product/by-branch-key/{key}` y la búsqueda a `/product/searchByBranchKeys`.
- `/branch/nearby` ahora diversifica: devuelve primero una sucursal por comercio/bandera y luego el resto por distancia. Antes el índice devolvía todo Carrefour y la app parecía no tener Coto/Disco.
- `BranchRepositoryImpl` no confía en una caché con una sola cadena; fuerza API para refrescar.
- `ProductImage` (`product/presentation/components/ProductImage.kt`): imagen real con fallback a placeholder con inicial/color. Se usa en tarjetas, listas y lista de compras.
- URL del backend:
  - `local.properties`: `API_BASE_URL_DEBUG` y `API_BASE_URL_RELEASE` al hostname del server.
  - `app/build.gradle.kts`: default release al hostname del server.

## Pendientes

1. UI del escáner de barras (el endpoint `/product/by-ean` y el DAO local ya existen).
2. Tickets OCR + comparación contra listas de compras.
3. Mejorar cobertura de imágenes reales (hoy se usa placeholder para las que faltan).
4. Dominio propio para producción (hoy se usa el hostname del VPS).
