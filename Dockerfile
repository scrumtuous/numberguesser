FROM maven:3.8.6-eclipse-temurin-8 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM tomcat:9.0

RUN groupadd --system appgroup \
    && useradd --system --create-home --gid appgroup appuser \
    && chown -R appuser:appgroup /usr/local/tomcat

COPY --from=build /workspace/target/numberguesser.war /usr/local/tomcat/webapps/

USER appuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080' || exit 1

CMD ["catalina.sh", "run"]
