# Stage 1: Build Stage (Uses a full JDK for compilation)
# We use 'eclipse-temurin:21-jdk-jammy' to fix the "not found" error.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy dependency files first to utilize Docker's build cache effectively
COPY pom.xml .
COPY src src

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Set execution permission for the Maven wrapper and build the JAR
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Final Runtime Stage (Uses a minimal JRE for deployment)
# Uses the smaller JRE-Slim image to reduce final image size.
FROM eclipse-temurin:21-jre-jammy

# The 'VOLUME /tmp' instruction has been removed as required.
# Temporary/Persistent storage should now be configured via Railway's volumes feature.

# Copy the built JAR artifact from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

# Specify the container's main command
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
