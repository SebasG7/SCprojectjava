# CREDENCIALES DE ACCESO - RENDER DEPLOYMENT

## 🔐 Usuarios de Prueba Creados Automáticamente

La aplicación desplegada en Render crea automáticamente usuarios de prueba si la base de datos está vacía.

### Administrador
- **Usuario:** `admin`
- **Contraseña:** `admin123`
- **Rol:** Administrador
- **Permisos:** Acceso completo al sistema

### Cajero
- **Usuario:** `cajero`
- **Contraseña:** `cajero123`
- **Rol:** Cajero
- **Permisos:** Acceso limitado para operaciones de venta

## 🌐 URL de la Aplicación en Render

Tu aplicación debería estar disponible en la URL que Render te proporcionó, algo como:
```
https://tu-app-name.onrender.com
```

## 🔍 Verificación de Login

1. Ve a la URL de tu aplicación en Render
2. Serás redirigido a `/login`
3. Ingresa las credenciales del administrador:
   - Usuario: `admin`
   - Contraseña: `admin123`
4. Deberías ser redirigido al dashboard principal

## 🛠️ Configuración Personalizada

Si deseas cambiar las credenciales del administrador, puedes hacerlo mediante variables de entorno en Render:

### Variables de Entorno en Render:
```
APP_ADMIN_USERNAME=tu_usuario_personalizado
APP_ADMIN_PASSWORD=tu_contraseña_segura
APP_ADMIN_NAME=Nombre del Administrador
```

## 📝 Notas Importantes

1. **Base de Datos:** La aplicación usa H2 en memoria, por lo que los datos se perderán al reiniciar el servicio.

2. **Inicialización:** Los usuarios de prueba solo se crean si la base de datos está completamente vacía.

3. **Logs:** Puedes verificar en los logs de Render que los usuarios se crearon correctamente. Busca mensajes como:
   ```
   ✓ Usuario administrador creado exitosamente
   ✓ Usuario cajero creado exitosamente
   ```

4. **Primer Acceso:** En el primer acceso después del despliegue, puede tomar unos segundos extra mientras se inicializa la base de datos.

## 🚨 Troubleshooting

### Si no puedes iniciar sesión:

1. **Verifica los logs en Render:**
   - Ve a tu servicio en Render
   - Revisa la pestaña "Logs"
   - Busca mensajes de creación de usuarios

2. **Credenciales correctas:**
   - Usuario: `admin` (todo en minúsculas)
   - Contraseña: `admin123` (exactamente así)

3. **Reinicia el servicio:**
   - En el dashboard de Render, haz clic en "Manual Deploy"
   - Esto recreará los usuarios de prueba

### Si los usuarios no se están creando:

1. Verifica que el perfil H2 esté activo en Render
2. Asegúrate de que no hay variables de entorno conflictivas
3. Revisa los logs para ver si hay errores durante la inicialización

## 🔄 Próximos Pasos

Una vez confirmado que el login funciona:

1. **Explora la aplicación:** Navega por todas las funcionalidades
2. **Crea contenido de prueba:** Añade productos, categorías, etc.
3. **Prueba las ventas:** Realiza algunas operaciones de prueba
4. **Documenta:** Anota cualquier funcionalidad que necesite ajustes

## 💡 Recomendación para Producción

Para un entorno de producción real:
- Cambia a una base de datos persistente (PostgreSQL)
- Usa contraseñas seguras
- Configura variables de entorno apropiadas
- Implementa un sistema de respaldos
