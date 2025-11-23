# REQUERIMIENTOS FUNCIONALES DEL SISTEMA DE ASISTENCIA UPeU

## MÓDULO: AUTENTICACIÓN Y GESTIÓN DE SESIONES

### Requerimiento: RF-AUT-001 – Inicio de Sesión mediante Credenciales

**Descripción:** El sistema debe permitir a los usuarios autenticarse mediante username y contraseña, validando las credenciales contra la base de datos y generando un token JWT para mantener la sesión activa.

**Historia de usuario:**
Como usuario del sistema, quiero iniciar sesión con mi username y contraseña para acceder a las funcionalidades según mi rol asignado.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Usuario del sistema (ADMIN, LIDER, INTEGRANTE, SUPERADMIN) |
| **Acciones del Usuario** | - Ingresar username en campo de texto<br>- Ingresar contraseña en campo de texto protegido<br>- Hacer clic en botón "Iniciar Sesión"<br>- Visualizar mensaje de error si las credenciales son incorrectas |
| **Requisitos técnicos** | - Endpoint REST: POST `/users/login`<br>- Request Body: `UsuarioDTO.CredencialesDto { user: String, clave: String }`<br>- Validación de campos obligatorios con `@NotBlank`<br>- Spring Security para validación de credenciales<br>- BCrypt para comparar hash de contraseña<br>- Generación de JWT token con expiración de 5 horas<br>- Response: `UsuarioDTO` con token JWT, idUsuario, estado, personaId<br>- Almacenamiento de token en `localStorage` del navegador<br>- Interceptor Axios para agregar token automáticamente a peticiones |
| **Condiciones adicionales** | - El usuario debe existir en la tabla `upeu_usuario`<br>- La contraseña debe coincidir con el hash almacenado<br>- El usuario debe tener estado "ACTIVO"<br>- El usuario debe tener al menos un rol asignado en `upeu_usuario_rol`<br>- Si el token es inválido o expirado, se redirige al login |
| **Diseño de arquitectura** | **Controller:** `AuthController.login()` → **Service:** `IUsuarioService.login()` → **Repository:** `IUsuarioRepository.findOneByUser()`, `IUsuarioRolRepository.findOneByUsuarioUser()` → **Security:** `JwtUserDetailsService.loadUserByUsername()`, `JwtTokenUtil.generateToken()`<br>**Tablas:** `upeu_usuario` (id_usuario, user, clave, estado), `upeu_usuario_rol` (usuario_id, rol_id), `upeu_roles` (id_rol, nombre) |

---

### Requerimiento: RF-AUT-002 – Registro de Nuevos Usuarios

**Descripción:** El sistema debe permitir el registro de nuevos usuarios proporcionando información personal (nombre completo, correo, documento) y credenciales de acceso, creando automáticamente las entidades Usuario y Persona asociadas.

**Historia de usuario:**
Como usuario nuevo, quiero registrarme en el sistema proporcionando mis datos personales y credenciales para poder acceder posteriormente.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Usuario nuevo (no autenticado) |
| **Acciones del Usuario** | - Completar formulario con: username, nombre completo, correo electrónico, documento, contraseña<br>- Confirmar contraseña<br>- Hacer clic en botón "Registrarse"<br>- Visualizar mensaje de confirmación o error |
| **Requisitos técnicos** | - Endpoint REST: POST `/users/register`<br>- Request Body: `UsuarioDTO.UsuarioCrearDto { user, nombreCompleto, correo, documento, clave, rol (opcional), estado (opcional) }`<br>- Validación con `@NotBlank`, `@Email`<br>- Verificación de unicidad de username, documento, correo<br>- Hash de contraseña con BCrypt antes de almacenar<br>- Creación de entidad `Persona` en tabla `upeu_persona`<br>- Creación de entidad `Usuario` en tabla `upeu_usuario`<br>- Asignación de rol por defecto "INTEGRANTE" si no se especifica<br>- Asignación de estado "ACTIVO" por defecto<br>- Generación automática de token JWT tras registro exitoso |
| **Condiciones adicionales** | - El username debe ser único (constraint UNIQUE en BD)<br>- El documento debe ser único (constraint UNIQUE en BD)<br>- El correo debe tener formato válido<br>- El rol especificado debe existir en `upeu_roles`<br>- Si el rol no existe, se asigna "INTEGRANTE" por defecto<br>- La contraseña debe tener longitud mínima (definida en validación) |
| **Diseño de arquitectura** | **Controller:** `AuthController.register()` → **Service:** `IUsuarioService.register()` → **Repositories:** `IUsuarioRepository`, `IPersonaRepository`, `IRolRepository`, `IUsuarioRolRepository`<br>**Tablas:** `upeu_usuario` (id_usuario, user, clave, estado), `upeu_persona` (id_persona, nombre_completo, documento, correo, usuario_id), `upeu_usuario_rol` (usuario_id, rol_id), `upeu_roles` (id_rol, nombre) |

---

### Requerimiento: RF-AUT-003 – Validación de Token JWT en Peticiones

**Descripción:** El sistema debe validar automáticamente el token JWT en cada petición HTTP al backend para permitir o denegar el acceso a recursos protegidos.

**Historia de usuario:**
Como sistema, quiero validar el token JWT en cada petición para asegurar que solo usuarios autenticados accedan a los recursos protegidos.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Sistema (filtro de seguridad), Cliente HTTP (frontend) |
| **Acciones del Sistema** | - Interceptar todas las peticiones HTTP al backend<br>- Extraer token del header `Authorization: Bearer {token}`<br>- Validar formato, firma, y expiración del token<br>- Cargar detalles del usuario desde el token<br>- Establecer contexto de seguridad de Spring<br>- Permitir o denegar el acceso según validación |
| **Requisitos técnicos** | - Spring Security Filter: `JwtRequestFilter` (extends `OncePerRequestFilter`)<br>- Interceptor Axios en frontend: agrega token automáticamente<br>- Componente `JwtTokenUtil` para validación<br>- Validación de formato JWT (3 partes separadas por punto)<br>- Validación de firma con clave secreta<br>- Validación de expiración (claim `exp`)<br>- Extracción de username del claim `sub`<br>- Extracción de roles del claim `role`<br>- `JwtUserDetailsService` para cargar `UserDetails`<br>- Establecimiento de `SecurityContext` con autenticación |
| **Condiciones adicionales** | - Rutas públicas (`/users/login`, `/users/register`) no requieren token<br>- Si el token no existe, se retorna 401 Unauthorized<br>- Si el token está expirado, se retorna 401 y se limpia del localStorage<br>- Si el token es inválido, se retorna 401<br>- El token debe contener claims: `sub` (username), `role` (roles), `exp` (expiración) |
| **Diseño de arquitectura** | **Filter:** `JwtRequestFilter.doFilterInternal()` → **Util:** `JwtTokenUtil.validateToken()`, `JwtTokenUtil.getUsernameFromToken()` → **Service:** `JwtUserDetailsService.loadUserByUsername()` → **Security:** `SecurityContextHolder.setAuthentication()`<br>**Configuración:** `WebSecurityConfig.filterChain()` define orden de filtros y rutas públicas |

---

## MÓDULO: GESTIÓN DE USUARIOS Y ROLES

### Requerimiento: RF-USR-001 – Construcción de Menú Dinámico según Rol

**Descripción:** El sistema debe proporcionar un menú de navegación personalizado según el rol del usuario autenticado, consultando los accesos asignados a ese rol desde la base de datos.

