# Lista de verificación y preguntas del flujo del token

## Lista contra la guía (nivel Excelente)

### Ejercicio 1 — API `prestamoservice`
- [x] Group id `com.santacruz`, artifact `prestamoservice`, package `com.santacruz.prestamoservice`.
- [x] Capas: entity, repository, dto, service, controller, security y config.
- [x] Tablas `usuario` y `prestamo` con los campos y tipos pedidos; relación 1:N.
- [x] 7 endpoints: register, login, listar, obtener, crear, actualizar, eliminar.
- [x] Contraseñas cifradas con BCrypt.
- [x] Login/registro devuelven JWT firmado, con `sub` = email, `rol`, `iat` y `exp`.
- [x] API `stateless`, sin sesiones; cabecera `Authorization: Bearer <token>`.
- [x] Sin token / token alterado / vencido → **401** con JSON propio.
- [x] El dueño se toma del token (`authentication.getName()`), nunca de URL ni del body.
- [x] Préstamo de otro prestamista → 404 (no se filtra su existencia).
- [x] Cada préstamo incluye `totalACobrar = monto + monto*interés/100`.
- [x] `JWT_SECRET`, `DB_URL`, `DB_USER`, `DB_PASSWORD` por variables de entorno.
- [x] `Dockerfile` + `docker-compose.yml` con PostgreSQL 16.
- [x] Colección Postman con 7 endpoints, token en variable y pruebas de 401.
- [x] Instrucciones para publicar en Render (Opción A). Tú debes hacer el deploy con tu cuenta.
- [x] Prueba automática `mvnw test` (401, registro, login, CRUD, aislamiento y total).

### Ejercicio 2 — App `prestacontrol_app`
- [x] Package `com.santacruz.prestacontrol_app`, Flutter stable + Dart 3 + Material 3.
- [x] Registro e login con mensajes de error claros.
- [x] Token guardado con `flutter_secure_storage`; sesión se mantiene al reabrir.
- [x] Lista con cliente, monto, interés, total a cobrar y vencimiento.
- [x] Registrar y editar con validaciones (DNI de 8 dígitos, monto > 0, interés ≥ 0).
- [x] Marcar pagado/pendiente y anular con confirmación.
- [x] Cerrar sesión y borrar token; 401 automático vuelve al login.
- [x] Permiso INTERNET y `usesCleartextTraffic` en Android.
- [x] Retos complementarios (4): resumen de cartera, pestañas y morosos en rojo, aviso de expiración, búsqueda.
- [x] Instrucciones para emulador (`10.0.2.2`) y celular físico (IP Wi-Fi).
- [x] APK de depuración generado (`prestacontrol_app-debug.apk` en el Escritorio).
- [ ] Instalar el APK en tu celular y presentarlo: depende de tu dispositivo.

## 10 preguntas que el profesor podría hacerte (con respuesta)

1. **¿Qué es un JWT y qué tiene?**  
   Es un token firmado con tres partes: cabecera, payload y firma. En jwt.io puedes ver el payload con `sub` (email), `rol`, `iat` y `exp`.

2. **¿La API guarda la contraseña en texto plano?**  
   No. Al registrar se cifra con BCrypt. En login solo se compara con `passwordEncoder.matches`.

3. **¿Cómo se envía el token en cada petición?**  
   En la cabecera `Authorization: Bearer <token>`. La app lo hace en `ApiService`.

4. **¿Qué hace el filtro JWT?**  
   Lee la cabecera, valida firma y expiración; si es válido, pone el email como autenticación en `SecurityContextHolder`.

5. **¿Por qué responde 401 sin token?**  
   Porque `/prestamos/**` está en `anyRequest().authenticated()` y `ManejadorAutenticacion` escribe el 401 en JSON.

6. **¿Cómo sabe la API de quién es el préstamo si no envío el usuario en el body?**  
   El controller recibe `Authentication` y usa `getName()` (el email del `sub`). Con ese email busca el `usuario_id`.

7. **¿Qué pasaría si uso el id de un préstamo de otro prestamista?**  
   El repositorio busca `findByIdAndUsuarioId`; como no coincide, devuelve 404. Nadie ve ni edita cartera ajena.

8. **¿La API usa sesiones del servidor?**  
   No. Es `stateless`: cada petición trae su token. El servidor no guarda quién está logueado.

9. **¿Qué hace la app cuando recibe 401?**  
   `ApiService` lanza `SesionExpiradaException`, se borra el token de `flutter_secure_storage` y vuelve al login.

10. **¿Cómo se calcula el total a cobrar y para qué sirve `exp` en la app?**  
    Total = `monto + (monto × interés / 100)`. El `exp` se lee en `TokenInfo` para avisar si el token vence en menos de 2 horas.
