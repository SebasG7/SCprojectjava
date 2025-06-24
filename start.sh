#!/bin/bash

# Script de inicio para Render
echo "🚀 Iniciando aplicación Spring Boot..."

# Determinar el perfil a usar basado en las variables de entorno disponibles
if [[ -n "$DATABASE_URL" ]]; then
    if [[ "$DATABASE_URL" == *"postgres"* ]]; then
        echo "🐘 Detectado PostgreSQL, usando perfil postgres"
        export SPRING_PROFILES_ACTIVE="postgres"
    elif [[ "$DATABASE_URL" == *"mysql"* ]]; then
        echo "🐬 Detectado MySQL, usando perfil production"
        export SPRING_PROFILES_ACTIVE="production"
    else
        echo "🗄️  Base de datos detectada, usando perfil por defecto"
        export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"production"}
    fi
else
    # Si no hay DATABASE_URL, usar H2 por defecto
    echo "⚡ No se detectó configuración de base de datos externa"
    echo "🗄️  Usando H2 en memoria para pruebas rápidas"
    export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"h2"}
fi

# Configurar puerto
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
echo "🔧 Perfil activo: $SPRING_PROFILES_ACTIVE"
echo "🌐 Iniciando servidor en puerto $SERVER_PORT..."

# Ejecutar la aplicación con optimizaciones para contenedores
java -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=80.0 \
     -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
     -Dserver.port=$SERVER_PORT \
     -jar "$JAR_FILE"
