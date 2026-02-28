FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY . .

RUN chmod +x mvnw && ./mvnw -DskipTests package

EXPOSE 8080
CMD ["sh", "-c", "java -jar target/*.jar"]