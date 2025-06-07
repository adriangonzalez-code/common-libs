# 📋 Logging Library - Documentación Completa

Una biblioteca robusta y flexible de logging para aplicaciones Java que proporciona capacidades de logging orientado a aspectos a través de anotaciones, con enmascaramiento inteligente de datos sensibles.

## 📋 Tabla de Contenidos

- [🚀 Características Principales](#-características-principales)
- [📦 Requisitos e Instalación](#-requisitos-e-instalación)
- [⚙️ Configuración](#️-configuración)
- [📝 Anotaciones Disponibles](#-anotaciones-disponibles)
- [🔧 Uso con Aspectos (@Loggable)](#-uso-con-aspectos-loggable)
- [🎭 Enmascaramiento de Datos Sensibles](#-enmascaramiento-de-datos-sensibles)
- [📖 Logging Manual](#-logging-manual)
- [🛠️ Métodos de Utilidad](#️-métodos-de-utilidad)
- [🌐 Filtro HTTP](#-filtro-http)
- [⚡ Consideraciones de Rendimiento](#-consideraciones-de-rendimiento)
- [🐛 Depuración y Troubleshooting](#-depuración-y-troubleshooting)
- [🔄 Limitaciones Conocidas](#-limitaciones-conocidas)
- [📚 Ejemplos Avanzados](#-ejemplos-avanzados)

## 🚀 Características Principales

- ✅ **Logging automático** basado en anotaciones para métodos
- ✅ **Enmascaramiento inteligente** de datos sensibles (PII, credenciales, tokens)
- ✅ **Logging de entrada y salida** de métodos con argumentos y resultados
- ✅ **Manejo personalizado de excepciones** con diferentes niveles de log
- ✅ **Medición automática de tiempo** de ejecución de métodos
- ✅ **Filtro HTTP** para logging automático de requests/responses
- ✅ **Logging manual** con enmascaramiento contextual
- ✅ **Configuración flexible** a través de properties
- ✅ **Cero configuración** - auto-configuración por defecto
- ✅ **Performance optimizada** con caché y verificaciones de nivel de log

## 📝 Anotaciones Disponibles

### @Loggable

**Descripción**: Anotación principal para habilitar logging automático en métodos.

**Ubicación**: Métodos

**Propiedades**:

| Propiedad | Tipo | Valor por defecto | Descripción |
|-----------|------|-------------------|-------------|
| `message` | `String` | `""` | Mensaje personalizado para incluir en logs |
| `level` | `Level` | `Level.INFO` | Nivel de log (DEBUG, INFO, WARN, ERROR) |
| `includeArgs` | `boolean` | `true` | Incluir argumentos del método en el log |
| `includeResult` | `boolean` | `true` | Incluir valor de retorno en el log |
| `exceptions` | `ExceptionLog[]` | `{}` | Configuración específica para excepciones |
| `logUnexpectedExceptions` | `boolean` | `true` | Loggear excepciones no controladas |
| `exceptionLevel` | `Level` | `Level.ERROR` | Nivel para excepciones no controladas |

**Ejemplo básico**:
```java
@Loggable
public User findUserById(Long id) {
    return userRepository.findById(id);
}
```

**Ejemplo avanzado**:
```java
@Loggable(
    message = "Procesando pago para cliente",
    level = Level.DEBUG,
    includeArgs = true,
    includeResult = false,
    exceptions = {
        @ExceptionLog(value = PaymentException.class, message = "Error en el pago: {0}", printStackTrace = true),
        @ExceptionLog(value = ValidationException.class, message = "Datos inválidos")
    }
)
public PaymentResult processPayment(String customerId, BigDecimal amount) {
    // Lógica de procesamiento
}
```

### @Mask

**Descripción**: Enmascara datos sensibles en logs manteniendo parte del valor visible.

**Ubicación**: Campos de clase, parámetros de método

**Propiedades**:

| Propiedad | Tipo | Valor por defecto | Descripción |
|-----------|------|-------------------|-------------|
| `maskChar` | `char` | `'*'` | Carácter usado para enmascarar |
| `visibleChars` | `int` | `4` | Número de caracteres visibles |
| `position` | `Position` | `Position.SUFFIX` | Posición de caracteres visibles (PREFIX/SUFFIX) |

**Enum Position**:
- `PREFIX`: Caracteres visibles al inicio (`john****@mail.com`)
- `SUFFIX`: Caracteres visibles al final (`****@mail.com`)

**Ejemplos**:

```java
public class User {
    @Mask(visibleChars = 4, position = Mask.Position.PREFIX)
    private String email; // Resultado: john****@example.com

    @Mask(visibleChars = 4, position = Mask.Position.SUFFIX)
    private String phone; // Resultado: ****5678

    @Mask(maskChar = 'X', visibleChars = 2)
    private String creditCard; // Resultado: XXXXXXXXXXXXXX34
}

@Loggable
public void updateUser(@Mask(visibleChars = 0) String password, User user) {
    // password aparecerá completamente enmascarado: ********
}
```

### @Exclude

**Descripción**: Excluye completamente un campo o parámetro de los logs.

**Ubicación**: Campos de clase, parámetros de método

**Propiedades**: Ninguna

**Ejemplos**:

```java
public class User {
    private String name;

    @Exclude
    private String secretKey; // No aparecerá en logs

    @Exclude
    private String internalToken; // Completamente oculto
}

@Loggable
public void processData(String publicData, @Exclude String privateKey) {
    // privateKey aparecerá como [EXCLUDED]
}
```

### @ExceptionLog

**Descripción**: Configura el comportamiento de logging para excepciones específicas.

**Ubicación**: Dentro del array `exceptions` de `@Loggable`

**Propiedades**:

| Propiedad | Tipo | Valor por defecto | Descripción |
|-----------|------|-------------------|-------------|
| `value` | `Class<? extends Throwable>` | **Requerido** | Clase de excepción a manejar |
| `message` | `String` | `""` | Mensaje personalizado (puede usar placeholders {0}, {1}...) |
| `printStackTrace` | `boolean` | `false` | Incluir stack trace completo |

**Ejemplos**:

```java
@Loggable(exceptions = {
    @ExceptionLog(
        value = UserNotFoundException.class, 
        message = "Usuario con ID {0} no encontrado", 
        printStackTrace = false
    ),
    @ExceptionLog(
        value = DatabaseException.class, 
        message = "Error de base de datos en operación {1}", 
        printStackTrace = true
    ),
    @ExceptionLog(value = SecurityException.class) // Usa mensaje por defecto
})
public User authenticateUser(String username, String password) {
    // Si se lanza UserNotFoundException con username="john123":
    // LOG: [EXCEPCIÓN] authenticateUser - Usuario con ID john123 no encontrado
}
```

## 📦 Requisitos e Instalación

### Requisitos

- **Java 21** o superior
- **Spring Boot 3.5.0** o superior
- **Jakarta EE** (jakarta imports)

### Instalación

Añade la dependencia en tu archivo `pom.xml`:

```xml
<dependency>
    <groupId>com.driagon.services</groupId>
    <artifactId>spring-boot-logging-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Para proyectos Gradle:

```gradle
implementation 'com.driagon.services:spring-boot-logging-starter:1.0.0-SNAPSHOT'
```

## ⚙️ Configuración

La biblioteca se auto-configura por defecto. Puedes personalizar su comportamiento:

### application.properties

```properties
# === Configuración del Aspecto de Logging ===
logging.aspect.enabled=true
logging.aspect.pretty-print=true
logging.aspect.log-request-duration=true
logging.aspect.default-mask-char=*
logging.aspect.default-visible-chars=4

# === Configuración del Filtro HTTP ===
logging.filter.enabled=true
logging.filter.exclude-paths=/health,/info,/actuator/**,/swagger-ui/**
logging.filter.request-headers=Authorization,Content-Type,Accept,X-Correlation-ID
logging.filter.response-headers=Content-Type,Content-Length,X-Response-Time
logging.filter.max-payload-length=1000

# === Configuración de Niveles ===
logging.level.com.driagon.services.logging=INFO
logging.level.com.tu.aplicacion=DEBUG
```

### application.yml

```yaml
logging:
  aspect:
    enabled: true
    pretty-print: true
    log-request-duration: true
    default-mask-char: "*"
    default-visible-chars: 4
  filter:
    enabled: true
    exclude-paths:
      - "/health"
      - "/info"
      - "/actuator/**"
      - "/swagger-ui/**"
    request-headers:
      - "Authorization"
      - "Content-Type"
      - "Accept"
    response-headers:
      - "Content-Type"
      - "Content-Length"
    max-payload-length: 1000
  level:
    com.driagon.services.logging: INFO
    com.tu.aplicacion: DEBUG
```

## 🔧 Uso con Aspectos (@Loggable)

### Logging Básico

```java
@Service
public class UserService {

    @Loggable
    public User createUser(CreateUserRequest request) {
        // Lógica de creación
        return userRepository.save(user);
    }
}
```

**Salida del log**:
```
INFO  c.e.UserService - [ENTRADA] createUser - Args: [CreateUserRequest{name=Juan, email=juan****@mail.com}]
INFO  c.e.UserService - [SALIDA] createUser - Resultado: User{id=123, name=Juan, email=juan****@mail.com} - Tiempo: 45ms
```

### Configuración Avanzada de Nivel

```java
@Loggable(level = Level.DEBUG)
public List<User> findAllUsers() {
    return userRepository.findAll();
}

@Loggable(level = Level.WARN, message = "Operación crítica de eliminación")
public void deleteUser(Long userId) {
    userRepository.deleteById(userId);
}
```

### Control de Argumentos y Resultados

```java
// Solo logear entrada, no salida
@Loggable(includeResult = false)
public void sendEmail(String to, String subject, String body) {
    emailService.send(to, subject, body);
}

// Solo logear salida, no entrada  
@Loggable(includeArgs = false)
public String generateReport() {
    return reportService.generateMonthlyReport();
}

// Sin argumentos ni resultados, solo timing
@Loggable(includeArgs = false, includeResult = false, message = "Limpieza de cache")
public void clearCache() {
    cacheManager.clearAll();
}
```

### Manejo Avanzado de Excepciones

```java
@Loggable(
    exceptions = {
        @ExceptionLog(
            value = ValidationException.class,
            message = "Datos inválidos para usuario {0}: {1}",
            printStackTrace = false
        ),
        @ExceptionLog(
            value = DuplicateEmailException.class,
            message = "Email {1} ya existe en el sistema",
            printStackTrace = false
        ),
        @ExceptionLog(
            value = DatabaseException.class,
            message = "Error de BD en creación de usuario",
            printStackTrace = true
        )
    },
    logUnexpectedExceptions = true,
    exceptionLevel = Level.ERROR
)
public User createUser(String username, String email) {
    // Los placeholders {0}, {1}... corresponden a los argumentos del método
    // {0} = username, {1} = email
    return userService.create(username, email);
}
```

## 🎭 Enmascaramiento de Datos Sensibles

### Enmascaramiento en Campos de Clase

```java
public class BankAccount {
    private String accountNumber;

    @Mask(visibleChars = 4, position = Mask.Position.SUFFIX)
    private String cardNumber; // ****1234

    @Mask(visibleChars = 3, position = Mask.Position.PREFIX, maskChar = 'X')
    private String sortCode; // 123XXX

    @Exclude
    private String pin; // [EXCLUDED]

    @Mask(visibleChars = 0) // Completamente enmascarado
    private String cvv; // ***
}
```

### Enmascaramiento en Parámetros de Método

```java
@Loggable
public TransactionResult transfer(
    @Mask(visibleChars = 4) String fromAccount,
    @Mask(visibleChars = 4) String toAccount, 
    BigDecimal amount,
    @Exclude String authToken
) {
    // fromAccount: ****5678
    // toAccount: ****9012  
    // amount: 1500.00 (sin enmascarar)
    // authToken: [EXCLUDED]
}
```

### Patrones de Enmascaramiento Comunes

```java
public class SecurityData {
    @Mask(visibleChars = 4, position = Mask.Position.PREFIX)
    private String email; // john****@company.com

    @Mask(visibleChars = 4, position = Mask.Position.SUFFIX) 
    private String phone; // ****5678

    @Mask(visibleChars = 0)
    private String password; // ********

    @Mask(visibleChars = 6, position = Mask.Position.PREFIX)
    private String address; // Calle ****

    @Exclude
    private String secretKey; // [EXCLUDED]

    @Mask(visibleChars = 8, position = Mask.Position.SUFFIX, maskChar = '#')
    private String socialSecurity; // ########12345678
}
```


## 🌐 Filtro HTTP

### Configuración del Filtro

El filtro HTTP se activa automáticamente y registra todas las peticiones/respuestas:

```properties
# Habilitar/deshabilitar filtro
logging.filter.enabled=true

# Rutas a excluir del logging
logging.filter.exclude-paths=/health,/info,/actuator/**,/swagger-ui/**,/v3/api-docs/**

# Headers de request a logear
logging.filter.request-headers=Authorization,Content-Type,Accept,X-Correlation-ID,User-Agent

# Headers de response a logear  
logging.filter.response-headers=Content-Type,Content-Length,X-Response-Time,Location

# Longitud máxima del payload a logear
logging.filter.max-payload-length=1000

# Nivel de log para requests/responses
logging.filter.level=INFO
```

### Salida del Filtro HTTP

**Request log**:
```
INFO c.d.s.l.f.RequestResponseLoggingFilter - [REQUEST] POST /api/users - Headers: {Content-Type=application/json, Authorization=Bearer ****ken} - Body: {"name":"Juan","email":"juan****@mail.com"}
```

**Response log**:
```
INFO c.d.s.l.f.RequestResponseLoggingFilter - [RESPONSE] POST /api/users - Status: 201 - Headers: {Content-Type=application/json, Content-Length=156} - Body: {"id":123,"name":"Juan","email":"juan****@mail.com"} - Duration: 245ms
```

### Exclusión de Rutas

```properties
# Múltiples patrones soportados
logging.filter.exclude-paths=/health,/info,/actuator/**,/swagger-ui/**,/webjars/**,/error

# También soporta patterns de Spring:
logging.filter.exclude-paths=/api/internal/**,/monitoring/*,/admin/health
```

### Personalización del Filtro

```java
@Component
public class CustomRequestResponseFilter extends RequestResponseLoggingFilter {

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        // Lógica personalizada para determinar si logear
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && !userAgent.contains("HealthCheck");
    }

    @Override
    protected String maskRequestBody(String body, String contentType) {
        // Enmascaramiento personalizado del body de request
        if ("application/json".equals(contentType)) {
            return MaskingUtils.maskSensitiveData(parseJson(body));
        }
        return super.maskRequestBody(body, contentType);
    }
}
```

## 📖 Logging Manual

### Usando MaskedLogger

El `MaskedLogger` proporciona logging manual con enmascaramiento automático basado en el contexto del método.

```java
import com.driagon.services.logging.utils.MaskedLogger;

@Service
public class PaymentService {

    // Crear instancia del logger
    private static final MaskedLogger log = MaskedLogger.getLogger(PaymentService.class);

    public void processPayment(@Mask String cardNumber, BigDecimal amount) {
        log.info("Iniciando procesamiento de pago por {} con tarjeta {}", amount, cardNumber);
        // Output: Iniciando procesamiento de pago por 100.50 con tarjeta ********

        try {
            // Lógica de procesamiento
            PaymentResult result = paymentGateway.charge(cardNumber, amount);

            log.info("Pago procesado exitosamente: {}", result);

        } catch (PaymentException e) {
            log.error("Error procesando pago con tarjeta {}: {}", cardNumber, e.getMessage());
            // cardNumber se enmascara automáticamente
        }
    }

    public void logTransactionDetails(Transaction transaction) {
        // Los campos anotados del objeto transaction se enmascaran automáticamente
        log.debug("Detalles de transacción: {}", transaction);

        // Para logging más específico
        log.info("Transacción {} procesada para cuenta {}", 
                 transaction.getId(), 
                 transaction.getAccountNumber()); // Se enmascara si está anotado
    }
}
```

### Métodos Disponibles en MaskedLogger

```java
// Todos los niveles de log estándar
log.trace("Mensaje trace con args: {}", arg);
log.debug("Mensaje debug con args: {}", arg);  
log.info("Mensaje info con args: {}", arg);
log.warn("Mensaje warning con args: {}", arg);
log.error("Mensaje error con args: {}", arg);

// Logging de excepciones
log.error("Error procesando: {}", data, exception);

// Verificación de niveles (para optimización)
if (log.isDebugEnabled()) {
    String expensiveString = buildComplexString();
    log.debug("Debug info: {}", expensiveString);
}
```

### Comportamiento del Enmascaramiento en MaskedLogger

El `MaskedLogger` analiza el contexto del método que lo llama para aplicar enmascaramiento inteligente:

1. **Parámetros anotados**: Si un argumento del log corresponde a un parámetro anotado (`@Mask` o `@Exclude`), aplica la anotación
2. **Objetos complejos**: Para objetos, enmascara campos anotados dentro del objeto
3. **Valores simples**: Para tipos primitivos o String, aplica enmascaramiento básico solo si hay anotaciones

```java
public void example(@Mask String sensitiveData, User user) {
    // sensitiveData se enmascara por la anotación del parámetro
    // user se procesa campo por campo según sus anotaciones
    log.info("Processing data {} for user {}", sensitiveData, user);
}
```

## 🛠️ Métodos de Utilidad

### MaskingUtils - Enmascaramiento Manual

Para casos donde necesitas enmascaramiento manual directo:

```java
import com.driagon.services.logging.utils.MaskingUtils;

public class Example {
    public void manualMaskingExamples() {

        // Enmascaramiento básico de objetos (respeta anotaciones)
        User user = new User("john@example.com", "secret123");
        String maskedUser = MaskingUtils.maskSensitiveData(user);

        // Enmascaramiento directo con configuración específica
        String email = "john.doe@company.com";
        String maskedEmail = MaskingUtils.maskField(email, createMaskAnnotation(4, '*', Mask.Position.PREFIX));
        // Resultado: john****@company.com

        // Procesamiento de arrays de argumentos (para aspectos personalizados)
        Object[] args = {"public", "secret123", user};
        Object[] maskedArgs = MaskingUtils.maskLoggingArguments(args);

        // Procesamiento con información de método (para aspectos avanzados)
        Method method = this.getClass().getMethod("someMethod", String.class, String.class);
        Object[] processedArgs = MaskingUtils.processArguments(method, args);
    }

    // Método helper para crear anotación @Mask programáticamente
    private Mask createMaskAnnotation(int visibleChars, char maskChar, Mask.Position position) {
        return new Mask() {
            public Class<? extends java.lang.annotation.Annotation> annotationType() { return Mask.class; }
            public int visibleChars() { return visibleChars; }
            public char maskChar() { return maskChar; }
            public Position position() { return position; }
        };
    }
}
```

### Métodos Específicos de MaskingUtils

```java
// Enmascaramiento de objetos complejos (respeta anotaciones en campos)
String result = MaskingUtils.maskSensitiveData(complexObject);

// Enmascaramiento directo con configuración de @Mask
String masked = MaskingUtils.maskField(value, maskAnnotation);

// Procesamiento de argumentos para logging
Object[] masked = MaskingUtils.maskLoggingArguments(arg1, arg2, arg3);

// Procesamiento con contexto de método (usado internamente por aspectos)
Object[] processed = MaskingUtils.processArguments(method, arguments);
```

## ⚡ Consideraciones de Rendimiento

### Optimizaciones Implementadas

1. **Verificación de nivel de log**: Los métodos verifican si el nivel está habilitado antes de procesar
2. **Caché de métodos**: Los métodos reflection se cachean para evitar búsquedas repetidas
3. **Procesamiento lazy**: Los argumentos solo se procesan si el nivel de log está activo
4. **Enmascaramiento eficiente**: Uso de StringBuilder y operaciones optimizadas

### Buenas Prácticas de Rendimiento

```java
// ✅ BUENO: Verificación de nivel antes de operaciones costosas
if (log.isDebugEnabled()) {
    String expensiveData = buildComplexString(); // Solo se ejecuta si DEBUG está activo
    log.debug("Debug data: {}", expensiveData);
}

// ✅ BUENO: Usar parámetros en lugar de concatenación
log.info("User {} has {} transactions", userId, transactionCount);

// ❌ MALO: Concatenación de strings costosa
log.info("User " + userId + " has " + buildExpensiveString() + " transactions");

// ✅ BUENO: Para objetos simples
@Loggable(level = Level.DEBUG)
public String getUsername(Long id) { return "user"; }

// ⚠️ CUIDADO: Para operaciones muy frecuentes o críticas en performance
@Loggable(level = Level.NONE) // Deshabilitar logging si es necesario
public int calculatePrimeNumbers(int range) { /* operación intensiva */ }
```

### Impacto en Performance

| Operación | Overhead | Recomendación |
|-----------|----------|---------------|
| Logging con @Loggable (método simple) | ~0.1-0.5ms | ✅ Usar libremente |
| Enmascaramiento de objeto simple | ~0.1-0.2ms | ✅ Usar libremente |
| Enmascaramiento de objeto complejo | ~1-5ms | ⚠️ Medir en casos críticos |
| Filtro HTTP | ~0.5-2ms por request | ✅ Aceptable para la mayoría |
| MaskedLogger con análisis de stack | ~0.2-1ms | ✅ Uso normal |

## 🐛 Depuración y Troubleshooting

### Activar Logging de Diagnóstico

```properties
# Logging detallado de la librería
logging.level.com.driagon.services.logging=DEBUG

# Logging específico por componente
logging.level.com.driagon.services.logging.aspects=DEBUG
logging.level.com.driagon.services.logging.filters=DEBUG
logging.level.com.driagon.services.logging.utils=DEBUG
```

### Problemas Comunes y Soluciones

#### 1. @Loggable no funciona

**Síntomas**: El método anotado no genera logs
**Causas posibles**:
- Spring AOP no está configurado
- El método es privado o final
- La clase no es un Spring Bean

**Solución**:
```java
// ❌ MALO: Método privado
@Loggable
private void privateMethod() { }

// ✅ BUENO: Método público en Spring Bean
@Component
public class MyService {
    @Loggable
    public void publicMethod() { }
}
```

#### 2. Enmascaramiento no se aplica

**Síntomas**: Datos sensibles aparecen sin enmascarar
**Causas posibles**:
- Anotación en campo privado sin getter
- Enmascaramiento manual de valores simples
- Configuración incorrecta de @Mask

**Solución**:
```java
// ❌ PROBLEMA: Valor directo no se enmascara automáticamente
log.info("Email: {}", user.getEmail()); // No se enmascara

// ✅ SOLUCIÓN 1: Usar objeto completo
log.info("User: {}", user); // Se enmascara según anotaciones del campo

// ✅ SOLUCIÓN 2: Usar parámetro anotado
public void method(@Mask String email) {
    log.info("Email: {}", email); // Se enmascara
}
```

#### 3. Performance degradada

**Síntomas**: Aplicación más lenta después de agregar la librería
**Diagnóstico**:
```properties
# Activar timing detallado
logging.level.com.driagon.services.logging.aspects.LoggingAspect=DEBUG
```

**Soluciones**:
```java
// Reducir nivel de logging en métodos frecuentes
@Loggable(level = Level.DEBUG) // Solo activo si DEBUG está habilitado

// Deshabilitar logging en métodos críticos
@Loggable(level = Level.NONE)

// Reducir información loggeada
@Loggable(includeArgs = false, includeResult = false)
```

### Configuración de Troubleshooting

```properties
# === Configuración para debugging ===
logging.level.com.driagon.services.logging=DEBUG

# Ver todos los métodos interceptados por aspectos
logging.level.org.springframework.aop=DEBUG

# Ver procesamiento de annotations
logging.level.org.springframework.context.annotation=DEBUG

# Logging detallado de HTTP filter
logging.filter.log-headers=true
logging.filter.log-body=true
```

## 🔄 Limitaciones Conocidas

### 1. Enmascaramiento de Valores Directos

**Limitación**: Los valores obtenidos mediante getters no se enmascaran automáticamente

```java
// ❌ NO SE ENMASCARA: valor directo de getter
log.info("Email: {}", request.getEmail()); 

// ✅ SE ENMASCARA: objeto completo
log.info("Request: {}", request);

// ✅ SE ENMASCARA: parámetro anotado
public void method(@Mask String email) {
    log.info("Email: {}", email);
}
```

### 2. Métodos Privados y Final

**Limitación**: Spring AOP no puede interceptar métodos privados, finales o estáticos

```java
public class MyService {
    @Loggable // ❌ NO FUNCIONA
    private void privateMethod() { }

    @Loggable // ❌ NO FUNCIONA
    public final void finalMethod() { }

    @Loggable // ❌ NO FUNCIONA
    public static void staticMethod() { }

    @Loggable // ✅ FUNCIONA
    public void publicMethod() { }
}
```

### 3. Self-Invocation

**Limitación**: Llamadas a métodos dentro de la misma clase no pasan por el proxy de Spring

```java
@Component
public class MyService {
    @Loggable
    public void methodA() {
        methodB(); // ❌ NO SE LOGGEA: self-invocation
    }

    @Loggable
    public void methodB() {
        // Lógica
    }
}
```

**Solución**: Inyectar self-reference o usar ApplicationContext

```java
@Component
public class MyService {
    @Autowired
    private MyService self; // Self-injection

    @Loggable
    public void methodA() {
        self.methodB(); // ✅ FUNCIONA: a través del proxy
    }

    @Loggable
    public void methodB() {
        // Lógica
    }
}
```

### 4. Objetos Circulares

**Limitación**: Objetos con referencias circulares pueden causar StackOverflowError

```java
public class Parent {
    private Child child;
    @Mask private String secret;
}

public class Child {
    private Parent parent; // Referencia circular
}
```

**Solución**: La librería detecta y maneja referencias circulares automáticamente, pero en casos complejos puede ser necesario usar `@Exclude` en algunas referencias.

## Extensión

Puedes proporcionar tus propias implementaciones de `LoggingService` o `RequestResponseLoggingFilter` registrándolas como beans en tu contexto de Spring, lo que anulará las implementaciones predeterminadas.

## 📚 Ejemplos Avanzados

### Sistema de E-commerce Completo

```java
// === Entidades ===
public class Order {
    private String id;
    private BigDecimal amount;

    @Mask(visibleChars = 4, position = Mask.Position.SUFFIX)
    private String cardNumber;

    @Exclude
    private String cvv;

    private Customer customer;
}

public class Customer {
    private String name;

    @Mask(visibleChars = 4, position = Mask.Position.PREFIX)
    private String email;

    @Mask(visibleChars = 4, position = Mask.Position.SUFFIX)
    private String phone;
}

// === Servicios ===
@Service
public class OrderService {

    private static final MaskedLogger log = MaskedLogger.getLogger(OrderService.class);

    @Loggable(
        message = "Procesando orden de compra",
        level = Level.INFO,
        exceptions = {
            @ExceptionLog(
                value = InsufficientFundsException.class,
                message = "Fondos insuficientes para orden {0} por {1}",
                printStackTrace = false
            ),
            @ExceptionLog(
                value = PaymentGatewayException.class,
                message = "Error en gateway de pago",
                printStackTrace = true
            )
        }
    )
    public OrderResult processOrder(
        @Mask(visibleChars = 6) String orderId,
        Order order,
        @Exclude String apiKey
    ) {
        log.info("Iniciando validación de orden {}", orderId);

        // Validaciones
        validateOrder(order);

        log.debug("Orden validada, procesando pago para customer {}", order.getCustomer());

        // Procesamiento de pago
        PaymentResult payment = paymentService.charge(order);

        log.info("Pago procesado exitosamente para orden {}: {}", orderId, payment);

        return new OrderResult(orderId, payment.getTransactionId());
    }

    @Loggable(level = Level.DEBUG, includeResult = false)
    private void validateOrder(Order order) {
        if (order.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Monto inválido");
        }
    }
}

@Service
public class PaymentService {

    private static final MaskedLogger log = MaskedLogger.getLogger(PaymentService.class);

    @Loggable(
        message = "Procesando pago",
        includeResult = false, // No logear resultado por seguridad
        exceptions = {
            @ExceptionLog(value = InsufficientFundsException.class, printStackTrace = false),
            @ExceptionLog(value = PaymentGatewayException.class, printStackTrace = true)
        }
    )
    public PaymentResult charge(Order order) {

        String maskedCard = order.getCardNumber(); // Ya viene enmascarado del objeto
        log.info("Cargando {} a tarjeta {}", order.getAmount(), maskedCard);

        try {
            // Llamada a gateway externo
            ExternalPaymentResponse response = paymentGateway.charge(
                order.getCardNumber(), 
                order.getCvv(), 
                order.getAmount()
            );

            log.info("Respuesta del gateway recibida para transacción {}", response.getTransactionId());

            return new PaymentResult(response.getTransactionId(), response.getStatus());

        } catch (Exception e) {
            log.error("Error en gateway de pago para orden {}: {}", order.getId(), e.getMessage());
            throw new PaymentGatewayException("Payment failed", e);
        }
    }
}

// === Controllers ===
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final MaskedLogger log = MaskedLogger.getLogger(OrderController.class);

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @RequestBody CreateOrderRequest request,
        @RequestHeader("Authorization") String authToken
    ) {
        // Log manual con enmascaramiento automático
        log.info("Recibida solicitud de orden: {}", request);

        try {
            Order order = mapToOrder(request);
            OrderResult result = orderService.processOrder(order.getId(), order, extractApiKey(authToken));

            return ResponseEntity.ok(new OrderResponse(result));

        } catch (ValidationException e) {
            log.warn("Validación fallida para request: {}", request);
            return ResponseEntity.badRequest().build();

        } catch (InsufficientFundsException e) {
            log.warn("Fondos insuficientes para customer {}", request.getCustomerId());
            return ResponseEntity.status(402).build(); // Payment Required
        }
    }
}
```

### Sistema de Autenticación y Autorización

```java
// === Entidades de Seguridad ===
public class LoginRequest {
    private String username;

    @Exclude
    private String password;

    @Mask(visibleChars = 6, position = Mask.Position.SUFFIX)
    private String deviceId;
}

public class User {
    private String id;
    private String username;

    @Mask(visibleChars = 4, position = Mask.Position.PREFIX)
    private String email;

    @Exclude
    private String passwordHash;

    @Exclude
    private String salt;

    private List<String> roles;
}

public class JwtToken {
    @Mask(visibleChars = 10, position = Mask.Position.PREFIX)
    private String accessToken;

    @Mask(visibleChars = 10, position = Mask.Position.PREFIX)
    private String refreshToken;

    private long expiresIn;
}

// === Servicios ===
@Service
public class AuthenticationService {

    private static final MaskedLogger log = MaskedLogger.getLogger(AuthenticationService.class);

    @Loggable(
        message = "Autenticando usuario",
        level = Level.INFO,
        includeResult = false, // No logear tokens en resultado
        exceptions = {
            @ExceptionLog(
                value = InvalidCredentialsException.class,
                message = "Credenciales inválidas para usuario {0}",
                printStackTrace = false
            ),
            @ExceptionLog(
                value = AccountLockedException.class,
                message = "Cuenta bloqueada para usuario {0}",
                printStackTrace = false
            ),
            @ExceptionLog(
                value = SecurityException.class,
                message = "Violación de seguridad en autenticación",
                printStackTrace = true
            )
        }
    )
    public JwtToken authenticate(LoginRequest request) {

        log.info("Validando credenciales para usuario: {}", request.getUsername());

        // Buscar usuario
        User user = userService.findByUsername(request.getUsername())
            .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));

        // Verificar password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password incorrecto para usuario: {}", request.getUsername());
            throw new InvalidCredentialsException("Password incorrecto");
        }

        // Verificar cuenta activa
        if (user.isLocked()) {
            log.warn("Intento de login en cuenta bloqueada: {}", request.getUsername());
            throw new AccountLockedException("Cuenta bloqueada");
        }

        // Generar tokens
        JwtToken tokens = jwtService.generateTokens(user);

        log.info("Usuario {} autenticado exitosamente desde dispositivo {}", 
                 user.getUsername(), request.getDeviceId());

        return tokens;
    }

    @Loggable(
        message = "Refrescando token",
        includeArgs = false, // No logear refresh token en args
        includeResult = false // No logear nuevos tokens
    )
    public JwtToken refreshToken(@Exclude String refreshToken) {

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            log.warn("Intento de refresh con token inválido");
            throw new InvalidTokenException("Refresh token inválido");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        JwtToken newTokens = jwtService.generateTokens(user);

        log.info("Token refrescado exitosamente para usuario: {}", username);

        return newTokens;
    }
}

@Service  
public class AuthorizationService {

    private static final MaskedLogger log = MaskedLogger.getLogger(AuthorizationService.class);

    @Loggable(level = Level.DEBUG)
    public boolean hasPermission(
        @Mask(visibleChars = 8) String accessToken,
        String resource,
        String action
    ) {
        try {
            String username = jwtService.extractUsername(accessToken);
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

            boolean hasPermission = permissionService.checkPermission(user, resource, action);

            if (!hasPermission) {
                log.warn("Acceso denegado para usuario {} al recurso {} con acción {}", 
                         username, resource, action);
            }

            return hasPermission;

        } catch (Exception e) {
            log.error("Error verificando permisos para recurso {}: {}", resource, e.getMessage());
            return false;
        }
    }
}

// === Interceptors y Filters ===
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final MaskedLogger log = MaskedLogger.getLogger(AuthenticationInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Request sin token de autorización a {}", request.getRequestURI());
            response.setStatus(401);
            return false;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtService.isValidAccessToken(token)) {
                log.warn("Token inválido en request a {}", request.getRequestURI());
                response.setStatus(401);
                return false;
            }

            String username = jwtService.extractUsername(token);
            log.debug("Request autorizado para usuario {} a {}", username, request.getRequestURI());

            return true;

        } catch (Exception e) {
            log.error("Error validando token para {}: {}", request.getRequestURI(), e.getMessage());
            response.setStatus(500);
            return false;
        }
    }
}
```

Este README completo proporciona toda la información necesaria para usar la librería de logging eficientemente, desde casos básicos hasta implementaciones complejas en sistemas empresariales.

---

Para más información o reporte de problemas, por favor contacta al equipo de desarrollo o consulta la documentación completa del proyecto.