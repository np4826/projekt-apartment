FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

ADD ./api/target/review-api-1.0-SNAPSHOT.jar /app

EXPOSE 8085

CMD ["java", "-jar", "review-api-1.0-SNAPSHOT.jar"]