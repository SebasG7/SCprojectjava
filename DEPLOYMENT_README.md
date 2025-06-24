# 🚀 Despliegue en Render - Guía Rápida

## Archivos Docker Creados

He creado todos los archivos necesarios para desplegar tu proyecto Spring Boot en Render:

### 📁 Archivos Principales
- `Dockerfile` - Imagen Docker estándar con Maven wrapper
- `Dockerfile.simple` - **RECOMENDADO** - Usa Maven directo, más compatible con Render
- `Dockerfile.render` - Optimizado específicamente para Render
- `docker-compose.yml` - Para desarrollo local
- `build.sh` - Script de construcción
- `start.sh` - Script de inicio flexible
- `render-build.sh` - Script específico para Render con fallbacks

### ⚙️ Archivos de Configuración
- `application-production.properties` - Configuración para producción
- `.dockerignore` - Archivos a ignorar en Docker
- `.env.example` - Ejemplo de variables de entorno
- `package.json` - Configuración para Render

## 🚀 Pasos para Desplegar

### 1. Subir a GitHub
```bash
git add .
git commit -m "Add Docker and Render deployment configuration"
git push origin main
```

### 2. Configurar en Render
1. Ve a [render.com](https://render.com) y crea una cuenta
2. Conecta tu repositorio de GitHub
3. Crea un nuevo **Web Service**
4. Usa estas configuraciones:

#### Configuración del Servicio
- **Environment**: `Docker`
- **Dockerfile**: `Dockerfile.simple` (recomendado)
- **Build Command**: `bash render-build.sh`
- **Start Command**: `bash start.sh`

#### Variables de Entorno Esenciales:
```
SPRING_PROFILES_ACTIVE=h2    # Para prueba rápida
# O para PostgreSQL:
# DATABASE_URL=postgresql://...
# SPRING_PROFILES_ACTIVE=postgres
```

#### Variables de Entorno Requeridas
```
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=jdbc:mysql://host:3306/database
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
PORT=8080
```

### 3. Configurar Base de Datos

#### Opción A: Base de Datos Externa (Recomendado)
- Usa PlanetScale, AWS RDS, o cualquier MySQL hosting
- Configura las variables DATABASE_*

#### Opción B: Render Database
- Crea una instancia PostgreSQL en Render
- Cambia el driver en application-production.properties

## 🔧 Desarrollo Local

### Con Docker Compose
```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f app

# Detener
docker-compose down
```

### Solo la aplicación
```bash
# Construir imagen
docker build -t scprojectjava2 .

# Ejecutar
docker run -p 8080:8080 scprojectjava2
```

### Solución Rápida para el Error del Maven Wrapper

Si encuentras el error `cannot open ./.mvn/wrapper/maven-wrapper.properties`:

1. **Usar Dockerfile.simple (Recomendado)**:
   ```bash
   # Construir con el Dockerfile alternativo
   docker build -f Dockerfile.simple -t scprojectjava2 .
   ```

2. **O verificar archivos del wrapper**:
   ```bash
   # Verificar que existan los archivos
   ls -la .mvn/wrapper/
   git status  # Ver si están siendo ignorados
   ```

## 📋 Checklist Pre-Despliegue

- [ ] Código subido a GitHub
- [ ] Base de datos configurada y accesible
- [ ] Variables de entorno configuradas en Render
- [ ] Health check endpoint funcionando (`/actuator/health`)

## 🐛 Solución de Problemas

- **Build falla**: Revisa que mvnw tenga permisos de ejecución
- **App no conecta a BD**: Verifica variables DATABASE_*
- **Puerto incorrecto**: Render usa la variable PORT automáticamente
- **Timeout**: El primer despliegue puede tomar 10-15 minutos

## 📞 Soporte
Si tienes problemas, revisa los logs en el dashboard de Render y asegúrate de que todas las variables de entorno estén configuradas correctamente.
