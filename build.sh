#!/bin/bash

# Script de construcción para Render
echo "🚀 Iniciando construcción del proyecto..."

# Instalar dependencias y compilar la aplicación
echo "📦 Instalando dependencias y compilando..."
./mvnw clean package -DskipTests

echo "✅ Construcción completada exitosamente!"
