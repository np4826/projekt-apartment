FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

ADD ./api/target/avaliability-api-1.0-SNAPSHOT.jar /app

EXPOSE 8084

CMD ["java", "-jar", "avaliability-api-1.0-SNAPSHOT.jar"]