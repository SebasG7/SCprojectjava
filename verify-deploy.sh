#!/bin/bash

# Script de verificación rápida para el despliegue en Render
# Este script valida que los cambios principales estén funcionando

echo "🔍 VERIFICACIÓN RÁPIDA DE DEPLOY EN RENDER"
echo "=========================================="

echo ""
echo "📁 Verificando archivos clave..."

# Verificar archivos Docker
if [ -f "Dockerfile" ]; then
    echo "✅ Dockerfile existe"
else
    echo "❌ Dockerfile NO encontrado"
fi

if [ -f "render-build.sh" ]; then
    echo "✅ render-build.sh existe"
else
    echo "❌ render-build.sh NO encontrado"
fi

if [ -f "start.sh" ]; then
    echo "✅ start.sh existe"
else
    echo "❌ start.sh NO encontrado"
fi

# Verificar configuraciones
echo ""
echo "⚙️ Verificando configuraciones..."

if [ -f "src/main/resources/application-h2.properties" ]; then
    echo "✅ application-h2.properties existe"
else
    echo "❌ application-h2.properties NO encontrado"
fi

if [ -f "src/main/java/com/scprojectjava2/config/DataInitializer.java" ]; then
    echo "✅ DataInitializer.java existe"
else
    echo "❌ DataInitializer.java NO encontrado"
fi

echo ""
echo "📋 CREDENCIALES DE PRUEBA"
echo "========================"
echo "Administrador:"
echo "  Usuario: admin"
echo "  Contraseña: admin123"
echo ""
echo "Cajero:"
echo "  Usuario: cajero"
echo "  Contraseña: cajero123"

echo ""
echo "🚀 COMANDOS PARA RENDER"
echo "======================"
echo "Build Command: bash render-build.sh"
echo "Start Command: bash start.sh"
echo ""
echo "Variable de entorno recomendada:"
echo "SPRING_PROFILES_ACTIVE=h2"

echo ""
echo "📊 ESTADO DEL PROYECTO"
echo "====================="

# Verificar si Maven funciona
if command -v mvn &> /dev/null || [ -f "./mvnw" ]; then
    echo "✅ Maven disponible"
else
    echo "❌ Maven NO disponible"
fi

# Verificar si Git está configurado
if [ -d ".git" ]; then
    echo "✅ Repositorio Git configurado"
    git_status=$(git status --porcelain 2>/dev/null)
    if [ -z "$git_status" ]; then
        echo "✅ No hay cambios pendientes"
    else
        echo "⚠️ Hay cambios sin commitear"
    fi
else
    echo "❌ No es un repositorio Git"
fi

echo ""
echo "🎯 PRÓXIMOS PASOS"
echo "================"
echo "1. Hacer commit y push de los cambios:"
echo "   git add ."
echo "   git commit -m 'Add automatic user creation for Render'"
echo "   git push origin main"
echo ""
echo "2. En Render Dashboard:"
echo "   - Configurar Build Command: bash render-build.sh"
echo "   - Configurar Start Command: bash start.sh"
echo "   - Agregar variable: SPRING_PROFILES_ACTIVE=h2"
echo ""
echo "3. Después del despliegue:"
echo "   - Ir a la URL de tu app en Render"
echo "   - Iniciar sesión con admin/admin123"
echo "   - ¡Listo!"

echo ""
echo "📚 DOCUMENTACIÓN"
echo "================"
echo "- LOGIN_CREDENTIALS.md - Credenciales de acceso"
echo "- QUICK_DEPLOY_GUIDE.md - Guía rápida de despliegue"
echo "- RENDER_DEPLOYMENT.md - Documentación completa"
