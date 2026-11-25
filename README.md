# PrestaControl API (`prestamoservice`)

API REST en **Spring Boot 3.3 + Java 17 + PostgreSQL**, protegida con **JWT**.
Group id y package: `com.santacruz`.

> Cada prestamista ve **solo su cartera**. El dueño se obtiene del email que viene dentro del token.

---

## 1) Ejecutar la API (lo más fácil: Docker)

Necesitas **Docker Desktop** abierto.

### Paso 1: crear el archivo de entorno
Copia `.env.example` y renómbralo `.env`. Si quieres, cambia el `JWT_SECRET` por una frase larga (mínimo 32 caracteres).

### Paso 2: levantar PostgreSQL + API
Dentro de esta carpeta:

```powershell
docker compose up --build
```

La primera vez tarda unos minutos porque descarga imágenes y compila el JAR.

- API: http://localhost:8080
- PostgreSQL: localhost:5432 (usuario `prestacontrol`, clave `prestacontrol123`, BD `prestacontrol`)

Para apagar: `docker compose down`. Para borrar también los datos: `docker compose down -v`.

### Alternativa sin Docker Desktop (con Java 17 instalado)
Incluí el *Maven Wrapper*, así que no necesitas instalar Maven.
Primero levanta solo la BD (para esto sí necesitas Docker):

```powershell
docker compose up -d db
.\mvnw.cmd spring-boot:run
```

O generas el JAR y lo ejecutas:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target/prestamoservice-0.0.1-SNAPSHOT.jar
```

En Linux/Mac usa `./mvnw` en vez de `.\mvnw.cmd`.

---

## 2) Importar la colección de Postman

1. Abre Postman → **Import** → selecciona `postman/PrestaControl.postman_collection.json`.
2. La variable `baseUrl` ya apunta a `http://localhost:8080`.
3. Ejecuta en este orden:
   1. **1. Registrar prestamista** → guarda el token en la variable `token`.
   2. **2. Iniciar sesión** → refresca el token.
   3. **5. Registrar préstamo** → guarda el id en `prestamoId`.
   4. Luego listar, obtener, actualizar, resumen y eliminar.
   5. **B. Sin token → 401** y **C. Token alterado → 401**.
4. Copia el token de la variable `token` y pégalo en https://jwt.io para ver el `sub` (email) y `exp` (expiración).

Los 7 endpoints de la guía están en la carpeta **Auth (público)** y **Préstamos (protegidos)**.

---

## 3) Endpoints

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/register` | Público | Crea prestamista, cifra contraseña con BCrypt y devuelve token |
| POST | `/auth/login` | Público | Valida email/contraseña y devuelve JWT |
| GET | `/prestamos` | Token | Lista solo los préstamos del prestamista del token |
| GET | `/prestamos/{id}` | Token | Obtiene un préstamo propio |
| POST | `/prestamos` | Token | Registra un préstamo |
| PUT | `/prestamos/{id}` | Token | Edita o marca como pagado |
| DELETE | `/prestamos/{id}` | Token | Anula un préstamo |
| GET | `/prestamos/resumen` | Token | **Reto:** capital, total por cobrar y ganancia |

Cabecera para los protegidos:

```text
Authorization: Bearer <token>
```

Sin token, con token alterado o vencido: **401** con este cuerpo:

```json
{"status":401,"mensaje":"Token ausente, inválido o vencido"}
```

El campo `totalACobrar` = `monto + (monto * interes / 100)`.

---

### Prueba automática incluida
```powershell
.\mvnw.cmd test
```
Verifica 401 sin token, registro/login, creación con total, que otro prestamista no vea el préstamo, marcado pagado, resumen y borrado. Usa H2 en memoria; la API normal sigue usando PostgreSQL.

---

## 4) Variables de entorno

| Variable | Ejemplo | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/prestacontrol` | Cadena JDBC (en Docker se usa `db` en vez de `localhost`) |
| `DB_USER` | `prestacontrol` | Usuario de PostgreSQL |
| `DB_PASSWORD` | `prestacontrol123` | Contraseña de PostgreSQL |
| `JWT_SECRET` | frase de 32+ caracteres | Firma del JWT |
| `JWT_EXP_MS` | `86400000` | Vigencia en milisegundos (24 h) |
| `PORT` | `8080` | Puerto (Render inyecta el suyo) |

---

## 5) Publicar en Render (Opción A de la guía)

1. Sube esta carpeta a GitHub.
2. En Render → **New +** → **Web Service** → conecta el repo.
3. Runtime: **Docker**. Render usa el `Dockerfile` de aquí.
4. Crea un **PostgreSQL** en Render y copia su **Internal Database URL**.
5. En el Web Service → **Environment**, agrega:
   - `DB_URL` = la URL interna de Render cambiando `postgres://` por `jdbc:postgresql://` (ej.: `jdbc:postgresql://host:5432/bd?sslmode=require`).
   - `DB_USER`, `DB_PASSWORD` = los de tu BD de Render.
   - `JWT_SECRET` = frase larga tuya.
   - `JWT_EXP_MS` = `86400000`.
6. Deploy. Copia la URL pública y ponla en `baseUrl` de Postman y en `lib/config/api_config.dart` de la app Flutter.

> Si Render te da `DATABASE_URL`, convierte el prefijo a `jdbc:postgresql://` antes de ponerlo en `DB_URL`.

---

## 6) Estructura de capas

```text
src/main/java/com/santacruz/prestamoservice/
├── config/       SecurityConfig (BCrypt, stateless, 401, CORS)
├── controller/   AuthController, PrestamoController
├── dto/          RegistroRequest, LoginRequest, AuthResponse,
│                 PrestamoRequest, PrestamoResponse, ResumenResponse, ApiError
├── entity/       Usuario, Prestamo
├── exception/    NoAutorizadoException, NoEncontradoException,
│                 ConflictoException, ManejadorErrores
├── repository/   UsuarioRepository, PrestamoRepository
├── security/     FiltroJwt, ManejadorAutenticacion
└── service/      JwtService, AuthService, PrestamoService
```

---

## 7) Problemas comunes

- **No conecta a la BD:** espera a que `docker compose ps` muestre `healthy` en `db`.
- **401 en todo:** ejecuta primero login y comprueba que la variable `token` no esté vacía.
- **`WeakKeyException`:** `JWT_SECRET` debe tener al menos 32 caracteres.
- **Puerto ocupado:** cambia `"8080:8080"` por `"8081:8080"` en `docker-compose.yml` y actualiza `baseUrl`.