**Historia de usuario:**
Como usuario autenticado, quiero ver solo las opciones de menú correspondientes a mi rol para acceder únicamente a las funcionalidades autorizadas.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Usuario autenticado (cualquier rol), Componente Sidebar (frontend) |
| **Acciones del Usuario** | - Iniciar sesión en el sistema<br>- Visualizar menú lateral (Sidebar) con opciones según su rol<br>- Expandir/colapsar grupos de menú<br>- Navegar a páginas desde items del menú |
| **Requisitos técnicos** | - Endpoint REST: POST `/accesos/menu`<br>- Request Body: String (username)<br>- Response: `List<MenuGroup>` donde `MenuGroup` contiene `id`, `name`, `icon`, `path`, `items` (List<MenuItem>)<br>- Consulta SQL JOIN: `upeu_usuario` → `upeu_usuario_rol` → `upeu_roles` → `upeu_acceso_rol` → `upeu_accesos`<br>- Agrupación de accesos en `MenuGroup` según lógica de negocio<br>- Mapeo de iconos Font Awesome desde campo `icono` de tabla `upeu_accesos`<br>- Frontend: `menuService.getMenuByUser()` → renderizado en `Sidebar.jsx` |
| **Condiciones adicionales** | - El usuario debe estar autenticado (token JWT válido)<br>- Los accesos deben estar asignados al rol mediante `upeu_acceso_rol`<br>- Los accesos se agrupan dinámicamente (ej: "Gestión Académica", "Eventos")<br>- Items sin grupo aparecen como links directos<br>- El menú se actualiza al cambiar de usuario o rol |
| **Diseño de arquitectura** | **Controller:** `AccesoController.getMenuByUser()` → **Service:** `IAccesoService.getMenuByUser()` → **Repository:** `IAccesoRepository.getAccesoByUser()` (consulta SQL nativa con JOINs)<br>**Tablas:** `upeu_usuario`, `upeu_usuario_rol`, `upeu_roles`, `upeu_acceso_rol`, `upeu_accesos` (id_acceso, nombre, url, icono)<br>**Frontend:** `Sidebar.jsx` renderiza `MenuGroup` con `MenuItem`, usa React Router para navegación |

---

### Requerimiento: RF-USR-002 – Consulta de Usuarios por Rol

**Descripción:** El sistema debe permitir consultar la lista de usuarios filtrados por un rol específico, útil para asignar líderes a grupos o gestionar usuarios por categoría.

**Historia de usuario:**
Como administrador, quiero consultar todos los usuarios con rol "LIDER" para poder asignarlos a grupos pequeños.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador, Sistema de asignación de líderes |
| **Acciones del Administrador** | - Seleccionar un rol específico (ej: "LIDER")<br>- Solicitar listado de usuarios con ese rol<br>- Visualizar lista de usuarios con información relevante |
| **Requisitos técnicos** | - Endpoint REST: GET `/users/rol/{rolNombre}`<br>- Path Variable: `rolNombre` (String): "ADMIN", "LIDER", "INTEGRANTE", "SUPERADMIN"<br>- Response: `List<UsuarioDTO>` con idUsuario, user, estado<br>- Consulta SQL JOIN: `upeu_usuario` → `upeu_usuario_rol` → `upeu_roles` WHERE nombre = rolNombre<br>- Validación de existencia del rol |
| **Condiciones adicionales** | - El rol especificado debe existir en `upeu_roles`<br>- Solo usuarios con estado "ACTIVO" deben ser considerados (si aplica)<br>- Se requiere autenticación JWT válida<br>- El usuario debe tener permisos para consultar usuarios |
| **Diseño de arquitectura** | **Controller:** `UsuarioController.getUsuariosPorRol()` → **Service:** `IUsuarioService.findByRol()` → **Repository:** `IUsuarioRepository.findByRol()` (consulta SQL nativa con JOINs)<br>**Tablas:** `upeu_usuario`, `upeu_usuario_rol`, `upeu_roles` |

---

### Requerimiento: RF-USR-003 – Consulta de Líderes Disponibles

**Descripción:** El sistema debe permitir consultar la lista de personas con rol "LIDER" que no están asignadas actualmente a ningún grupo pequeño, excluyendo opcionalmente un grupo específico (útil para reasignaciones).

**Historia de usuario:**
Como administrador, quiero consultar los líderes disponibles (no asignados a grupos) para poder asignarlos a nuevos grupos pequeños.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Solicitar listado de líderes disponibles<br>- Opcionalmente excluir un grupo específico (para reasignaciones)<br>- Visualizar lista de líderes con información de persona |
| **Requisitos técnicos** | - Endpoint REST: GET `/users/lideres-disponibles?excludeGrupoId={id}`<br>- Query Parameter: `excludeGrupoId` (Long, opcional)<br>- Response: `List<PersonaDTO>` con idPersona, nombreCompleto, codigoEstudiante, documento<br>- Consulta SQL compleja: JOIN `upeu_persona` → `upeu_usuario` → `upeu_usuario_rol` → `upeu_roles` WHERE nombre = 'LIDER'<br>- Exclusión: LEFT JOIN con `upeu_grupo_pequeno` WHERE lider_id NOT IN (lideres ya asignados) o WHERE grupo.id != excludeGrupoId |
| **Condiciones adicionales** | - Solo se consideran personas con rol "LIDER" activo<br>- Solo se muestran líderes no asignados a grupos (o excluyendo el grupo especificado)<br>- La persona debe tener usuario asociado<br>- Se requiere autenticación JWT válida |
| **Diseño de arquitectura** | **Controller:** `UsuarioController.getLideresDisponibles()` → **Service:** `IUsuarioService.getLideresDisponibles()` → **Repository:** `IPersonaRepository.findLideresDisponibles()` (consulta SQL nativa con múltiples JOINs y subconsulta)<br>**Tablas:** `upeu_persona`, `upeu_usuario`, `upeu_usuario_rol`, `upeu_roles`, `upeu_grupo_pequeno` (lider_id) |

---

## MÓDULO: GESTIÓN DE EVENTOS

### Requerimiento: RF-EVT-001 – Creación de Evento General

**Descripción:** El sistema debe permitir a los administradores crear eventos generales que agrupan múltiples sesiones, asociando cada evento a un periodo académico y un programa de estudio.

**Historia de usuario:**
Como administrador, quiero crear un evento general (ej: "SAV 2025-I") asociándolo a un periodo y programa para luego poder crear sesiones individuales dentro de ese evento.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Ingresar nombre del evento<br>- Ingresar descripción (opcional)<br>- Seleccionar periodo académico<br>- Seleccionar programa de estudio<br>- Definir fechas de inicio y fin del evento<br>- Definir lugar (opcional)<br>- Guardar evento |
| **Requisitos técnicos** | - Endpoint REST: POST `/eventos-generales`<br>- Request Body: `EventoGeneralDTO { nombre, descripcion, lugar, fechaInicio, fechaFin, periodoId, programaId, estado }`<br>- Validación: `@NotBlank` para nombre, `@NotNull` para periodoId, programaId, fechas<br>- Mapeo DTO → Entity mediante `EventoGeneralMapper`<br>- Persistencia en tabla `upeu_evento_general`<br>- Validación de existencia de Periodo y ProgramaEstudio<br>- Estado por defecto: "ACTIVO"<br>- Campos auditables: `created_at`, `updated_at` con `@PrePersist`, `@PreUpdate` |
| **Condiciones adicionales** | - El periodo debe existir en `upeu_periodo`<br>- El programa debe existir en `upeu_programa_estudio`<br>- La fecha de inicio debe ser anterior a la fecha de fin<br>- Solo puede haber un evento activo por periodo-programa (regla de negocio, si aplica)<br>- El usuario debe tener rol ADMIN o SUPERADMIN |
| **Diseño de arquitectura** | **Controller:** `EventoGeneralController.save()` → **Service:** `IEventoGeneralService.save()` → **Repositories:** `IEventoGeneralRepository`, `IPeriodoRepository`, `IProgramaEstudioRepository`<br>**Tablas:** `upeu_evento_general` (id_evento_general, nombre, descripcion, lugar, fecha_inicio, fecha_fin, periodo_id, programa_id, estado, created_at, updated_at), `upeu_periodo`, `upeu_programa_estudio` |

