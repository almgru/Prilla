# Prilla

Self-hosted web app for recording and visualizing your consumption of Snus + Android app for effortless recording.

`prilla` aims to help anyone who wishes to reduce their consumption of Snus, or just wants to gain insight into how much they consume. It provides an Android app where you simply press a button when you start snusing and press it again when you stop, and the consumption data is automatically sent to a server which you control.
When you want to get some insight into how much snus you've used and for how long, you just log in to the web app where you can see visualizations of consumption and duration.

## Screenshots

Coming soon.

## Setup

1. Copy `config/application.properties.template` to `config/application.properties` and update values as needed.
2. `docker-compose up -d`

## Development Setup

1. Copy `config/application.properties.template` to `server/src/main/resources/application.properties` and update values as needed.
2. Run `docker-compose -f docker-compose.dev.yml up -d` from the repo root directory.
3. Wait a minute for everything to build, then access the website at http://localhost:8080 using the username and password specified in `application.properties`.

## Project Structure

The project is divided into four components:

- server, located under `server/`, written in Java 16 with Spring Boot
- chart generator, located under `client/chart-generator/`, written in Javascript
- styling for the client side, located under `client/css`, written in SCSS
- an Android app for recording snus consumption on the go, located under `client/android/`, written in Kotlin.

## Troubleshooting

### Does not work behind a reverse proxy or gives Mixed Content Warning

Add the following to application.properties

```
server.forward-headers-strategy = FRAMEWORK
```

And make sure your reverse proxy forwards the following headers:

- Host
- X-Forwarded-Proto
- X-Real-IP
- X-Forwarded-For

With nginx this can be done with:

```
server {
    ...

    location / {
        proxy_set_header        Host              $host;
        proxy_set_header        X-Forwarded-Proto $scheme;
        proxy_set_header        X-Real-IP         $remote_addr;
        proxy_set_header        X-Forwarded-For   $proxy_add_x_forwarded_for;

        proxy_pass              http://<upstream target>;
    }
```
