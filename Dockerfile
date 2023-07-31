FROM node:current-alpine AS jsbuild
WORKDIR /js
COPY ./client/web/chart-generator/ .
RUN npm ci
RUN npm run build

FROM node:current-alpine AS cssbuild
WORKDIR /css
COPY ./client/web/css/ .
RUN npm ci
RUN npm run build

FROM eclipse-temurin:20-jdk AS serverbuild
WORKDIR /server
COPY ./server/ .
COPY --from=jsbuild /js/out/prilla.js ./src/main/resources/static/js/prilla.js
COPY --from=cssbuild /css/out/prilla.css ./src/main/resources/static/css/prilla.css
RUN ./mvnw -q package

FROM eclipse-temurin:20-jre
WORKDIR /app
COPY --from=serverbuild /server/target/server-0.1.1.jar .
CMD [ "java", "-jar", "server-0.1.1.jar" ]