---

### Requerimiento: RF-EVT-002 – Creación de Sesiones Individuales (Eventos Específicos)

**Descripción:** El sistema debe permitir crear sesiones individuales dentro de un evento general, cada una con fecha, hora de inicio, hora de fin, lugar, y tolerancia en minutos para el registro de asistencia.

**Historia de usuario:**
Como administrador, quiero crear una sesión individual dentro de un evento general con fecha y horario específicos para que los líderes puedan generar códigos QR para esa sesión.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Seleccionar evento general<br>- Ingresar nombre de la sesión<br>- Seleccionar fecha<br>- Definir hora de inicio y hora de fin<br>- Definir lugar<br>- Definir tolerancia en minutos (default: 10)<br>- Guardar sesión |
| **Requisitos técnicos** | - Endpoint REST: POST `/eventos-especificos`<br>- Request Body: `EventoEspecificoDTO { eventoGeneralId, nombreSesion, fecha, horaInicio, horaFin, lugar, descripcion, toleranciaMinutos, estado }`<br>- Validación: `@NotNull` para eventoGeneralId, fecha, horas<br>- Mapeo DTO → Entity mediante `EventoEspecificoMapper`<br>- Persistencia en tabla `upeu_evento_especifico`<br>- Validación de existencia de EventoGeneral<br>- Validación de que la fecha esté dentro del rango del evento general<br>- Estado por defecto: "PROGRAMADO"<br>- Tolerancia por defecto: 10 minutos |
| **Condiciones adicionales** | - El evento general debe existir<br>- La fecha debe estar entre fechaInicio y fechaFin del evento general<br>- La hora de inicio debe ser anterior a la hora de fin<br>- Tolerancia debe ser un número positivo<br>- El usuario debe tener rol ADMIN o SUPERADMIN |
| **Diseño de arquitectura** | **Controller:** `EventoEspecificoController.save()` → **Service:** `IEventoEspecificoService.save()` → **Repositories:** `IEventoEspecificoRepository`, `IEventoGeneralRepository`<br>**Tablas:** `upeu_evento_especifico` (id_evento_especifico, evento_general_id, nombre_sesion, fecha, hora_inicio, hora_fin, lugar, descripcion, tolerancia_minutos, estado, created_at), `upeu_evento_general` |

---

### Requerimiento: RF-EVT-003 – Creación de Sesiones Recurrentes

**Descripción:** El sistema debe permitir crear múltiples sesiones de forma automática mediante un patrón de recurrencia (días de la semana y rango de fechas), útil para eventos semanales como "SAV" que se realizan los mismos días cada semana.

**Historia de usuario:**
Como administrador, quiero crear sesiones recurrentes para un evento (ej: todos los lunes de marzo a julio) para evitar crear cada sesión manualmente.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Seleccionar evento general<br>- Ingresar nombre base de sesión<br>- Seleccionar fecha de inicio y fin de recurrencia<br>- Definir hora de inicio y hora de fin<br>- Seleccionar días de la semana (ej: lunes, miércoles)<br>- Definir tolerancia<br>- Guardar recurrencia |
| **Requisitos técnicos** | - Endpoint REST: POST `/eventos-especificos/recurrencia`<br>- Request Body: `RecurrenceRequestDTO { idEventoGeneral, nombreSesion, fechaInicioRecurrencia, fechaFinRecurrencia, horaInicio, horaFin, toleranciaMinutos, diasSemana (List<Integer>), lugar, descripcion }`<br>- Validación: `@NotNull` para campos obligatorios<br>- Lógica de generación: iterar sobre rango de fechas, filtrar por días de semana especificados<br>- Creación múltiple: para cada fecha válida, crear un `EventoEspecifico`<br>- Nomenclatura de sesión: "{nombreSesion} {fecha}" (ej: "SAV Lunes 5 enero")<br>- Response: `List<EventoEspecificoDTO>` con todas las sesiones creadas |
| **Condiciones adicionales** | - El evento general debe existir<br>- El rango de fechas debe estar dentro del rango del evento general<br>- Los días de semana se representan como números (1=Lunes, 7=Domingo)<br>- Si lugar/descripción están vacíos, se usan los del evento general<br>- No se deben crear sesiones duplicadas para la misma fecha |
| **Diseño de arquitectura** | **Controller:** `EventoEspecificoController.createRecurrence()` → **Service:** `IEventoEspecificoService.createRecurrence()` → **Repositories:** `IEventoEspecificoRepository`, `IEventoGeneralRepository`<br>**Lógica:** Cálculo de fechas válidas mediante `LocalDate` y `DayOfWeek`, creación transaccional de múltiples entidades<br>**Tablas:** `upeu_evento_especifico` (múltiples registros creados en una transacción) |

---

### Requerimiento: RF-EVT-004 – Consulta de Sesiones por Fecha

**Descripción:** El sistema debe permitir consultar todas las sesiones programadas para una fecha específica, útil para que los líderes vean las sesiones del día actual.

**Historia de usuario:**
Como líder, quiero consultar las sesiones programadas para hoy para poder generar códigos QR o registrar asistencias.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Líder, Sistema |
| **Acciones del Líder** | - Solicitar sesiones del día actual<br>- Visualizar lista de sesiones con información relevante<br>- Seleccionar una sesión para acciones (generar QR, ver lista) |
| **Requisitos técnicos** | - Endpoint REST: GET `/eventos-especificos/fecha?fecha={YYYY-MM-DD}`<br>- Query Parameter: `fecha` (LocalDate, formato ISO 8601)<br>- Response: `List<EventoEspecificoDTO>` con información de sesiones<br>- Consulta JPA: `findByFecha(LocalDate fecha)`<br>- Mapeo Entity → DTO mediante `EventoEspecificoMapper`<br>- Inclusión de nombre del evento general en DTO |
| **Condiciones adicionales** | - La fecha debe estar en formato ISO (YYYY-MM-DD)<br>- Se retornan todas las sesiones de esa fecha, independientemente del estado<br>- Solo sesiones de eventos activos deben mostrarse (si aplica regla de negocio)<br>- Se requiere autenticación JWT válida |
| **Diseño de arquitectura** | **Controller:** `EventoEspecificoController.findByFecha()` → **Service:** `IEventoEspecificoService.findByFecha()` → **Repository:** `IEventoEspecificoRepository.findByFecha()` (método derivado JPA)<br>**Tablas:** `upeu_evento_especifico` (consulta por campo `fecha`) |

---

## MÓDULO: GESTIÓN DE GRUPOS

### Requerimiento: RF-GRP-001 – Creación de Grupo General

**Descripción:** El sistema debe permitir crear grupos generales (categorías de organización) asociados a un evento general, que luego contendrán múltiples grupos pequeños.

