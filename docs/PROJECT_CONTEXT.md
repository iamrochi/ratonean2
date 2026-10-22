# Ratonean2 — contexto del proyecto

Última actualización: 2026-10-21. Este documento resume el estado de la app,
el backend, la ingesta SEPA y el deploy. Es un snapshot: antes de operar,
verificar el estado real del server.

## 1. Repos y rutas

| Componente | Ruta |
|---|---|
| App Android (Compose) | `/Users/mamorales/AndroidStudioProjects/ratonean2app` |
| Backend Ktor + ETL SEPA | `/Users/mamorales/AndroidStudioProjects/ratonean2` |
| Scripts Python viejos (referencia) | `/Users/mamorales/PycharmProjects/sepa_loader` |
| Buscador de imágenes viejo | `/Users/mamorales/PycharmProjects/sepa_image_searcher` |

La app y el backend son repos separados. El ETL nuevo vive en `ratonean2/sepa/`.

## 2. Arquitectura

```text
Android (Compose + Room + Ktor client + MapLibre)
        |
        | HTTPS
        v
nginx (v2202609396147529037.quicksrv.de:443)
        |
        v
ratonean_api (Ktor, contenedor, 127.0.0.1:8083)
        |
        v
ratonean_postgres (Postgres 16, contenedor, 127.0.0.1:5434)
        ^
        | COPY + swap atómico
        |
artefactos SEPA (CSV.gz) subidos desde el ETL local
```

El ETL corre local (Mac) por launchd y sube los artefactos. El server no baja
ni parsea SEPA, para no competir con la API y con compra2.

## 3. Server

- IP: `159.195.19.151`
- Hostname: `v2202609396147529037.quicksrv.de`
- Debian 13, 2 vCPU, 3.8 GB RAM, Docker 26.1, Postgres 16.
- **compra2** (no tocar): `compra2_api` en 8082, `compra2_postgres` en 5432,
  `compra2_s3mock` en 9090/9191.
- **Ratonean2**:
  - `ratonean_api`: 127.0.0.1:8083.
  - `ratonean_postgres`: 127.0.0.1:5434.
  - nginx con TLS Let's Encrypt para el hostname.
- Directorio: `/opt/ratonean/`.
- Acceso: clave SSH `~/.ssh/ratonean_deploy` (no password en scripts).
- Secretos: `/opt/ratonean/.env` (root, 600). No copiar al repo.

## 4. Datos actuales

- Snapshot actual: **2026-10-20**.
- Productos: **13.739.113**.
- Productos con EAN real: **13.569.223**.
- Comercios/banderas: **28**.
- Sucursales: **2.661**.
- Histórico: 7 días (2026-10-14 a 2026-10-20).
  - `precio_hist_dia`: ~76k filas/día.
  - `precio_hist_comercio`: ~240k filas/día.
- La tabla `productos` contiene solo el último snapshot. El histórico es
  agregado (min/mediana/max), no detalle por sucursal.

## 5. Modelo SEPA y mapeo

SEPA publica 7 ZIP (lunes a domingo). Cada ZIP contiene ZIPs por comercio y,
adentro, `comercio.csv`, `sucursales.csv`, `productos.csv`.

Campos importantes:

- `id_comercio`, `id_bandera`: clave real del comercio.
- `id_sucursal`: código interno del comercio, **no único global**.
  - Medido el 2026-10-21: 2.661 sucursales, solo 1.463 `id_sucursal` únicos;
    705 IDs repetidos entre comercios (1.903 filas).
- `id_comercio`: 28 filas de comercio/bandera, solo 16 IDs únicos.
- `id_producto`: es el **EAN/GTIN real** cuando `productos_ean = '1'`.
- `productos_ean`: flag 1/0, **no es el código de barras**.
- `productos_precio_lista`, `productos_precio_unitario_promo1/2`.
- `sucursales_latitud/longitud`, `sucursales_provincia` (código ISO, se traduce a nombre en el ETL).

Reglas:

- PK de comercios: `(id_comercio, id_bandera)`.
- PK de sucursales: `(id_comercio, id_bandera, id_sucursal)`.
- PK de productos: `(id_comercio, id_bandera, id_sucursal, id_producto)`.
- Precio efectivo: `COALESCE(promo1, listPrice)`.
- `id_producto` se conserva como string para no perder ceros.

## 6. API backend

Base: `https://v2202609396147529037.quicksrv.de`

Endpoints relevantes:

- `GET /` → `{"status":"ok"}`.
- `GET /product/nearby?lat=&lon=&radius=&limit=`.
- `GET /product/searchNearby?branchIds=&q=`.
- `GET /product/popularProducts?branchIds=&limitPerKeyword=`.
- `GET /product/branch/{branchId}`.
- `GET /product/by-branch-key/{key}` y `GET /product/searchByBranchKeys?branchKeys=&q=`.
- `GET /branch/by-key/{key}`.
- `GET /product/by-ean?ean=&lat=&lon=&radius=&limit=`:
  - Devuelve descripción, marca, presentación, cantidad de sucursales,
    mínimo/mediana/máximo y sucursales con precio efectivo e imagen.
  - Solo considera `productos_ean = '1'`.
