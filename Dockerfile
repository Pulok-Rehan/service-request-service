# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY service-request-service-0.0.1-SNAPSHOT.jar app.jar
COPY application.properties application.properties

# Expose the port your Spring Boot app runs on
EXPOSE 8085

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties"]
