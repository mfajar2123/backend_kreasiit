# Gunakan JDK 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Salin seluruh proyek termasuk mvnw dan pom.xml
COPY . .

# Build aplikasi dengan Maven
RUN ./mvnw -B -DskipTests clean package

# Jalankan aplikasi
ENTRYPOINT ["java", "-jar", "target/website-fajar-0.0.1-SNAPSHOT.jar"]