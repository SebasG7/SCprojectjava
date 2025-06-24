# Render.com Deployment Configuration

## 📋 Configuración del Servicio Web

### Configuración Básica:
- **Tipo de Servicio**: Web Service
- **Entorno**: Docker
- **Repositorio**: Tu repositorio de GitHub
- **Rama**: main (o la rama que prefieras)

### Configuración de Build:
- **Build Command**: `./build.sh`
- **Start Command**: `./start.sh`
- **Dockerfile**: `Dockerfile` (en la raíz del proyecto)

### Variables de Entorno Requeridas:

#### Base de Datos (si usas una base de datos externa):
```
DATABASE_URL=jdbc:mysql://tu-host:3306/tu-base-de-datos
DATABASE_USERNAME=tu-usuario
DATABASE_PASSWORD=tu-contraseña
```

#### Configuración de la Aplicación:
```
SPRING_PROFILES_ACTIVE=production
PORT=8080
```

#### Configuración de Correo (opcional):
```
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=tu-email@gmail.com
SPRING_MAIL_PASSWORD=tu-password-de-aplicacion
```

## 🗄️ Configuración de Base de Datos

### Opción 1: Base de Datos Externa (Recomendado)
- Usar un servicio como PlanetScale, AWS RDS, o similar
- Configurar las variables de entorno con los datos de conexión

### Opción 2: Base de Datos en el mismo servicio (No recomendado para producción)
- Usar SQLite o H2 para desarrollo/pruebas
- Modificar application-production.properties para usar una base de datos en memoria

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

- **Error de conexión a base de datos**: Verificar variables de entorno DATABASE_*
- **Error 404**: Verificar que las rutas estén configuradas correctamente
- **Aplicación no inicia**: Revisar logs en Render dashboard
- **TimeOut durante build**: El build toma tiempo la primera vez, ser paciente

## 📚 Recursos Adicionales

- [Documentación de Render](https://render.com/docs)
- [Spring Boot en Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Configuración de Base de Datos](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource)
