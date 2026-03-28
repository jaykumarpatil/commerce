# Admin Service Dockerfile
FROM eclipse-temurin:25-jre

WORKDIR /app

# Create a non-root user
RUN addgroup -S app && adduser -S -G app app

# Copy the application JAR
COPY build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R app:app /app

USER app

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
