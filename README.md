# TiendaApp — API REST con JWT y roles

API de una tienda en línea con tres roles: **ADMIN**, **CLIENTE** y **VENDEDOR**.
La seguridad está hecha a mano (sin Spring Security completo): un filtro (`JwtValidationFilter`) valida el token JWT
y un interceptor (`RoleInterceptor`) lee la anotación `@RequiresRole`.

- Java 17+ · Spring Boot 4.1.0 (webmvc + data-jpa + validation)
- Base de datos MySQL / MariaDB
- JWT con jjwt 0.12.5 · contraseñas con BCrypt · Lombok
- Puerto `9090` y contexto `/api/v1/tienda`

URL base: `http://localhost:9090/api/v1/tienda`

---

## 1. Requisitos

- JDK 17 o superior
- Maven (o el wrapper `mvnw` / `mvnw.cmd`, si el proyecto lo incluye)
- MySQL 8 o MariaDB

## 2. Base de datos

1. Abre tu cliente SQL (MySQL Workbench, DBeaver, phpMyAdmin...) conectado al servidor.
2. Ejecuta **todo** el script `tienda.sql`. Crea el esquema `tienda_db`, las tablas, los 3 roles
   (ADMIN = 1, CLIENTE = 2, VENDEDOR = 3) y el usuario ADMIN inicial.
3. Comprueba que quedó bien:

```sql
SELECT * FROM tienda_db.roles;
SELECT id, username, rol_id FROM tienda_db.users;
```

> Si el esquema ya existía de una prueba anterior, ejecuta antes `DROP SCHEMA tienda_db;`.

## 3. Variables de entorno

La configuración sensible **no está escrita en el código**: se lee de variables de entorno
(`src/main/resources/application.yaml`).

| Variable | Obligatoria | Descripción | Valor por defecto |
| --- | --- | --- | --- |
| `JWT_SECRET_KEY` | Sí | Secreto en Base64 con el que se firma el JWT | — |
| `DB_PASSWORD` | No | Contraseña del usuario de la base de datos | vacío |
| `DB_USERNAME` | No | Usuario de la base de datos | `root` |
| `DB_URL` | No | URL JDBC de la base de datos | `jdbc:mysql://localhost:3306/tienda_db` |
| `JWT_EXPIRATION` | No | Duración del token en milisegundos | `600000` (10 minutos) |

### Generar `JWT_SECRET_KEY`

Linux / macOS / Git Bash:

```bash
openssl rand -base64 32
```

Windows (PowerShell):

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

### Definir las variables

Windows (PowerShell) — duran solo mientras la ventana esté abierta:

```powershell
$env:JWT_SECRET_KEY = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
$env:DB_PASSWORD    = "tu_clave"                                  # omitir si el usuario no tiene contraseña
$env:DB_URL         = "jdbc:mysql://localhost:3308/tienda_db"     # cambiar el puerto si no es el 3306
```

Linux / macOS:

```bash
export JWT_SECRET_KEY=$(openssl rand -base64 32)
export DB_PASSWORD=tu_clave
export DB_URL=jdbc:mysql://localhost:3306/tienda_db
```

> **Puerto de la base de datos:** para saber en qué puerto escucha tu servidor, ejecuta en el cliente SQL
> `SHOW VARIABLES LIKE 'port';`

### Nota sobre MariaDB

