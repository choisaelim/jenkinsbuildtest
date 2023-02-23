FROM adoptopenjdk/openjdk11
CMD ["./mvnw", "clean", "package"]
ARG JAR_FILE_PATH=/var/jenkins_home/workspace/pipe/demo/build/libs/*.jar
COPY ${JAR_FILE_PATH} app.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java", "-jar", "app.jar"]