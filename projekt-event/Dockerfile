FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

ADD ./api/target/event-api-1.0-SNAPSHOT.jar /app

EXPOSE 8086

CMD ["java", "-jar", "event-api-1.0-SNAPSHOT.jar"]