FROM adoptopenjdk/openjdk11
ARG JAR_FILE_PATH=/build/libs/*.jar
COPY ${JAR_FILE_PATH} /app.jar
EXPOSE 3030/tcp
ENTRYPOINT ["java", "-jar", "app.jar"]