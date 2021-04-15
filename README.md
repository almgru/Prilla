# snustrack

Self-hosted web app for recording and visualizing your consumption of Snus + Android app for effortless recording.

`snustrack` aims to help anyone who wishes to reduce their consumption of Snus, or just wants to gain insight into how much they consume. It provides an Android app where you simply press a button when you start snusing and press it again when you stop, and the consumption data is automatically sent to a server which you control.
When you want to get some insight into how much snus you've used and for how long, you just log in to the web app where you can see visualizations of consumption and duration.

## Screenshots

![Screenshot of amount chart](./screenshots/screenshot-1.JPG)

![Screenshot of duration chart](./screenshots/screenshot-2.JPG)

## Setup

Coming soon. See [Development Setup](#development-setup) for running locally.

## Development Setup

1. Create `server/src/main/resources/application.properties` with the following content:

   ```
   spring.datasource.url = jdbc:h2:file:./data/data
   spring.datasource.username = <DATABASE USERNAME>
   spring.datasource.password = <DATABASE PASSWORD>
   spring.datasource.driverClassName = org.h2.Driver
   spring.jpa.database-platform = org.hibernate.dialect.H2Dialect
   spring.jpa.hibernate.ddl-auto = update
   spring.security.user.name = <DEFAULT USER USERNAME>
   spring.security.user.password = <DEFAULT USER PASSWORD>
   spring.security.user.roles = manager
   server.servlet.session.timeout=30d
   server.servlet.session.cookie.max-age=30d
   snustrack.js.uri = http://localhost:8081/snustrack.js
   snustrack.css.uri = http://localhost:8082/snustrack.css
   ```

   - `<DATABASE USERNAME>` -- username to use for the database.
   - `<DATABASE PASSWORD>` -- password to use for the database.
   - `<DEFAULT USER USERNAME>` -- username for accessing the website.
   - `<DEFAULT USER PASSWORD>` -- password for accessing the website.

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