**Historia de usuario:**
Como administrador, quiero crear un grupo general (ej: "Grupo A", "Grupo B") dentro de un evento para organizar a los participantes en categorías.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Seleccionar evento general<br>- Ingresar nombre del grupo general<br>- Ingresar descripción (opcional)<br>- Guardar grupo general |
| **Requisitos técnicos** | - Endpoint REST: POST `/grupos-generales`<br>- Request Body: `GrupoGeneralDTO { eventoGeneralId, nombre, descripcion }`<br>- Validación: `@NotNull` para eventoGeneralId y nombre<br>- Mapeo DTO → Entity mediante `GrupoGeneralMapper`<br>- Persistencia en tabla `upeu_grupo_general`<br>- Validación de existencia de EventoGeneral<br>- Campo audit: `created_at` con `@PrePersist` |
| **Condiciones adicionales** | - El evento general debe existir<br>- El nombre debe ser único dentro del evento (regla de negocio, si aplica)<br>- El usuario debe tener rol ADMIN o SUPERADMIN |
| **Diseño de arquitectura** | **Controller:** `GrupoGeneralController.save()` → **Service:** `IGrupoGeneralService.save()` → **Repositories:** `IGrupoGeneralRepository`, `IEventoGeneralRepository`<br>**Tablas:** `upeu_grupo_general` (id_grupo_general, evento_general_id, nombre, descripcion, created_at), `upeu_evento_general` |

---

### Requerimiento: RF-GRP-002 – Creación de Grupo Pequeño con Asignación de Líder

**Descripción:** El sistema debe permitir crear grupos pequeños dentro de un grupo general, asignando un líder específico y definiendo la capacidad máxima de participantes.

**Historia de usuario:**
Como administrador, quiero crear un grupo pequeño dentro de un grupo general asignándole un líder para que ese líder pueda gestionar las asistencias de los participantes.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Seleccionar grupo general<br>- Ingresar nombre del grupo pequeño<br>- Seleccionar líder de la lista de líderes disponibles<br>- Definir capacidad máxima (default: 20)<br>- Ingresar descripción (opcional)<br>- Guardar grupo pequeño |
| **Requisitos técnicos** | - Endpoint REST: POST `/grupos-pequenos`<br>- Request Body: `GrupoPequenoDTO { grupoGeneralId, nombre, liderId, capacidadMaxima, descripcion }`<br>- Validación: `@NotNull` para grupoGeneralId, nombre, liderId<br>- Mapeo DTO → Entity mediante `GrupoPequenoMapper`<br>- Persistencia en tabla `upeu_grupo_pequeno`<br>- Validación de existencia de GrupoGeneral y Persona (líder)<br>- Validación de que el líder tenga rol "LIDER"<br>- Validación de que el líder no esté asignado a otro grupo del mismo evento |
| **Condiciones adicionales** | - El grupo general debe existir<br>- El líder debe existir y tener rol "LIDER"<br>- El líder no debe estar asignado a otro grupo del mismo evento<br>- La capacidad máxima debe ser un número positivo<br>- El usuario debe tener rol ADMIN o SUPERADMIN |
| **Diseño de arquitectura** | **Controller:** `GrupoPequenoController.save()` → **Service:** `IGrupoPequenoService.save()` → **Repositories:** `IGrupoPequenoRepository`, `IGrupoGeneralRepository`, `IPersonaRepository`, `IUsuarioRolRepository`<br>**Tablas:** `upeu_grupo_pequeno` (id_grupo_pequeno, grupo_general_id, nombre, lider_id, capacidad_maxima, descripcion, created_at), `upeu_grupo_general`, `upeu_persona` |

---

### Requerimiento: RF-GRP-003 – Agregar Participante a Grupo Pequeño

**Descripción:** El sistema debe permitir agregar participantes (personas matriculadas en el evento) a un grupo pequeño, validando que no estén duplicados y que el grupo no haya alcanzado su capacidad máxima.

**Historia de usuario:**
Como administrador, quiero agregar participantes a un grupo pequeño seleccionándolos de la lista de disponibles para organizar la asistencia.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Seleccionar grupo pequeño<br>- Consultar participantes disponibles del evento<br>- Seleccionar participantes a agregar<br>- Confirmar agregado |
| **Requisitos técnicos** | - Endpoint REST: POST `/grupo-participantes`<br>- Request Body: `GrupoParticipanteDTO { grupoPequenoId, personaId }`<br>- Validación: ambos IDs obligatorios<br>- Service: `IGrupoParticipanteService.agregarParticipante()`<br>- Validaciones de negocio:<br>  - El grupo pequeño debe existir<br>  - La persona debe existir<br>  - La persona debe estar matriculada en el evento del grupo<br>  - La persona no debe estar ya inscrita en el grupo (unique constraint)<br>  - El grupo no debe haber alcanzado su capacidad máxima<br>- Persistencia en tabla `upeu_grupo_participante`<br>- Estado por defecto: "ACTIVO"<br>- Campo audit: `fecha_inscripcion` con `@PrePersist` |
| **Condiciones adicionales** | - Unique constraint en BD: `(grupo_pequeno_id, persona_id)` previene duplicados<br>- La capacidad máxima se calcula contando participantes ACTIVOS<br>- La persona debe estar matriculada en el periodo del evento (validación mediante Matricula) |
| **Diseño de arquitectura** | **Controller:** `GrupoParticipanteController.save()` → **Service:** `IGrupoParticipanteService.agregarParticipante()` → **Repositories:** `IGrupoParticipanteRepository`, `IGrupoPequenoRepository`, `IPersonaRepository`, `IMatriculaRepository` (para validar pertenencia al evento)<br>**Tablas:** `upeu_grupo_participante` (id_grupo_participante, grupo_pequeno_id, persona_id, fecha_inscripcion, estado), `upeu_grupo_pequeno`, `upeu_persona`, `upeu_matricula` |

---

### Requerimiento: RF-GRP-004 – Consulta de Participantes Disponibles

**Descripción:** El sistema debe permitir consultar la lista de personas matriculadas en un evento que están disponibles para ser agregadas a grupos pequeños (no están ya inscritas o están en otro grupo).

**Historia de usuario:**
Como administrador, quiero consultar los participantes disponibles para un evento para poder agregarlos a grupos pequeños.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Seleccionar evento general<br>- Solicitar listado de participantes disponibles<br>- Visualizar lista con información de persona e indicador si ya está inscrito |
| **Requisitos técnicos** | - Endpoint REST: GET `/grupos-pequenos/disponibles/{eventoGeneralId}`<br>- Path Variable: `eventoGeneralId` (Long)<br>- Response: `List<ParticipanteDisponibleDTO>` con personaId, nombreCompleto, codigoEstudiante, documento, correo, yaInscrito, grupoActual<br>- Lógica de servicio: consultar matriculados del periodo del evento, verificar inscripción en grupos, marcar yaInscrito y grupoActual<br>- Consulta SQL compleja: JOIN entre Matricula, Persona, GrupoParticipante, GrupoPequeno, GrupoGeneral |
| **Condiciones adicionales** | - Solo se consideran personas matriculadas en el periodo del evento<br>- Se indica si la persona ya está inscrita en algún grupo<br>- Si está inscrita, se muestra el nombre del grupo actual<br>- Se requiere autenticación JWT válida |
| **Diseño de arquitectura** | **Controller:** `GrupoPequenoController.getParticipantesDisponibles()` → **Service:** `IGrupoPequenoService.getParticipantesDisponibles()` → **Repositories:** `IMatriculaRepository`, `IGrupoParticipanteRepository`, consultas JOIN complejas<br>**Tablas:** `upeu_matricula`, `upeu_persona`, `upeu_grupo_participante`, `upeu_grupo_pequeno`, `upeu_grupo_general`, `upeu_evento_general` |

---

## MÓDULO: GENERACIÓN Y VALIDACIÓN DE CÓDIGOS QR

