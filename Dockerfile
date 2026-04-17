FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Descarga el agente APM de Datadog
RUN apt-get update && apt-get install -y wget && \
    wget -O /dd-java-agent.jar https://dtdg.co/latest-java-tracer

COPY --from=build /app/target/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/dd-java-agent.jar"

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]