FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

ADD ./api/target/apartment-api-1.0-SNAPSHOT.jar /app

EXPOSE 8081

CMD ["java", "-jar", "apartment-api-1.0-SNAPSHOT.jar"]