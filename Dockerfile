FROM adoptopenjdk/openjdk11
ARG JAR_FILE_PATH= /demo/build/libs/*.jar
COPY ${JAR_FILE_PATH} app.jar
EXPOSE 80/tcp
ENTRYPOINT ["java", "-jar", "app.jar"]