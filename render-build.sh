#!/bin/bash

# Render build script
echo "🚀 Building Spring Boot application for Render..."

# Make sure we're in the right directory
cd /opt/render/project/src

# Make mvnw executable
chmod +x mvnw

# Clean and package the application
echo "📦 Cleaning and packaging application..."
./mvnw clean package -DskipTests -B

echo "✅ Build completed successfully!"

# Verify the JAR was created
if [ -f "target/scprojectjava2-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 JAR file created: target/scprojectjava2-0.0.1-SNAPSHOT.jar"
    ls -la target/scprojectjava2-0.0.1-SNAPSHOT.jar
else
    echo "❌ JAR file not found!"
    exit 1
fi
