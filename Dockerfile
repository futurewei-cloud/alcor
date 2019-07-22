FROM openjdk:8
COPY ./target/AliothController-0.1-SNAPSHOT.jar /app/AliothController-0.1.jar
WORKDIR /app
CMD ["java", "-jar", "AliothController-0.1.jar"]