# Spring Boot Error Handler Starter

## Descripción

Spring Boot Error Handler Starter es una biblioteca diseñada para estandarizar y simplificar el manejo de excepciones en aplicaciones Spring Boot. Proporciona un conjunto de excepciones predefinidas, modelos de respuesta de error uniformes y un manejador global de excepciones que ayuda a mantener consistencia en todas las respuestas de error de tu API REST.

## Características

- **Manejo centralizado de excepciones**: Captura automáticamente las excepciones y las convierte en respuestas HTTP coherentes
- **Excepciones tipificadas**: Conjunto completo de excepciones predefinidas para casos de uso comunes
- **Respuestas estandarizadas**: Formato uniforme para todas las respuestas de error
- **Integración con validación**: Manejo automático de errores de validación de Spring
- **Logging automático**: Registro detallado de todas las excepciones

## Instalación

Agrega la siguiente dependencia a tu archivo `pom.xml`:

```xml
<dependency>
    <groupId>com.driagon.services</groupId>
    <artifactId>spring-boot-error-handler-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Añade las siguientes propiedades a tu archivo `application.properties` o `application.yml` para habilitar el manejo de errores 404 personalizado:

```properties
# Configuración para activar NoHandlerFoundException (404)
spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=false
```

## Uso Básico

Una vez agregada la dependencia, el manejador de excepciones se registrará automáticamente en tu aplicación Spring Boot. No se requiere configuración adicional.

### Estructura de la respuesta de error

Todas las excepciones gestionadas producirán una respuesta con la siguiente estructura:

```json
{
  "timestamp": "2025-05-28 15:30:45",
  "status": "NOT_FOUND",
  "code": 404,
  "message": "El recurso solicitado no fue encontrado",
  "path": "/api/recursos/123",
  "details": []
}
```

En caso de errores de validación, el campo `details` contendrá información adicional:

```json
{
  "timestamp": "2025-05-28 15:30:45",
  "status": "BAD_REQUEST",
  "code": 400,
  "message": "Error de validación",
  "path": "/api/usuarios",
  "details": [
    {
      "field": "email",
      "message": "debe ser una dirección de correo electrónico válida",
      "code": "INVALID_FIELD"
    },
    {
      "field": "nombre",
      "message": "no debe estar vacío",
      "code": "INVALID_FIELD"
    }
  ]
}
```

## Tipos de Excepciones y Cuándo Usarlas

### NotFoundException (404 Not Found)

Usa esta excepción cuando un recurso solicitado no existe en el sistema.

```java
@GetMapping("/usuarios/{id}")
public Usuario obtenerUsuario(@PathVariable Long id) {
    return usuarioServicio.buscarPorId(id)
        .orElseThrow(() -> new NotFoundException("Usuario con ID " + id + " no encontrado"));
}
```

### InvalidArgumentsException (400 Bad Request)

Usa esta excepción cuando los parámetros proporcionados por el cliente son inválidos o incompletos.

```java
@PostMapping("/pagos")
public Recibo procesarPago(@RequestBody Pago pago) {
    if (pago.getMonto() <= 0) {
        throw new InvalidArgumentsException("El monto del pago debe ser mayor que cero");
    }
    return servicioFacturacion.procesarPago(pago);
}
```

### UnauthorizedException (401 Unauthorized)

Usa esta excepción cuando un usuario intenta acceder a un recurso sin autenticarse correctamente.

```java
@GetMapping("/perfil")
public Usuario obtenerPerfil(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
        throw new UnauthorizedException("Debe iniciar sesión para acceder a esta información");
    }
    return usuarioServicio.obtenerPerfilActual(auth);
}
```

### ForbiddenException (403 Forbidden)

Usa esta excepción cuando un usuario autenticado no tiene permisos suficientes para acceder a un recurso.

```java
@DeleteMapping("/usuarios/{id}")
public void eliminarUsuario(@PathVariable Long id, Authentication auth) {
    if (!tieneRolAdmin(auth)) {
        throw new ForbiddenException("No tiene permisos para eliminar usuarios");
    }
    usuarioServicio.eliminar(id);
}
```

### BusinessException (409 Conflict)

Usa esta excepción cuando ocurre un problema relacionado con la lógica de negocio.

```java
@PostMapping("/inventario/transferencia")
public void transferirProducto(@RequestBody TransferenciaDto dto) {
    Producto producto = inventarioServicio.buscarProducto(dto.getProductoId());
    if (producto.getStock() < dto.getCantidad()) {
        throw new BusinessException("Stock insuficiente para realizar la transferencia");
    }
    inventarioServicio.transferir(dto);
}
```

### ProcessException (500 Internal Server Error)

Usa esta excepción cuando ocurre un error interno en el procesamiento que no es responsabilidad del cliente.

```java
@PostMapping("/reportes/generar")
public Reporte generarReporte(@RequestBody SolicitudReporte solicitud) {
    try {
        return reporteServicio.generar(solicitud);
    } catch (Exception e) {
        throw new ProcessException("Error al generar el reporte: " + e.getMessage());
    }
}
```

### ServiceUnavailableException (503 Service Unavailable)

Usa esta excepción cuando un servicio externo del que depende tu aplicación no está disponible.

```java
@GetMapping("/clima")
public DatosClima obtenerClima(@RequestParam String ciudad) {
    try {
        return servicioClimaExterno.consultarClima(ciudad);
    } catch (Exception e) {
        throw new ServiceUnavailableException("El servicio de clima no está disponible en este momento");
    }
}
```

## Integración con Errores de Spring Boot

La librería respeta y formatea los errores estándar de Spring Boot, manteniendo los códigos de estado correctos mientras proporciona una estructura de respuesta consistente. Los siguientes errores comunes de Spring Boot están soportados:

| Código | Descripción | Causa |
|--------|-------------|-------|
| 400 | Bad Request | Formato de cuerpo incorrecto, parámetros inválidos, errores de validación |
| 404 | Not Found | Recurso o endpoint no encontrado |
| 405 | Method Not Allowed | Método HTTP no soportado para el endpoint |
| 415 | Unsupported Media Type | Tipo de contenido no soportado |
| 500 | Internal Server Error | Errores no controlados del servidor |

La biblioteca mantiene la consistencia del formato de respuesta para todos estos errores, facilitando el manejo por parte del cliente.

## Manejo de Errores de Validación

La librería maneja automáticamente los errores de validación generados por las anotaciones de Bean Validation.

```java
@PostMapping("/usuarios")
public Usuario crearUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
    // Si hay errores de validación, se manejará automáticamente
    return usuarioServicio.crear(usuarioDto);
}

