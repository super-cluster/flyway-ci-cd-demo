# Use a lightweight Java 21 runtime image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from the Maven target directory into the container
# (We assume 'mvn clean verify' has already been executed before running docker build)
COPY target/*.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# Define the command to start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
