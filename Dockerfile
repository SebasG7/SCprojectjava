# Usar la imagen oficial de OpenJDK 21 con Alpine para menor tamaño
FROM openjdk:21-jdk-slim

# Información del mantenedor
LABEL maintainer="scprojectjava2"

# Instalar dependencias del sistema necesarias
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos Maven wrapper y pom.xml primero para aprovechar el cache de Docker
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn/ .mvn/
COPY pom.xml .

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Verificar que los archivos del wrapper existan
RUN ls -la .mvn/wrapper/

# Descargar dependencias (esto se cachea si no cambia el pom.xml)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Health check para Render
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para ejecutar la aplicación con optimizaciones para contenedores
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=80.0", "-jar", "target/scprojectjava2-0.0.1-SNAPSHOT.jar"]
