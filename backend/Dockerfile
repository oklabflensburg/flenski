# Build-Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Run-Stage
FROM eclipse-temurin:21-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