### Requerimiento: RF-QR-001 – Generación de Código QR para Sesión

**Descripción:** El sistema debe permitir a los líderes generar un código QR único para una sesión específica, conteniendo información estructurada de la sesión (eventoId, fecha, hora, lugar, timestamp) codificada en formato JSON y visualizada como imagen.

**Historia de usuario:**
Como líder, quiero generar un código QR para una sesión programada para hoy para que los integrantes puedan escanearlo y registrar su asistencia.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Líder |
| **Acciones del Líder** | - Seleccionar una sesión del día actual<br>- Hacer clic en "Generar QR"<br>- Visualizar código QR generado en modal<br>- Compartir QR con integrantes (visualmente o impreso) |
| **Requisitos técnicos** | - Endpoint REST: GET `/asistencias/generar-qr/{eventoEspecificoId}/lider/{liderId}`<br>- Path Variables: `eventoEspecificoId` (Long), `liderId` (Long)<br>- Response: `QRResponseDTO { qrImageBase64: String, qrData: QRAsistenciaDTO, mensaje: String }`<br>- Validación: el líder debe ser líder de un grupo asociado al evento de la sesión<br>- Construcción de `QRAsistenciaDTO`: eventoEspecificoId, eventoNombre, sesionNombre, fecha, horaInicio, horaFin, toleranciaMinutos, lugar, timestamp<br>- Serialización: JSON.stringify(qrData) para generar string QR<br>- Generación de imagen QR: biblioteca QR Code (ZXing o similar) genera PNG<br>- Codificación base64: imagen PNG convertida a base64 para transferencia HTTP<br>- Frontend: mostrar imagen desde `data:image/png;base64,...` |
| **Condiciones adicionales** | - El líder debe estar autenticado<br>- El líder debe ser líder de al menos un grupo del evento general<br>- La sesión debe existir y estar programada para el día actual o futuro<br>- El timestamp se usa para validar vigencia del QR posteriormente |
| **Diseño de arquitectura** | **Controller:** `AsistenciaController.generarQR()` → **Service:** `AsistenciaServiceImp.generarQRParaSesion()` → **Repositories:** `IEventoEspecificoRepository`, `IGrupoPequenoRepository` (validar liderazgo)<br>**Librería:** QR Code Generator (ZXing/Java) genera imagen PNG desde JSON string<br>**DTOs:** `QRAsistenciaDTO` (datos del QR), `QRResponseDTO` (respuesta con imagen base64)<br>**Tablas:** `upeu_evento_especifico`, `upeu_grupo_pequeno` (validar liderId) |

---

### Requerimiento: RF-QR-002 – Escaneo y Validación de Código QR

**Descripción:** El sistema debe permitir a los integrantes escanear un código QR usando la cámara de su dispositivo, validar que el QR sea válido, y procesar el registro de asistencia automáticamente.

**Historia de usuario:**
Como integrante, quiero escanear el código QR de una sesión con la cámara de mi dispositivo para registrar mi asistencia automáticamente.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Integrante |
| **Acciones del Integrante** | - Acceder a página "Escanear QR"<br>- Permitir acceso a cámara del dispositivo<br>- Apuntar cámara al código QR<br>- Esperar confirmación de registro de asistencia |
| **Requisitos técnicos** | - Frontend: `EscanearQRPage.jsx` utiliza biblioteca `HTML5-QRCode`<br>- Captura de video: acceso a cámara mediante `navigator.mediaDevices.getUserMedia()`<br>- Decodificación QR: `Html5Qrcode.scan()` decodifica QR y retorna string JSON<br>- Parsing: `JSON.parse(qrText)` convierte string a objeto `QRAsistenciaDTO`<br>- Validación frontend: verificar que el QR contenga `eventoEspecificoId` y `timestamp`<br>- Envío: POST `/asistencias/registrar-qr` con payload `AsistenciaRegistroDTO`<br>- Validación backend: múltiples validaciones antes de registrar (ver RF-ASIS-001) |
| **Condiciones adicionales** | - El dispositivo debe tener cámara disponible<br>- El navegador debe soportar acceso a cámara (HTTPS o localhost)<br>- El usuario debe dar permiso para acceso a cámara<br>- El QR debe ser válido (generado por el sistema)<br>- El formato del QR debe ser JSON con estructura `QRAsistenciaDTO` |
| **Diseño de arquitectura** | **Frontend:** `EscanearQRPage.jsx` → `HTML5-QRCode` library → `asistenciaService.registrarAsistencia()` → POST `/asistencias/registrar-qr`<br>**Backend:** Validación y registro (ver RF-ASIS-001)<br>**Tecnología:** HTML5-QRCode 2.3.8 para escaneo, WebRTC para acceso a cámara |

---

## MÓDULO: REGISTRO DE ASISTENCIAS

### Requerimiento: RF-ASIS-001 – Registro de Asistencia mediante Escaneo QR

**Descripción:** El sistema debe permitir registrar la asistencia de un integrante mediante el escaneo de un código QR, validando múltiples condiciones (fecha, horario permitido, pertenencia al evento, duplicados) antes de persistir el registro.

**Historia de usuario:**
Como integrante, quiero que mi asistencia se registre automáticamente al escanear el QR de una sesión, siempre que cumpla con las validaciones de fecha y horario.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Integrante |
| **Acciones del Integrante** | - Escanear código QR válido (ver RF-QR-002)<br>- Esperar confirmación de registro<br>- Visualizar mensaje de éxito o error |
| **Requisitos técnicos** | - Endpoint REST: POST `/asistencias/registrar-qr`<br>- Request Body: `AsistenciaRegistroDTO { eventoEspecificoId, personaId, observacion, latitud, longitud }`<br>- Service: `AsistenciaServiceImp.registrarAsistencia()`<br>- Validaciones en orden:<br>  1. La sesión (EventoEspecifico) debe existir<br>  2. La fecha actual debe ser igual a la fecha de la sesión<br>  3. La hora actual debe estar dentro del rango permitido (30 min antes de inicio, 2 horas después del fin)<br>  4. La persona debe estar inscrita en un grupo del evento general<br>  5. No debe existir registro previo de asistencia para esta sesión-persona (unique constraint)<br>- Determinación de estado: `PRESENTE` si llega antes de hora inicio + tolerancia, `TARDE` si excede tolerancia<br>- Persistencia en tabla `upeu_asistencia`<br>- Response: `AsistenciaDTO` con información del registro |
| **Condiciones adicionales** | - Solo se permite registro el día de la sesión (fecha exacta)<br>- Rango temporal: desde 30 minutos antes de hora inicio hasta 2 horas después de hora fin<br>- Unique constraint en BD: `(evento_especifico_id, persona_id)` previene duplicados<br>- La persona debe pertenecer al evento mediante inscripción en grupo<br>- Si falla alguna validación, se retorna error 400 con mensaje descriptivo |
| **Diseño de arquitectura** | **Controller:** `AsistenciaController.registrarAsistenciaPorQR()` → **Service:** `AsistenciaServiceImp.registrarAsistencia()` → **Repositories:** `IAsistenciaRepository`, `IEventoEspecificoRepository`, `IGrupoParticipanteRepository`<br>**Validaciones:** Cálculos de tiempo con `LocalDateTime`, `LocalTime`, consultas de pertenencia<br>**Tablas:** `upeu_asistencia` (id_asistencia, evento_especifico_id, persona_id, fecha_hora_registro, estado, observacion, latitud, longitud, created_at), `upeu_evento_especifico`, `upeu_grupo_participante` |

---

