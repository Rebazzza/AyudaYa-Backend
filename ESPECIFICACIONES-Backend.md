# AyudaYa-Backend — Especificaciones funcionales

Backend REST (`AyudaYa-Backend`, Spring Boot 4.1 / Java 21 / MySQL) de la plataforma de gestión de donaciones de emergencia.

## 1. Autenticación y usuarios

- **Registro** (`POST /api/v1/auth/register`): crea usuario con rol Donante / Personal de Apoyo / Administrador; valida DNI (8 dígitos) y correo/DNI únicos; contraseña cifrada con BCrypt.
- **Login** (`POST /api/v1/auth/login`): valida credenciales contra correo + contraseña cifrada.
- **Gestión de usuarios** (`/api/v1/usuarios`): listar, obtener por ID, crear y actualizar (datos, contraseña y rol).

## 2. Donaciones en especie (ciclo de vida completo)

- **Registrar** donación con código de seguimiento autogenerado (`DON-AAAA-XXXXXX`), estado inicial `REGISTRADO` y sus detalles/artículos por categoría.
- **Listar** con filtros opcionales por donante y/o estado.
- **Obtener por ID / por código de seguimiento** (para prellenar corroboración de almacén).
- **Actualizar** cabecera + reemplazo de detalles.
- **Cambiar estado** con registro automático en historial y notificación por correo.
- **Anular** solo el donante dueño y solo si está `REGISTRADO`.
- **Eliminar** donación y detalles.
- **Historial del donante** (orden descendente por fecha).

## 3. Seguimiento y ubicación GPS

- **Tracking por código** (`GET /tracking/{codigo}`): línea de tiempo cronológica de estados.
- **Escaneo de ubicación** (`POST /api/v1/ubicaciones/escaneo`): el trabajador captura GPS, actualiza estado y local; si es `ENTREGADO` fija fecha de verificación.
- **Ubicación actual** para el mapa del donante (GPS + centro de acopio).

## 4. Almacén (recepción e inventario)

- **Corroborar recepción**: compara declarado vs. verificado, clasifica incidencias (`Conforme` / `Excedente` / `Faltante`), registra GPS y pasa a `EN_ALMACEN`; si >50% de ítems tienen incidencia, notifica a administradores.
- **Inventario por local**: stock verificado por categoría (suma de donaciones en `EN_ALMACEN`).
- **Alertas de caducidad**: insumos con vencimiento a menos de 15 días.

## 5. Kits de ayuda

- **Armar 1..N kits** (`POST /api/v1/kits/armar`): valida stock suficiente por categoría, descuenta stock en FIFO (por fecha de vencimiento), genera código `KIT-AAAA-XXXXXX`, estado `DISPONIBLE`.
- **Listar kits por local** y **obtener kit por código**.

## 6. Donaciones monetarias

- **Registrar** (queda `PENDIENTE`) validando número de operación único; moneda por defecto PEN.
- **Verificar/rechazar** fondos (solo `VERIFICADO` / `RECHAZADO`).
- **Total recaudado** de donaciones verificadas.

## 7. Locales de recepción

- CRUD: listar solo activos (para que el donante elija), registrar (capacidad en m3 > 0, activo por defecto), actualizar (incluye latitud/longitud) y eliminar.

## 8. Trabajadores de centros de acopio

- CRUD: registrar/actualizar vincula un usuario a un local con cargo y fecha de contratación; valida duplicados.

## 9. Categorías de insumos

- CRUD completo (alimentos, medicinas, etc., con unidad de medida y refrigeración).

## 10. Notificaciones

- Listar (por destinatario), por ID, crear, marcar como leída y eliminar. Generación automática hacia administradores ante incidencias >50%.

## 11. Historial de estados (auditoría)

- Listar todo, por donación, por ID, registrar manualmente y eliminar.

## 12. Imágenes / evidencias

- Subir imagen (DNI frontal / posterior, evidencia de recepción / entrega) con validación de formato (JPG/PNG/WebP) y almacenamiento físico.
- Subir máximo 3 evidencias de recepción por donación.
- Listar imágenes de una donación y eliminar (archivo físico + registro).

## 13. Documentos y comunicaciones

- **Comprobante PDF** de donación: detalle de insumos, timeline de movimientos y QR de seguimiento empotrado.
- **Etiquetas QR masivas** (hoja 3x4) por códigos de seguimiento.
- **Correo de notificación de estado** vía SMTP con plantilla Thymeleaf y enlace al portal (`/seguimiento/{codigo}`), disparado en cada cambio de estado / anulación.
- **Utilidad de prueba** de envío de correo.

## 14. Soporte técnico

- Swagger/OpenAPI UI en `/swagger-ui.html`.
- CORS habilitado, manejador global de excepciones (respuestas uniformes `ApiResponseDTO`), cifrado de contraseñas con BCrypt.
- Seed automático de roles (Donante, Personal de Apoyo, TrabajadorCentroAcopio, Organización, Administrador).
- BD MySQL (`db_donaciones_emergent`) con auto-create y `ddl-auto=update`; subida de imágenes hasta 10 MB.