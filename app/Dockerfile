FROM amazoncorretto:17-alpine-jdk

EXPOSE 8080

COPY ./target/app-*.jar /usr/app/app.jar
WORKDIR /usr/app

ENTRYPOINT ["java", "-jar", "app.jar"]
