# Render.com Deployment Configuration

## 📋 Configuración del Servicio Web

### Configuración Básica:
- **Tipo de Servicio**: Web Service
- **Entorno**: Docker
- **Repositorio**: Tu repositorio de GitHub
- **Rama**: main (o la rama que prefieras)

### Configuración de Build:
- **Build Command**: `bash render-build.sh`
- **Start Command**: `java -Dspring.profiles.active=production -jar target/scprojectjava2-0.0.1-SNAPSHOT.jar`
- **Dockerfile**: `Dockerfile.simple` (recomendado para evitar problemas con Maven wrapper)

### Variables de Entorno Requeridas:

#### ✅ SOLUCIÓN INMEDIATA (H2 en memoria):
```
SPRING_PROFILES_ACTIVE=h2
```
**¡Tu aplicación funcionará inmediatamente!**

#### 🐘 Con PostgreSQL (Render Database):
```
DATABASE_URL=postgresql://user:pass@host:5432/database
SPRING_PROFILES_ACTIVE=postgres
```

#### 🐬 Con MySQL Externa:
```
DATABASE_URL=jdbc:mysql://tu-host:3306/tu-base-de-datos
DATABASE_USERNAME=tu-usuario
DATABASE_PASSWORD=tu-contraseña
SPRING_PROFILES_ACTIVE=production
```

#### 🤖 Auto-detección (Recomendado):
```
# No configurar SPRING_PROFILES_ACTIVE
# El script detectará automáticamente:
# - Si hay DATABASE_URL → usa el perfil apropiado
# - Si no hay DATABASE_URL → usa H2 automáticamente
```

#### Configuración de Correo (opcional):
```
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=tu-email@gmail.com
SPRING_MAIL_PASSWORD=tu-password-de-aplicacion
```

## 🗄️ Configuración de Base de Datos

### ✅ Solución Rápida - H2 en Memoria (Para Pruebas)
Si solo quieres probar el despliegue rápidamente:

**Variables de Entorno**:
```
SPRING_PROFILES_ACTIVE=h2
```

No necesitas configurar ninguna base de datos externa. La aplicación usará H2 en memoria.

### 🐘 Opción 1: PostgreSQL en Render (Recomendado)
1. **Crear PostgreSQL Database en Render**:
   - Dashboard → "New" → "PostgreSQL"
   - Copiar la "Internal Database URL"

2. **Variables de Entorno**:
   ```
   DATABASE_URL=postgresql://user:pass@host:5432/database
   SPRING_PROFILES_ACTIVE=postgres
   ```

### 🐬 Opción 2: MySQL Externa (Avanzado)
- Usar PlanetScale, AWS RDS, o cualquier MySQL hosting
- Variables de entorno:
   ```
   DATABASE_URL=jdbc:mysql://host:3306/database
   DATABASE_USERNAME=tu-usuario
   DATABASE_PASSWORD=tu-contraseña
   SPRING_PROFILES_ACTIVE=production
   ```

### 📊 Perfiles Disponibles
- **`h2`** - Base de datos en memoria (pruebas)
- **`postgres`** - PostgreSQL (recomendado para producción)
- **`production`** - MySQL (requiere configuración externa)

## 🚀 Pasos para Desplegar en Render

1. **Subir el código a GitHub**:
   ```bash
   git add .
   git commit -m "Add Docker configuration for Render deployment"
   git push origin main
   ```

2. **Crear un nuevo Web Service en Render**:
   - Ir a https://render.com
   - Hacer clic en "New" → "Web Service"
   - Conectar tu repositorio de GitHub
   - Configurar según las especificaciones arriba

3. **Configurar variables de entorno**:
   - En el dashboard de Render, ir a "Environment"
   - Agregar las variables listadas arriba

4. **Configurar la base de datos**:
   - Si usas una base de datos externa, configurar las variables DATABASE_*
   - Si usas Render Database, crear una instancia de PostgreSQL/MySQL

5. **Hacer el deploy**:
   - Render automáticamente detectará los cambios y construirá la aplicación
   - El proceso tomará unos minutos la primera vez

## 🔧 Comandos Útiles para Desarrollo Local

### Usando Docker Compose:
```bash
# Ejecutar en desarrollo
docker-compose up -d

# Ver logs
docker-compose logs -f app

# Detener servicios
docker-compose down
```

### Usando solo Docker:
```bash
# Construir imagen
docker build -t scprojectjava2 .

# Ejecutar contenedor
docker run -p 8080:8080 scprojectjava2
```