Si el servidor es **MariaDB** (por ejemplo el de XAMPP), `application.yaml` debe indicar el dialecto,
porque la sintaxis del bloqueo de filas que usa Hibernate (`FOR UPDATE OF ...`) no existe en MariaDB:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
```

Con **MySQL 8** esa propiedad no es necesaria y se puede quitar.

## 4. Cómo correr el proyecto

Desde la carpeta raíz (donde está el `pom.xml`) y en la misma terminal donde definiste las variables:

```bash
mvn spring-boot:run
```

(o `./mvnw spring-boot:run` / `.\mvnw.cmd spring-boot:run` si usas el wrapper).

La API está lista cuando aparece una línea como:

```
Started TiendaApplication in 7.5 seconds
```

La terminal queda ocupada mientras la API corre. Para detenerla: **Ctrl + C**.

## 5. Credenciales del ADMIN inicial

| Campo | Valor |
| --- | --- |
| username | `admin` |
| email | `admin@tienda.com` |
| password | `Admin123*` |

La contraseña está guardada encriptada con BCrypt en `tienda.sql`. Cámbiala si el proyecto se usa fuera de clase.

## 6. Cómo se crean los demás usuarios

- `POST /auth/register` crea siempre un **CLIENTE** (el body no recibe rol).
- Los **VENDEDOR** y otros **ADMIN** los crea un ADMIN con `POST /users`, indicando `rolId`
  (1 = ADMIN, 2 = CLIENTE, 3 = VENDEDOR).

## 7. Cómo funciona la seguridad

1. `JwtValidationFilter` lee el header `Authorization: Bearer <token>`.
   - Sin header, token inválido o expirado → **401**.
   - Si es válido, guarda `username`, `userId` y `rolId` como atributos del request.
2. `RoleInterceptor` revisa la anotación `@RequiresRole` (en el método o, si el método no la tiene, en la clase).
   - Rol no permitido → **403**.
3. El servicio valida la **propiedad del recurso** cuando aplica: un VENDEDOR solo modifica sus productos y los pedidos
   de sus productos; un CLIENTE solo cancela sus pedidos → **403** si no es suyo.
4. El dueño de un producto y el cliente de un pedido salen **del token**, nunca del body.

Rutas públicas (el filtro no exige token): `/auth/**`, `GET /products` y `GET /products/{id}`.
`GET /products/mine` **sí** exige token.

Respuestas de error con la forma `{"message": "...", "data": null}`:

| Código | Cuándo |
| --- | --- |
| 400 | Datos inválidos, stock insuficiente, estado inválido |
| 401 | Credenciales malas, usuario inactivo, token ausente / inválido / expirado |
| 403 | Rol no permitido o recurso que no es del usuario |
| 404 | Recurso inexistente |
| 409 | Duplicados (usuario, correo, categoría) o recurso en uso |

## 8. Endpoints

| Método | Ruta | Roles |
| --- | --- | --- |
| POST | `/auth/register` | Público (crea CLIENTE) |
| POST | `/auth/login` | Público (por username o email) |
| POST | `/auth/refreshtoken` | Público (envía el token vigente en el header) |
| GET | `/users/me` | ADMIN, CLIENTE, VENDEDOR |
| GET | `/users` | ADMIN |
| POST | `/users` | ADMIN |
| PATCH | `/users/{id}/role` | ADMIN |
| PATCH | `/users/{id}/status` | ADMIN |
| GET | `/categories` | ADMIN, CLIENTE, VENDEDOR |
| POST | `/categories` | ADMIN |
| DELETE | `/categories/{id}` | ADMIN |
| GET | `/products` (`?categoryId=&page=&size=`) | Público |
| GET | `/products/{id}` | Público |
| GET | `/products/mine` | VENDEDOR |
| POST | `/products` | VENDEDOR |
| PUT | `/products/{id}` | VENDEDOR (dueño), ADMIN |
| DELETE | `/products/{id}` | VENDEDOR (dueño), ADMIN |
| POST | `/orders` | CLIENTE |
| GET | `/orders/mine` | CLIENTE |
| GET | `/orders/sales` | VENDEDOR |
| GET | `/orders` | ADMIN |
| PATCH | `/orders/{id}/status` | VENDEDOR (pedidos de sus productos), ADMIN |
| PATCH | `/orders/{id}/cancel` | CLIENTE (su pedido, solo si está PENDIENTE) |

### Reglas de negocio de los pedidos

- El **total** lo calcula el servidor: `precio × cantidad`.
- Al crear un pedido se valida el stock (si no alcanza → **400**) y se **descuenta** el stock, todo en una transacción.
- Al cancelar un pedido (solo si está `PENDIENTE`) se **devuelve** el stock.
- Estados permitidos: `PENDIENTE`, `ENVIADO`, `ENTREGADO`, `CANCELADO`.
- Un pedido `CANCELADO` ya no puede cambiar de estado (409).
- No se puede eliminar un producto que tenga pedidos asociados (409), ni una categoría que tenga productos (409).

## 9. Pruebas con Postman

Importa `TiendaApp.postman_collection.json`. Tiene una petición por endpoint y guarda los tokens en variables
(`token_admin`, `token_vendedor`, `token_cliente`) automáticamente al hacer login.

Orden recomendado la primera vez:

1. Login ADMIN
2. Crear VENDEDOR (`POST /users`, `rolId = 3`)
3. Registrar CLIENTE
4. Login VENDEDOR y login CLIENTE
5. Crear categoría
6. Crear producto (con el VENDEDOR)
7. Crear pedido (con el CLIENTE)

## 10. Pruebas desde PowerShell (sin Postman)

Pega este bloque una sola vez en una terminal **distinta** de la que corre la API:

```powershell
$base = "http://localhost:9090/api/v1/tienda"
Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
function Api($method, $path, $body = $null, $token = $null) {
  $req = New-Object System.Net.Http.HttpRequestMessage ([System.Net.Http.HttpMethod]::new($method), "$base$path")
  if ($token) { $req.Headers.Add("Authorization", "Bearer $token") }
  if ($body) {
    $json = $body | ConvertTo-Json
    $req.Content = New-Object System.Net.Http.StringContent($json, [Text.Encoding]::UTF8, "application/json")
  }
  $resp = $client.SendAsync($req).GetAwaiter().GetResult()
  $bytes = $resp.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
  $text = [Text.Encoding]::UTF8.GetString($bytes)
  Write-Host "HTTP $([int]$resp.StatusCode)" -ForegroundColor Yellow
  Write-Host $text
  $global:last = $null
  if ($text) { try { $global:last = $text | ConvertFrom-Json } catch {} }
}
```

Preparar datos:

```powershell
Api POST "/auth/login" @{ user="admin"; password="Admin123*" }
$tAdmin = $last.data.jwt
Api POST "/users" @{ username="vendedor1"; email="vendedor1@tienda.com"; password="Vendedor123*"; rolId=3 } $tAdmin
Api POST "/users" @{ username="vendedor2"; email="vendedor2@tienda.com"; password="Vendedor123*"; rolId=3 } $tAdmin
Api POST "/auth/register" @{ username="cliente1"; email="cliente1@tienda.com"; password="Cliente123*" }
Api POST "/auth/login" @{ user="vendedor1"; password="Vendedor123*" }
$tVend = $last.data.jwt
Api POST "/auth/login" @{ user="vendedor2"; password="Vendedor123*" }
$tVend2 = $last.data.jwt
Api POST "/auth/login" @{ user="cliente1@tienda.com"; password="Cliente123*" }
$tCli = $last.data.jwt
Api POST "/categories" @{ name="Tecnología" } $tAdmin
Api GET "/categories" $null $tAdmin
$catId = $last.data[0].id
Api POST "/products" @{ name="Audífonos"; description="Bluetooth"; price=120000.5; stock=10; categoryId=$catId } $tVend
$prodId = $last.data.id
Api POST "/products" @{ name="Teclado"; description="Mecánico"; price=90000; stock=5; categoryId=$catId } $tVend2
$prod2 = $last.data.id
```

> Los tokens duran 10 minutos. Si una respuesta dice `HTTP 401` con "El token está expirado", repite el login.

## 11. Evidencias de los casos pedidos

| # | Caso | Código | Comando |
| --- | --- | --- | --- |
| 1 | Login correcto | 200 | `Api POST "/auth/login" @{ user="admin"; password="Admin123*" }` |
| 2 | Token ausente | 401 | `Api GET "/users/me"` |
| 3 | Token expirado | 401 | ver abajo |
| 4 | Rol no permitido (cliente lista usuarios) | 403 | `Api GET "/users" $null $tCli` |
| 5 | Vendedor edita producto ajeno | 403 | `Api PUT "/products/$prod2" @{ name="Hack"; description="x"; price=1; stock=1; categoryId=$catId } $tVend` |
| 6 | Pedido sin stock | 400 | `Api POST "/orders" @{ productId=$prodId; quantity=999999 } $tCli` |

**Token expirado:** detener la API, arrancarla con `$env:JWT_EXPIRATION = "5000"` (5 segundos), y luego:

```powershell
Api POST "/auth/login" @{ user="admin"; password="Admin123*" }
$tExp = $last.data.jwt
Start-Sleep -Seconds 7
Api GET "/users/me" $null $tExp
```

Al terminar: detener la API, ejecutar `Remove-Item Env:JWT_EXPIRATION` y arrancarla de nuevo.

## 12. Estructura del proyecto

```
src/main/java/com/adso1651/tienda
├── config        (AppConfig, FilterConfig, WebConfig)
├── controller    (Auth, Users, Categories, Products, Orders)
├── dto           (DTO de entrada y salida; UserDTO no tiene password)
├── entity        (Roles, Users, Categories, Products, Orders)
├── enums         (RoleEnum, OrderStatusEnum)
├── exception     (ApiException, GlobalExceptionHandler)
├── filter        (JwtValidationFilter)
├── repository    (un repositorio JPA por entidad)
├── security      (RequiresRole, RoleInterceptor, SecurityResponseWriter)
└── service       (Auth, Jwt, Users, Categories, Products, Orders)
```

## 13. Retos extra implementados

- Validación de DTO con `spring-boot-starter-validation` (400 con los errores por campo).
- `@RestControllerAdvice` (`GlobalExceptionHandler`) para centralizar el manejo de errores.
- `@RequiresRole` a nivel de clase (`ElementType.TYPE`): el interceptor la usa si el método no tiene la suya.
- Paginación en `GET /products` con `?page=` y `?size=`.