// Clase DTO con validaciones
public class UsuarioDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Email(message = "Debe ser un email válido")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    // getters y setters
}
```

## Extendiendo la Librería

### Crear excepciones personalizadas

Puedes crear tus propias excepciones extendiendo `BaseException`:

```java
public class RecursoExpiradoException extends BaseException {
    private static final long serialVersionUID = 1L;

    public RecursoExpiradoException(String message) {
        super(HttpStatus.GONE, message); // 410 Gone
    }
}
```

### Personalizar el manejo de excepciones

Si necesitas un comportamiento personalizado para manejar excepciones específicas, puedes crear tu propio controlador que extienda o complemente el `GlobalExceptionHandler`:

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // Para que se ejecute antes que GlobalExceptionHandler
public class CustomExceptionHandler {

    @ExceptionHandler(MiExcepcionPersonalizada.class)
    public ResponseEntity<ErrorResponse> handleMiExcepcion(MiExcepcionPersonalizada ex, WebRequest request) {
        // Lógica personalizada
        ErrorResponse error = new ErrorResponse();
        // Configurar respuesta
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## Mejores Prácticas

1. **Mensajes claros**: Proporciona mensajes de error descriptivos y orientados al usuario
2. **No exponer detalles sensibles**: Evita incluir información sensible en los mensajes de error
3. **Usar excepciones específicas**: Utiliza la excepción más específica para cada caso de uso
4. **Respetar la semántica HTTP**: Utiliza los códigos de estado HTTP apropiados para cada situación
5. **Consistencia**: Mantén un enfoque coherente para el manejo de errores en toda tu aplicación
6. **Log adecuado**: No registres información sensible en los logs
7. **Excepciones no controladas**: Deja que la biblioteca maneje las excepciones no controladas como errores 500
8. **Errores de framework**: Permite que los errores nativos de Spring Boot mantengan sus códigos de estado apropiados

## Soporte y Contribuciones

Para reportar problemas, solicitar características o contribuir al desarrollo, contacta al equipo de desarrollo de Driagon Services.

## Licencia

Esta biblioteca es propiedad de Driagon. Todos los derechos reservados.