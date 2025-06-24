#!/bin/bash

# Render build script
echo "🚀 Building Spring Boot application for Render..."

# Make sure we're in the right directory
cd /opt/render/project/src

# Check if Maven wrapper exists and is functional
if [ -f "mvnw" ] && [ -f ".mvn/wrapper/maven-wrapper.properties" ]; then
    echo "📦 Using Maven wrapper..."
    # Make mvnw executable
    chmod +x mvnw
    
    # Clean and package the application
    echo "📦 Cleaning and packaging application with wrapper..."
    ./mvnw clean package -DskipTests -B
else
    echo "⚠️  Maven wrapper not available, using system Maven..."
    # Fallback to system Maven if wrapper is not available
    mvn clean package -DskipTests -B
fi

echo "✅ Build completed successfully!"

# Verify the JAR was created
if [ -f "target/scprojectjava2-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 JAR file created: target/scprojectjava2-0.0.1-SNAPSHOT.jar"
    ls -la target/scprojectjava2-0.0.1-SNAPSHOT.jar
    
    # Copy to a simpler name for easier reference
    cp target/scprojectjava2-0.0.1-SNAPSHOT.jar app.jar
    echo "📦 Also created: app.jar"
else
    echo "❌ JAR file not found!"
    echo "📁 Contents of target directory:"
    ls -la target/ || echo "Target directory not found"
    exit 1
fi
