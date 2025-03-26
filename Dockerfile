FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
  
  # Pastikan JDK 21 terdeteksi
RUN java -version
  
  # Build dengan Maven
RUN --mount=type=cache,id=s/75cb767a-978f-4022-b4fb-4f5aae287bf6-m2/repository,target=/app/.m2/repository \
chmod +x ./mvnw && \
./mvnw -DoutputFile=target/mvn-dependency-list.log -B -DskipTests clean dependency:list install