FROM node:current-alpine3.13 AS jsbuild
WORKDIR /js
COPY ./client/chart-generator/ .
RUN npm ci
RUN npm run build

FROM node:current-alpine3.13 AS cssbuild
WORKDIR /css
COPY ./client/css/ .
RUN npm ci
RUN npm run build

FROM openjdk:16-jdk-slim AS serverbuild
WORKDIR /server
COPY ./server/ .
COPY --from=jsbuild /js/out/snustrack.js ./src/main/resources/static/js/snustrack.js
COPY --from=cssbuild /css/out/snustrack.css ./src/main/resources/static/css/snustrack.css
RUN ./mvnw package

FROM openjdk:16-jdk-slim
WORKDIR /app
COPY --from=serverbuild /server/target/server-0.0.1-SNAPSHOT.jar .
CMD [ "java", "-jar", "server-0.0.1-SNAPSHOT.jar" ]