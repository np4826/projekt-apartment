FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

ADD ./api/target/user-api-1.0-SNAPSHOT.jar /app

EXPOSE 8082

CMD ["java", "-jar", "user-api-1.0-SNAPSHOT.jar"]