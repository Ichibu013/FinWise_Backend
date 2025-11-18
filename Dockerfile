# # Importing JDK and copying required files
# FROM openjdk:21-jdk AS build
# WORKDIR /app
# COPY pom.xml .
# COPY src src

# # Copy Maven wrapper
# COPY mvnw .
# COPY .mvn .mvn

# # Set execution permission for the Maven wrapper
# RUN chmod +x ./mvnw
# RUN ./mvnw clean package -DskipTests

# # Stage 2: Create the final Docker image using OpenJDK 19
# FROM openjdk:19-jdk
# VOLUME /tmp

# # Copy the JAR from the build stage
# COPY --from=build /app/target/*.jar app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]
# EXPOSE 8080

# Stage 1: Build Stage
# Use an explicit, trusted, and well-maintained JDK image for building.
# eclipse-temurin:21-jdk-jammy is a good choice for JDK 21 on a common Linux base.
FROM **eclipse-temurin:21-jdk-jammy** AS build
WORKDIR /app

# Copy dependency files first to leverage Docker's build cache
COPY pom.xml .
COPY src src

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Set execution permission for the Maven wrapper
RUN chmod +x ./mvnw
# Run package to build the JAR (Note: The first time, this will also download dependencies)
RUN ./mvnw clean package -DskipTests

---

# Stage 2: Final Runtime Stage
# Use a much smaller JRE (Java Runtime Environment) image for the final deployment.
# This reduces the image size and attack surface significantly.
FROM **eclipse-temurin:21-jre-jammy**
VOLUME /tmp

# Copy the JAR from the named build stage
COPY --from=build /app/target/*.jar app.jar

# Specify the container's main command
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
