FROM adoptopenjdk/openjdk11
COPY /build/libs/demo-0.0.1-SNAPSHOT.jar /app.jar
EXPOSE 3030/tcp
ENTRYPOINT ["java", "-jar", "app.jar"]