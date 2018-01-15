FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

ADD ./api/target/payment-api-1.0-SNAPSHOT.jar /app

EXPOSE 8088

CMD ["java", "-jar", "payment-api-1.0-SNAPSHOT.jar"]