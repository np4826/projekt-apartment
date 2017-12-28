FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

ADD ./api/target/rent-api-1.0-SNAPSHOT.jar /app

EXPOSE 8083

CMD ["java", "-jar", "rent-api-1.0-SNAPSHOT.jar"]