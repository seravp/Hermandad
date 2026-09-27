FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/gestion-hermandad-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=docker"]