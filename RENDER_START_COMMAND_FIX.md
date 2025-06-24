# 🚨 CONFIGURACIÓN INMEDIATA PARA RENDER

## El problema: La variable de entorno no funciona

### 🎯 SOLUCIÓN GARANTIZADA - Cambiar Start Command

## Pasos en Render Dashboard:

### 1. Ve a tu servicio → **Settings**

### 2. En la sección **Build & Deploy**, busca **Start Command**

### 3. Reemplaza el Start Command actual con:

```bash
java -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -Dspring.profiles.active=h2 -Dserver.port=$PORT -jar target/scprojectjava2-0.0.1-SNAPSHOT.jar
```

### 4. Hacer clic en **Save Changes**

### 5. **Manual Deploy** (o automático si hay cambios en Git)

---

## ✅ ¿Por qué esta solución funciona?

- **Bypasea variables de entorno**: Usa `-Dspring.profiles.active=h2` directamente
- **Usa H2 en memoria**: No necesita base de datos externa
- **Optimizado para contenedores**: Incluye flags de JVM apropiados
- **Puerto dinámico**: Usa `$PORT` que Render asigna automáticamente

---

## 🔄 Configuración Alternativa

Si el start command anterior no funciona, usar:

```bash
bash start-forced.sh
```

Este script fuerza H2 independientemente de cualquier configuración.

---

## 📊 Resultados Esperados

Después del cambio verás en los logs:

```
INFO: Starting Scprojectjava2Application using Java 21
INFO: The following profiles are active: h2
INFO: Using H2 database  
INFO: Tomcat started on port(s): 8080 (http)
INFO: Started Scprojectjava2Application
```

**¡Tu aplicación funcionará en el siguiente despliegue!** 🎉
