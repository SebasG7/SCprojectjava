#!/bin/bash

# Script de inicio para Render
echo "🚀 Iniciando aplicación Spring Boot..."

# Configurar variables de entorno por defecto si no están definidas
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-production}
export SERVER_PORT=${PORT:-8080}

# Ejecutar la aplicación
echo "🌐 Iniciando servidor en puerto $SERVER_PORT..."
java -jar target/scprojectjava2-0.0.1-SNAPSHOT.jar