### Requerimiento: RF-ASIS-002 – Registro Manual de Asistencia por Líder

**Descripción:** El sistema debe permitir a los líderes registrar asistencias manualmente para los participantes de sus grupos, marcando estado (PRESENTE, TARDE, AUSENTE, JUSTIFICADO) según corresponda.

**Historia de usuario:**
Como líder, quiero marcar manualmente la asistencia de los participantes de mi grupo cuando no pueden escanear el QR o en casos especiales.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Líder |
| **Acciones del Líder** | - Seleccionar sesión del día<br>- Solicitar lista de participantes de sus grupos<br>- Marcar estado de asistencia para cada participante (PRESENTE, TARDE, AUSENTE, JUSTIFICADO)<br>- Agregar observaciones opcionales<br>- Confirmar registro |
| **Requisitos técnicos** | - Endpoint REST: POST `/asistencias/marcar-manual`<br>- Request Body: `MarcarAsistenciaRequest { eventoEspecificoId, personaId, liderId, estado: String, observacion }`<br>- Service: `AsistenciaServiceImp.marcarAsistenciaPorLider()`<br>- Validaciones:<br>  1. El líder debe ser líder de un grupo del evento<br>  2. La persona debe ser participante de un grupo liderado por ese líder<br>  3. La sesión debe existir<br>  4. No debe existir registro previo (o permitir actualización, según regla)<br>- Persistencia en tabla `upeu_asistencia`<br>- Response: `AsistenciaDTO` con registro actualizado |
| **Condiciones adicionales** | - El líder solo puede marcar asistencias de participantes de sus propios grupos<br> - El estado debe ser uno de los valores válidos: PRESENTE, TARDE, AUSENTE, JUSTIFICADO<br> - Se puede actualizar un registro existente si el líder tiene permisos<br> - La fecha/hora de registro se establece al momento de la marcación manual |
| **Diseño de arquitectura** | **Controller:** `AsistenciaController.marcarAsistenciaManual()` → **Service:** `AsistenciaServiceImp.marcarAsistenciaPorLider()` → **Repositories:** `IAsistenciaRepository`, `IGrupoPequenoRepository`, `IGrupoParticipanteRepository`<br>**Validación:** Verificar relación líder-grupo-participante mediante JOINs<br>**Tablas:** `upeu_asistencia`, `upeu_grupo_pequeno`, `upeu_grupo_participante` |

---

### Requerimiento: RF-ASIS-003 – Consulta de Lista de Participantes para Llamado

**Descripción:** El sistema debe proporcionar a los líderes una lista de todos los participantes de sus grupos para una sesión específica, mostrando el estado de asistencia de cada uno (si ya registró o no).

**Historia de usuario:**
Como líder, quiero ver la lista de todos los participantes de mis grupos para una sesión con su estado de asistencia para poder hacer llamado y marcar manualmente si es necesario.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Líder |
| **Acciones del Líder** | - Seleccionar sesión del día<br>- Solicitar lista de participantes<br>- Visualizar lista con: nombre, código, grupo, estado de asistencia<br>- Marcar asistencias faltantes si es necesario |
| **Requisitos técnicos** | - Endpoint REST: GET `/asistencias/lista-llamado/{eventoEspecificoId}/lider/{liderId}`<br>- Path Variables: `eventoEspecificoId` (Long), `liderId` (Long)<br>- Response: `List<ParticipanteAsistenciaDTO>` con personaId, nombreCompleto, codigoEstudiante, documento, grupoPequenoNombre, tieneAsistencia, estadoAsistencia, horaRegistro, observacion<br>- Lógica de servicio:<br>  1. Consultar grupos pequeños liderados por el líderId<br>  2. Consultar participantes de esos grupos<br>  3. Para cada participante, verificar si tiene asistencia registrada en la sesión<br>  4. Construir DTO con información combinada<br>- JOINs complejos: GrupoPequeno → GrupoParticipante → Persona → Asistencia (LEFT JOIN) |
| **Condiciones adicionales** | - Solo se muestran participantes de grupos liderados por el líder especificado<br> - Se muestra estado de asistencia actual (PRESENTE, TARDE, AUSENTE, PENDIENTE)<br> - Si no hay registro de asistencia, `tieneAsistencia = false` y `estadoAsistencia = PENDIENTE`<br> - La lista está ordenada por grupo y luego por nombre |
| **Diseño de arquitectura** | **Controller:** `AsistenciaController.obtenerListaLlamado()` → **Service:** `AsistenciaServiceImp.obtenerListaParaLlamado()` → **Repositories:** `IGrupoPequenoRepository`, `IGrupoParticipanteRepository`, `IAsistenciaRepository`<br>**Lógica:** Consultas JOIN entre múltiples tablas, construcción de DTOs combinados<br>**Tablas:** `upeu_grupo_pequeno`, `upeu_grupo_participante`, `upeu_persona`, `upeu_asistencia` |

---

### Requerimiento: RF-ASIS-004 – Consulta de Historial de Asistencias por Persona

**Descripción:** El sistema debe permitir a los usuarios consultar su propio historial de asistencias o a los líderes consultar el historial de sus participantes, mostrando todas las sesiones asistidas con estado y fecha.

**Historia de usuario:**
Como integrante o líder, quiero consultar el historial de asistencias para ver todas las sesiones en las que he asistido o mis participantes han asistido.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Integrante, Líder, Administrador |
| **Acciones del Usuario** | - Acceder a página "Mis Asistencias" (integrante) o "Ver Asistencias" (líder/admin)<br>- Seleccionar persona o período (si aplica)<br>- Visualizar lista de asistencias con detalles |
| **Requisitos técnicos** | - Endpoint REST: GET `/asistencias/persona/{personaId}`<br>- Path Variable: `personaId` (Long)<br>- Response: `List<AsistenciaDTO>` con eventoEspecificoId, eventoNombre, fechaHoraRegistro, estado, observacion<br>- Consulta JPA: `IAsistenciaRepository.findByPersonaIdPersona()`<br>- Mapeo Entity → DTO mediante `AsistenciaMapper`<br>- Inclusión de información del evento y sesión en DTO<br>- Frontend: `MisAsistenciasPage.jsx` para integrantes, `VerAsistenciasPage.jsx` para líderes/admins |
| **Condiciones adicionales** | - Los integrantes solo pueden ver sus propias asistencias (personaId del token)<br> - Los líderes pueden ver asistencias de participantes de sus grupos<br> - Los administradores pueden ver todas las asistencias<br> - Se ordena por fecha de registro descendente (más reciente primero) |
| **Diseño de arquitectura** | **Controller:** `AsistenciaController.findByPersona()` → **Service:** `IAsistenciaService.findByPersona()` → **Repository:** `IAsistenciaRepository.findByPersonaIdPersona()`<br>**Tablas:** `upeu_asistencia` (consulta por persona_id con JOIN a evento_especifico para obtener nombre de sesión), `upeu_evento_especifico`, `upeu_persona` |

---

## MÓDULO: REPORTES Y ESTADÍSTICAS

### Requerimiento: RF-REP-001 – Generación de Reporte de Asistencia por Evento

**Descripción:** El sistema debe generar un reporte agregado de asistencias para un evento general, mostrando estadísticas por participante (total de sesiones, presentes, tardes, ausentes, porcentaje de asistencia).

