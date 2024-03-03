FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR enrichment
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jdk-jammy
MAINTAINER chitzkoy@gmail.com
WORKDIR enrichment
COPY --from=builder enrichment/dependencies/ ./
COPY --from=builder enrichment/spring-boot-loader/ ./
COPY --from=builder enrichment/snapshot-dependencies/ ./
COPY --from=builder enrichment/application/ ./
ENV ACTIVE_PROFILES=default
ENV TZ=Europe/Moscow
ENTRYPOINT ["java", "-Duser.timezone=${TZ}", "-Dspring.profiles.active=${ACTIVE_PROFILES}", "org.springframework.boot.loader.JarLauncher"]