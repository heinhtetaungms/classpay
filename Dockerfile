# Use Maven with OpenJDK for the build stage
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Set the working directory for the build
WORKDIR /build

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Build the project and package the JAR (skip tests)
RUN mvn clean package -DskipTests

# Create the final image, based on a smaller base image
FROM amazoncorretto:21-alpine-jdk

# Set the timezone environment variable
ENV TZ=Asia/Yangon

# Set the working directory in the final image
WORKDIR /app

# Copy the JAR file from the Maven build (from the builder image)
COPY --from=build /build/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set the entrypoint to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