**Historia de usuario:**
Como administrador o líder, quiero generar un reporte de asistencia para un evento completo para ver las estadísticas de participación de todos los estudiantes.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador, Líder |
| **Acciones del Usuario** | - Seleccionar evento general<br>- Solicitar generación de reporte<br>- Visualizar tabla con estadísticas por participante<br>- Opcionalmente exportar a Excel |
| **Requisitos técnicos** | - Endpoint REST: GET `/asistencias/reporte/{eventoGeneralId}`<br>- Path Variable: `eventoGeneralId` (Long)<br>- Response: `List<ReporteAsistenciaDTO>` con personaId, nombreCompleto, codigoEstudiante, totalSesiones, asistenciasPresente, asistenciasTarde, asistenciasAusente, asistenciasJustificado, porcentajeAsistencia<br>- Lógica de servicio: `AsistenciaServiceImp.generarReporteAsistencia()`<br>  1. Consultar todas las sesiones del evento general<br>  2. Consultar todos los participantes del evento (mediante grupos)<br>  3. Para cada participante, contar asistencias por estado<br>  4. Calcular porcentaje: (presentes + tardes) / total sesiones * 100<br>- Consultas SQL complejas con COUNT y GROUP BY por estado |
| **Condiciones adicionales** | - El evento general debe existir<br> - Solo se consideran participantes inscritos en grupos del evento<br> - El porcentaje se calcula sobre el total de sesiones del evento<br> - Las sesiones canceladas no se cuentan en el total<br> - Se requiere autenticación JWT válida |
| **Diseño de arquitectura** | **Controller:** `AsistenciaController.generarReporte()` → **Service:** `AsistenciaServiceImp.generarReporteAsistencia()` → **Repositories:** `IEventoEspecificoRepository`, `IGrupoParticipanteRepository`, `IAsistenciaRepository`<br>**Lógica:** Agregaciones con SQL COUNT, GROUP BY, cálculos de porcentaje<br>**Tablas:** `upeu_evento_general`, `upeu_evento_especifico`, `upeu_grupo_participante`, `upeu_asistencia`, `upeu_persona` |

---

## MÓDULO: GESTIÓN DE MATRÍCULAS

### Requerimiento: RF-MAT-001 – Importación Masiva de Matrículas desde Excel

**Descripción:** El sistema debe permitir importar matrículas de estudiantes e invitados desde un archivo Excel (.xlsx, .xls), procesando múltiples registros en una operación, aplicando filtros opcionales, y validando datos antes de persistir.

**Historia de usuario:**
Como administrador, quiero importar matrículas masivamente desde un archivo Excel para cargar rápidamente los datos de estudiantes sin ingresarlos uno por uno.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Descargar plantilla Excel (opcional)<br>- Completar plantilla con datos de matrículas<br>- Subir archivo Excel en página de importación<br>- Seleccionar filtros (sede, facultad, programa, periodo, tipo persona)<br>- Confirmar importación<br>- Visualizar resultados (exitosos, fallidos, errores) |
| **Requisitos técnicos** | - Endpoint REST: POST `/matriculas/importar`<br>- Content-Type: `multipart/form-data`<br>- Request Parameters: `file` (MultipartFile), `sedeId` (Long, opcional), `facultadId` (Long, opcional), `programaId` (Long, opcional), `periodoId` (Long, obligatorio), `tipoPersona` (Enum, opcional)<br>- Service: `IMatriculaService.importarDesdeExcel()`<br>- Procesamiento: Apache POI para leer Excel (.xlsx, .xls)<br>- Validaciones:<br>  - Formato de archivo (.xlsx o .xls)<br>  - Estructura de columnas esperada (según `MatriculaExcelDTO`)<br>  - Datos requeridos no nulos<br>  - Existencia de Sede, Facultad, Programa, Periodo<br>  - Unicidad de código de estudiante y documento<br>- Resultado: `ImportResultDTO { totalRegistros, exitosos, fallidos, errores: List<String>, warnings: List<String> }`<br>- Transacción: rollback si hay errores críticos, o procesamiento parcial con reporte de errores |
| **Condiciones adicionales** | - El periodoId es obligatorio para la importación<br> - Los filtros aplican restricciones sobre qué registros se procesan<br> - Si un registro falla, se continúa con los demás y se reporta en errores<br> - Se crean entidades Persona si no existen (buscando por documento o código)<br> - Se actualizan matrículas existentes o se crean nuevas según lógica de negocio |
| **Diseño de arquitectura** | **Controller:** `MatriculaController.importarExcel()` → **Service:** `IMatriculaService.importarDesdeExcel()` → **Librería:** Apache POI (`XSSFWorkbook`, `HSSFWorkbook`)<br>**Repositories:** `IMatriculaRepository`, `IPersonaRepository`, `ISedeRepository`, `IFacultadRepository`, `IProgramaEstudioRepository`, `IPeriodoRepository`<br>**DTOs:** `MatriculaExcelDTO` (estructura del Excel), `ImportResultDTO` (resultado), `ImportFilterDTO` (filtros)<br>**Tablas:** `upeu_matricula`, `upeu_persona` (creación/actualización), `upeu_sede`, `upeu_facultad`, `upeu_programa_estudio`, `upeu_periodo` |

---

### Requerimiento: RF-MAT-002 – Consulta de Matrículas con Filtros

**Descripción:** El sistema debe permitir consultar matrículas aplicando múltiples filtros opcionales (sede, facultad, programa, periodo, tipo de persona) para facilitar la búsqueda y gestión.

**Historia de usuario:**
Como administrador, quiero consultar matrículas filtrando por sede, facultad, programa y periodo para encontrar rápidamente los estudiantes que necesito gestionar.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Seleccionar filtros en página de matrículas<br>- Seleccionar sede, facultad, programa, periodo, tipo persona<br>- Aplicar filtros<br>- Visualizar lista filtrada de matrículas |
| **Requisitos técnicos** | - Endpoint REST: GET `/matriculas/filtrar?sedeId={id}&facultadId={id}&programaId={id}&periodoId={id}&tipoPersona={tipo}`<br>- Query Parameters: todos opcionales (nullable)<br>- Response: `List<MatriculaDTO>` con información completa de matrícula y persona<br>- Consulta JPQL: `IMatriculaRepository.findByFiltros()` usa `COALESCE` para manejar filtros nulos<br>  - Query: `WHERE sede.id = COALESCE(:sedeId, sede.id) AND facultad.id = COALESCE(:facultadId, facultad.id) ...`<br>- Mapeo Entity → DTO mediante `MatriculaMapper` con JOINs a Sede, Facultad, Programa, Periodo, Persona |
| **Condiciones adicionales** | - Si un filtro es null, se ignora (muestra todos los valores)<br> - Los filtros se combinan con AND (deben cumplirse todos los especificados)<br> - Se requiere autenticación JWT válida |
| **Diseño de arquitectura** | **Controller:** `MatriculaController.findByFiltros()` → **Service:** `IMatriculaService.findByFiltros()` → **Repository:** `IMatriculaRepository.findByFiltros()` (consulta JPQL con COALESCE)<br>**Tablas:** `upeu_matricula` (con JOINs a upeu_sede, upeu_facultad, upeu_programa_estudio, upeu_periodo, upeu_persona) |

---

### Requerimiento: RF-MAT-003 – Exportación de Matrículas a Excel

**Descripción:** El sistema debe permitir exportar las matrículas (filtradas o no) a un archivo Excel para uso externo o respaldo.

