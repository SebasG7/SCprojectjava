# Usar la imagen oficial de OpenJDK 21 con Alpine para menor tamaño
FROM openjdk:21-jdk-slim

# Información del mantenedor
LABEL maintainer="scprojectjava2"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos Maven wrapper y pom.xml primero para aprovechar el cache de Docker
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Descargar dependencias (esto se cachea si no cambia el pom.xml)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/scprojectjava2-0.0.1-SNAPSHOT.jar"]
