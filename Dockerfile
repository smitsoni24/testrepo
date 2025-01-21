FROM openjdk:21-jdk

ENV ENVIRONMENT default
ARG JAR_FILE=@project.artifactId@-@project.version@.jar
COPY target/*.jar app.jar

HEALTHCHECK --interval=5s --retries=10 CMD curl -fs http://localhost:8090/actuator/health || exit 1

EXPOSE 8090

CMD java -Djava.security.egd=file:/dev/./urandom -jar ./app.jar