**Historia de usuario:**
Como administrador, quiero exportar las matrículas a Excel para tener un respaldo o compartir los datos con otros sistemas.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Aplicar filtros (opcional)<br>- Solicitar exportación<br>- Descargar archivo Excel generado |
| **Requisitos técnicos** | - Endpoint REST: GET `/matriculas/exportar?sedeId={id}&...` (mismos parámetros que filtrar)<br>- Response: `byte[]` (archivo Excel) con headers:<br>  - `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`<br>  - `Content-Disposition: attachment; filename=Matriculas_YYYYMMDD_HHmmss.xlsx`<br>- Service: `IMatriculaService.exportarMatriculasAExcel()`<br>- Generación: Apache POI crea archivo .xlsx con estructura de columnas<br>- Consulta de datos: misma lógica que `findByFiltros()` |
| **Condiciones adicionales** | - El archivo se genera en formato .xlsx (Excel 2007+)<br> - El nombre del archivo incluye timestamp para evitar sobrescritura<br> - Se requiere autenticación JWT válida |
| **Diseño de arquitectura** | **Controller:** `MatriculaController.exportarExcel()` → **Service:** `IMatriculaService.exportarMatriculasAExcel()` → **Librería:** Apache POI (`XSSFWorkbook`, `XSSFSheet`, `XSSFRow`)<br>**Repositories:** `IMatriculaRepository.findByFiltros()` (misma consulta que RF-MAT-002)<br>**Tablas:** `upeu_matricula` y tablas relacionadas (mismo que RF-MAT-002) |

---

## MÓDULO: ADMINISTRACIÓN DE CATÁLOGOS

### Requerimiento: RF-ADM-001 – Gestión CRUD de Sedes

**Descripción:** El sistema debe permitir a los administradores gestionar (crear, leer, actualizar, eliminar) las sedes de la universidad.

**Historia de usuario:**
Como administrador, quiero gestionar las sedes de la universidad para poder asignarlas a las matrículas.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Crear nueva sede<br>- Consultar lista de sedes<br>- Actualizar información de sede<br>- Eliminar sede (si no tiene matrículas asociadas) |
| **Requisitos técnicos** | - Endpoints REST: GET `/sedes`, GET `/sedes/{id}`, POST `/sedes`, PUT `/sedes/{id}`, DELETE `/sedes/{id}`<br>- DTO: `SedeDTO { idSede, nombre, descripcion }`<br>- Validación: nombre único (`@Unique` constraint en BD)<br>- Mapeo mediante `SedeMapper`<br>- Persistencia en tabla `upeu_sede` |
| **Condiciones adicionales** | - El nombre de la sede debe ser único<br> - No se puede eliminar una sede si tiene matrículas asociadas<br> - Se requiere autenticación JWT válida y rol ADMIN o SUPERADMIN |
| **Diseño de arquitectura** | **Controller:** `SedeController` → **Service:** `ISedeService` → **Repository:** `ISedeRepository`<br>**Tablas:** `upeu_sede` (id_sede, nombre UNIQUE, descripcion) |

---

### Requerimiento: RF-ADM-002 – Gestión CRUD de Facultades

**Descripción:** El sistema debe permitir gestionar las facultades de la universidad.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Crear, consultar, actualizar, eliminar facultades |
| **Requisitos técnicos** | - Endpoints: GET `/facultades`, GET `/facultades/{id}`, POST `/facultades`, PUT `/facultades/{id}`, DELETE `/facultades/{id}`<br>- DTO: `FacultadDTO { idFacultad, nombre, descripcion }`<br>- Validación: nombre único<br>- Tabla: `upeu_facultad` |
| **Condiciones adicionales** | - Nombre único<br> - No eliminar si tiene programas asociados |
| **Diseño de arquitectura** | **Controller:** `FacultadController` → **Service:** `IFacultadService` → **Repository:** `IFacultadRepository`<br>**Tablas:** `upeu_facultad` (id_facultad, nombre UNIQUE, descripcion) |

---

### Requerimiento: RF-ADM-003 – Gestión CRUD de Programas de Estudio

**Descripción:** El sistema debe permitir gestionar los programas de estudio asociados a facultades.

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Crear programa asociado a una facultad<br>- Consultar, actualizar, eliminar programas |
| **Requisitos técnicos** | - Endpoints: GET `/programas`, GET `/programas/{id}`, POST `/programas`, PUT `/programas/{id}`, DELETE `/programas/{id}`<br>- DTO: `ProgramaEstudioDTO { idPrograma, nombre, facultadId, facultadNombre, descripcion }`<br>- Validación: nombre único, facultadId obligatorio<br>- Tabla: `upeu_programa_estudio` con FK a `upeu_facultad` |
| **Condiciones adicionales** | - Nombre único<br> - La facultad debe existir<br> - No eliminar si tiene eventos o matrículas asociadas |
| **Diseño de arquitectura** | **Controller:** `ProgramaEstudioController` → **Service:** `IProgramaEstudioService` → **Repository:** `IProgramaEstudioRepository`<br>**Tablas:** `upeu_programa_estudio` (id_programa, nombre UNIQUE, facultad_id FK, descripcion) |

---

### Requerimiento: RF-ADM-004 – Gestión CRUD de Periodos Académicos

**Descripción:** El sistema debe permitir gestionar los periodos académicos (ej: "2025-I", "2025-II") con fechas de inicio y fin, y estado (ACTIVO, INACTIVO, FINALIZADO).

**Especificaciones Técnicas del Requerimiento:**

| Aspecto | Descripción |
|---------|-------------|
| **Actores** | Administrador |
| **Acciones del Administrador** | - Crear periodo con nombre, descripción, fechas, estado<br>- Consultar periodos<br>- Consultar periodo activo<br>- Actualizar, eliminar periodos |
| **Requisitos técnicos** | - Endpoints: GET `/periodos`, GET `/periodos/{id}`, GET `/periodos/activo`, GET `/periodos/estado/{estado}`, POST `/periodos`, PUT `/periodos/{id}`, DELETE `/periodos/{id}`<br>- DTO: `PeriodoDTO { idPeriodo, nombre, descripcion, fechaInicio, fechaFin, estado }`<br>- Validación: nombre único<br>- Tabla: `upeu_periodo`<br>- Lógica: solo un periodo puede estar "ACTIVO" a la vez (regla de negocio) |
| **Condiciones adicionales** | - Nombre único<br> - Fecha inicio < fecha fin<br> - Estado: ACTIVO, INACTIVO, FINALIZADO<br> - Solo un periodo ACTIVO (validación en servicio) |
| **Diseño de arquitectura** | **Controller:** `PeriodoController` → **Service:** `IPeriodoService` → **Repository:** `IPeriodoRepository`<br>**Tablas:** `upeu_periodo` (id_periodo, nombre UNIQUE, descripcion, fecha_inicio, fecha_fin, estado) |

---

## RESUMEN DE REQUERIMIENTOS FUNCIONALES

**Total de Requerimientos:** 24

**Por Módulo:**
- Autenticación y Sesiones: 3 (RF-AUT-001 a RF-AUT-003)
- Gestión de Usuarios y Roles: 3 (RF-USR-001 a RF-USR-003)
- Gestión de Eventos: 4 (RF-EVT-001 a RF-EVT-004)
- Gestión de Grupos: 4 (RF-GRP-001 a RF-GRP-004)
- Generación y Validación QR: 2 (RF-QR-001 a RF-QR-002)
- Registro de Asistencias: 4 (RF-ASIS-001 a RF-ASIS-004)
- Reportes y Estadísticas: 1 (RF-REP-001)
- Gestión de Matrículas: 3 (RF-MAT-001 a RF-MAT-003)
- Administración de Catálogos: 4 (RF-ADM-001 a RF-ADM-004)

**Priorización:** Los requerimientos están ordenados por prioridad funcional dentro de cada módulo, priorizando los flujos críticos del sistema (autenticación, generación QR, registro de asistencias) antes de las funcionalidades administrativas.

