FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app
COPY pre-registration-booking-service.jar /app/booking.jar
COPY application-local.properties /app/application-local.properties
COPY application-local.properties /app/application.properties

EXPOSE 9095

# Do not use profile "local": kernel-auth-adapter then tries offline JWT verify (KER-ATH-007).
CMD ["java", "-Dspring.profiles.active=default", "-Dspring.cloud.config.enabled=false", "-Dspring.config.additional-location=file:/app/", "-Dserver.port=9095", "-jar", "/app/booking.jar"]
