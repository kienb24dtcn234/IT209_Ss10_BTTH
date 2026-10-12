# Stage 1: build file .jar bằng Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# Stage 2: chỉ chạy file .jar với JRE nhẹ
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/student-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
