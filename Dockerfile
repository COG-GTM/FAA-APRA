FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 -S apra && \
    adduser -u 1001 -S apra -G apra

COPY --from=builder /app/target/apra.jar /app/apra.jar

RUN mkdir -p /opt/apra/conf && \
    chown -R apra:apra /opt/apra

USER apra

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/management/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/apra.jar"]