- `GET /branch/nearby?lat=&lon=&radius=&limit=`.
- `POST /users` y `POST /auth/login`.

Swagger: `https://v2202609396147529037.quicksrv.de/swagger-ui`

## 7. App Android

- Compose + Room + Ktor + MapLibre.
- Catálogo en `CatalogDatabase` (`noBackupFilesDir`, caché evictable).
- `ProductEntity`:
  - PK `productKey` = `commerceId|flagId|branchId|productId`.
  - `branchKey` = `commerceId|flagId|branchId`; índice propio.
  - Índices por `branchId`, `branchKey`, `productId`, `ean`.
  - `ProductDao.getByEan` y `getByBranchKey`.
- `BranchEntity` PK `branchKey`; `CommerceEntity` PK `commerceKey = id|flagId`.
  - `id_sucursal` no es único global: 2.661 sucursales, 1.463 IDs únicos.
  - `id_comercio` no es único: 28 filas, 16 IDs únicos.
  - `/branch/nearby` diversifica por comercio/bandera para no devolver solo una cadena.
- `CatalogDatabase` versión 5 (destructive, es caché).
- `ProductImage` (`product/presentation/components/ProductImage.kt`):
  - Carga foto real con Coil.
  - Si no hay URL o falla, muestra placeholder determinístico con inicial y color.
  - Se usa en tarjetas, listas y lista de compras.
- URL:
  - `local.properties` (no se commitea): `API_BASE_URL_DEBUG` y `API_BASE_URL_RELEASE`.
  - `app/build.gradle.kts`: default release al hostname del server.
- Estado: compila y los tests unitarios pasan.

## 8. Ingesta SEPA

Detalle operativo en `ratonean2/docs/SEPA_PIPELINE.md`.

Resumen:

```bash
# Local: procesa los 7 recursos, genera artefactos y sube/importa
/Users/mamorales/AndroidStudioProjects/ratonean2/sepa/run_sepa_local.sh

# Solo ETL
cd /Users/mamorales/AndroidStudioProjects/ratonean2
python3 sepa/etl.py --all --out-dir sepa/out
```

- launchd local: `com.ratonean.sepa`, todos los días 03:00 ART.
- Estado: `sepa/state/state.json`.
- Artefactos: `sepa/out/sepa_YYYY-MM-DD.tar.gz`.
- Marcadores de import: `sepa/out/.imported/`.
- Server: `/opt/ratonean/import_sepa.sh` hace staging, `COPY`, swap y
  actualiza histórico.
- Retención de artefactos: 7 locales, 14 en el server.
- Histórico: 30 días por comercio, 365 días nacional.

## 9. Imágenes

- Tabla: `productos_imagenes_url` (`id_producto`, `imagen_url`, `fuente`, `confianza`, `fecha_actualizacion`).
- Fuentes:
  1. Open Food Facts (food/products/beauty/petfood).
  2. DuckDuckGo Images (fallback).
- Script: `ratonean2/sepa/images.py`.
- Cron server:
  - Diario 10:00 CEST (05:00 ART): 5.000 productos, Open Food Facts + DDG.
  - Domingo 12:00 CEST: retry de 2.000 `not_found`, solo DDG.
- Log: `/var/log/ratonean-images.log`.
- La app garantiza imagen visual con placeholder aunque no haya foto real.
- No se puede garantizar 100% fotos reales: SEPA no trae imágenes y hay
  productos genéricos o códigos internos.
- Al 2026-10-21 había ~1.900 fotos reales cargadas; el retry y los lotes
  diarios siguen aumentando la cobertura.

## 10. Automatización y operación

- ETL local: launchd 03:00 ART.
- Imágenes: cron del server.
- Backups/rollback: el import conserva `*_old` hasta el próximo import.
- Limpieza de histórico: `/opt/ratonean/cleanup_history.sh [dias_comercio] [dias_nacional]`.
- Logs:
  - ETL local: `~/Library/Logs/ratonean-sepa.log`.
  - Import server: `/opt/ratonean/backups/import_*.log`.
  - Imágenes: `/var/log/ratonean-images.log`.
  - API: `docker logs ratonean_api`.

## 11. Seguridad

- No commitear secretos.
- Usar `~/.ssh/ratonean_deploy`.
- Postgres Ratonean solo en loopback (`127.0.0.1:5434`).
- El 5432 de compra2 sigue público; pendiente de hardening, no tocar sin
  coordinar con compra2.
- Certificado Let's Encrypt con renovación automática (certbot).

## 12. Pendientes

1. UI del escáner de barras (endpoint y DAO listos).
2. Tickets OCR + comparación con listas.
3. Cobertura de imágenes reales para no-alimentos y productos sin EAN.
4. Dominio propio para producción.
5. Histórico por sucursal (hoy solo agregado) si se necesita.
6. Hardening del server (cerrar 5432 de compra2, revisar secretos en compose).
