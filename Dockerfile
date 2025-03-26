FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Salin file dengan izin eksekusi untuk mvnw
COPY --chmod=+x mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Build
RUN ./mvnw -B -DskipTests clean package

# Jalankanchmod +x mvnw
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/website-fajar-0.0.1-SNAPSHOT.jar"]