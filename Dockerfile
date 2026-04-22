# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven wrapper + pom first (cache optimization)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw -q dependency:go-offline

# Copy source
COPY src src

# Build jar
RUN ./mvnw clean package -DskipTests

ARG JAR_FILE=target/DocuSpaceApplication-0.0.1-SNAPSHOT.jar
# Extract Spring Boot layers
RUN java -Djarmode=tools -jar target/*.jar extract --layers --destination extracted


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN useradd -r -u 1001 springuser
USER springuser

# Copy layers (better caching)
COPY --from=builder /app/target/*.jar app.jar

# Environment defaults (can be overridden in ECS)
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=api
ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]