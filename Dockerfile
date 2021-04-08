FROM openjdk:16-jdk-slim AS build

WORKDIR /app
COPY . .

RUN ./mvnw package

RUN useradd \
    --create-home \
    --home-dir /var/lib/spring \
    --shell /bin/false \
    --user-group \
    spring

RUN cp -r target/* /var/lib/spring

USER spring
WORKDIR /var/lib/spring

ENTRYPOINT [ \
    "/usr/local/openjdk-16/bin/java", \
    "-jar", \
    "trabacco-0.0.1-SNAPSHOT.jar" \
]
