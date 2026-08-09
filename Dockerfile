FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/kafka-query-engine-operator-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]