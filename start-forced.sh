#!/bin/bash

# Script de inicio FORZADO para H2 - Sin depender de variables externas
echo "🚀 Iniciando aplicación Spring Boot con H2 forzado..."

# FORZAR H2 por defecto - sin importar variables externas
FORCED_PROFILE="h2"

# Solo cambiar si hay una DATABASE_URL específica configurada
if [[ -n "$DATABASE_URL" ]]; then
    if [[ "$DATABASE_URL" == *"postgres"* ]]; then
        echo "🐘 PostgreSQL detectado, usando perfil postgres"
        FORCED_PROFILE="postgres"
    elif [[ "$DATABASE_URL" == *"mysql"* ]]; then
        echo "🐬 MySQL detectado, usando perfil production"
        FORCED_PROFILE="production"
    fi
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
    exit 1
fi

echo "📦 Usando JAR: $JAR_FILE"
echo "🔧 PERFIL FORZADO: $FORCED_PROFILE"
echo "🌐 Puerto: $SERVER_PORT"

# EJECUTAR CON PERFIL FORZADO DIRECTAMENTE EN LA JVM
java -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=80.0 \
     -Dspring.profiles.active=$FORCED_PROFILE \
     -Dserver.port=$SERVER_PORT \
     -jar "$JAR_FILE"
