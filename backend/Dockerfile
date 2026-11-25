# Stage 1: Build the jar using Maven Wrapper
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Maven wrapper and POM first to leverage Docker layer caching
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy application source code and build the package
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Run the application on JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/grabaseat-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
