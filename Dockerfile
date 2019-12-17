FROM openjdk:8-jdk-alpine
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/GoogleCloudFinal-0.0.1-SNAPSHOT.jar"]