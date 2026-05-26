FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy parent pom first
COPY pom.xml ./pom.xml

# Copy all service poms so Maven doesn't complain about missing modules
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY arena-service/pom.xml ./arena-service/pom.xml
COPY booking-service/pom.xml ./booking-service/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY ai-service/pom.xml ./ai-service/pom.xml

# Copy actual source code for this service
COPY booking-service/src ./booking-service/src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -pl booking-service -am

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=builder /app/booking-service/target/*.jar app.jar
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]