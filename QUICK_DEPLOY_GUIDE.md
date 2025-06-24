# 🚀 Guía de Despliegue Rápido en Render

## ✅ Tu aplicación ya se compila correctamente!

El build está funcionando bien. Solo necesitas configurar la base de datos.

## 🎯 Pasos Rápidos para Desplegar

### 1. Commitea los cambios
```bash
git add .
git commit -m "Add Docker config and database alternatives"
git push origin main
```

### 2. En Render Dashboard

#### Configuración Básica:
- **Environment**: Docker
- **Build Command**: `bash render-build.sh`
- **Start Command**: `bash start.sh`

#### Variables de Entorno (Elige una opción):

**Opción A - Prueba Rápida (H2 en memoria)**:
```
SPRING_PROFILES_ACTIVE=h2
```

**Opción B - PostgreSQL (Recomendado)**:
1. Crear PostgreSQL en Render
2. Configurar:
```
DATABASE_URL=postgresql://...
SPRING_PROFILES_ACTIVE=postgres
```

**Opción C - MySQL Externa**:
```
DATABASE_URL=jdbc:mysql://...
DATABASE_USERNAME=...
DATABASE_PASSWORD=...
SPRING_PROFILES_ACTIVE=production
```

### 3. ¡Deploy!
- Render detectará automáticamente los cambios
- El primer deploy tomará ~5-10 minutos
- Los siguientes serán más rápidos

## 🔧 Troubleshooting

### "Communications link failure"
✅ **Solucionado**: Usa `SPRING_PROFILES_ACTIVE=h2` para una solución rápida

### "No active profile set"
✅ **Solucionado**: El script de start ahora detecta automáticamente el perfil

### "No open ports detected"
✅ **Normal**: Render necesita unos segundos para detectar el puerto 8080

## 📞 Soporte
Si algo falla, revisa los logs en Render Dashboard → tu servicio → "Logs"
