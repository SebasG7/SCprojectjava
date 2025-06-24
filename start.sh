#!/bin/bash

# Script de inicio para Render
echo "🚀 Iniciando aplicación Spring Boot..."

# Configurar variables de entorno por defecto si no están definidas
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-production}
export SERVER_PORT=${PORT:-8080}

# Buscar el archivo JAR
JAR_FILE=""
if [ -f "app.jar" ]; then
    JAR_FILE="app.jar"
elif [ -f "target/scprojectjava2-0.0.1-SNAPSHOT.jar" ]; then
    JAR_FILE="target/scprojectjava2-0.0.1-SNAPSHOT.jar"
else
    echo "❌ No se encontró el archivo JAR"
    echo "📁 Archivos disponibles:"
    ls -la
    ls -la target/ || echo "No hay directorio target"
    exit 1
fi

echo "📦 Usando JAR: $JAR_FILE"
echo "🌐 Iniciando servidor en puerto $SERVER_PORT..."

# Ejecutar la aplicación con optimizaciones para contenedores
java -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -jar "$JAR_FILE"
