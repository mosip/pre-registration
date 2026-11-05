# Official wiremock/wiremock:* images still ship Temurin 17.
ARG WIREMOCK_BASE=wiremock/wiremock:3.13.2-alpine
FROM ${WIREMOCK_BASE} AS upstream

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache bash openssl curl

WORKDIR /home/wiremock

COPY --from=upstream /var/wiremock/lib /var/wiremock/lib
COPY --from=upstream /docker-entrypoint.sh /docker-entrypoint.sh

RUN mkdir -p /home/wiremock/mappings /home/wiremock/__files /var/wiremock/extensions \
	&& chmod +x /docker-entrypoint.sh

EXPOSE 8080 8443
ENTRYPOINT ["/docker-entrypoint.sh"]