## 📝 Notas Importantes

1. **Puerto**: Render asigna automáticamente un puerto através de la variable `PORT`
2. **Perfil de Spring**: Se usa `production` por defecto
3. **Base de Datos**: Asegúrate de que la base de datos esté accesible desde internet
4. **SSL**: Render proporciona HTTPS automáticamente
5. **Logs**: Los logs están disponibles en el dashboard de Render

## 🐛 Troubleshooting

### Problemas Comunes de Build

#### ✅ Build exitoso pero app no conecta a base de datos
**ESTE ES TU PROBLEMA ACTUAL** - La aplicación se construye y ejecuta perfectamente, pero falla en la base de datos.

**Síntomas:**
- ✅ Build exitoso
- ✅ "Starting Scprojectjava2Application"
- ✅ "Tomcat initialized with port 8080"  
- ❌ "No active profile set, falling back to 1 default profile: 'default'"
- ❌ "Communications link failure"

**SOLUCIÓN INMEDIATA:**
En Render Dashboard → Environment → Agregar:
```
SPRING_PROFILES_ACTIVE=h2
```

**¿Por qué pasa esto?**
- Tu aplicación intenta usar el perfil "default"
- El perfil "default" usa la configuración de `application.properties` (MySQL localhost)
- En Render no hay MySQL localhost, por eso falla

**Resultado después del fix:**
```
INFO: Starting with profile: h2
INFO: Using H2 database  
INFO: Tomcat started on port 8080
INFO: Application started successfully
```

...existing code...

**1. Configurar variables de entorno de base de datos**:
```
DATABASE_URL=jdbc:mysql://tu-host:3306/tu-database
DATABASE_USERNAME=tu-usuario  
DATABASE_PASSWORD=tu-contraseña
SPRING_PROFILES_ACTIVE=production
```

**2. Opciones de base de datos**:
- **Render Database**: Crear una instancia MySQL en Render
- **PlanetScale**: Base de datos MySQL gratuita y escalable
- **AWS RDS**: Para producción seria
- **Railway**: Alternativa simple y gratuita

**3. Start Command corregido**:
```
java -Dspring.profiles.active=production -jar target/scprojectjava2-0.0.1-SNAPSHOT.jar
```

#### Error: "No active profile set"
**Solución**: Agregar variable de entorno:
```
SPRING_PROFILES_ACTIVE=production
```

#### Error: "Communications link failure"
**Problema**: No hay base de datos configurada
**Solución**: Configurar una de estas opciones:

**Opción 1 - Render Database** (Recomendado):
1. En Render Dashboard → "New" → "PostgreSQL" 
2. Copiar la DATABASE_URL
3. Modificar application-production.properties para PostgreSQL:
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Opción 2 - Base de datos en memoria** (Solo para pruebas):
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Error: "Connection refused" o "Cannot connect to database"
**Solución 1**: Usar Dockerfile.simple
- En Render, cambiar el Dockerfile a `Dockerfile.simple`
- Este usa Maven directo en lugar del wrapper

**Solución 2**: Verificar .dockerignore
- Asegurarse de que `.mvn/wrapper/` no esté excluido
- Los archivos del wrapper deben estar incluidos

**Solución 3**: Build Command alternativo
- Usar: `mvn clean package -DskipTests && cp target/*.jar app.jar`
- Start Command: `java -jar app.jar`

#### Error de permisos en mvnw
**Solución**: Usar el build script
- Build Command: `bash render-build.sh`
- Este script maneja permisos automáticamente

#### Build muy lento o timeout
**Solución**: Usar multi-stage build
- El `Dockerfile.simple` usa multi-stage para optimizar

### Archivos Dockerfile Disponibles
1. **`Dockerfile`** - Versión estándar con Maven wrapper
2. **`Dockerfile.simple`** - Versión con Maven directo (recomendado para Render)
3. **`Dockerfile.render`** - Versión optimizada con todas las características

- **Error de conexión a base de datos**: Verificar variables de entorno DATABASE_*
- **Error 404**: Verificar que las rutas estén configuradas correctamente
- **Aplicación no inicia**: Revisar logs en Render dashboard
- **TimeOut durante build**: El build toma tiempo la primera vez, ser paciente

## 📚 Recursos Adicionales

- [Documentación de Render](https://render.com/docs)
- [Spring Boot en Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Configuración de Base de Datos](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource)
