# Trabacco

Self-hosted tracker for snus.

## Setup

1. Create `application.properties` with the following content:

   ```
   spring.datasource.url = <DATASOURCE URL>
   spring.datasource.username = <DATABASE USERNAME>
   spring.datasource.password = <DATABASE PASSWORD>
   spring.datasource.driverClassName = org.h2.Driver
   spring.jpa.database-platform = org.hibernate.dialect.H2Dialect
   spring.jpa.hibernate.ddl-auto = update
   spring.security.user.name = <DEFAULT USER USERNAME>
   spring.security.user.password = <DEFAULT USER PASSWORD>
   spring.security.user.roles = manager
   ```

   - `<DATASOURCE URL>` is the datasource URL to the database, for
   example `jdbc:h2:file:data` for a H2 database.
   - `<DATABASE USERNAME>` is the username of the database user that has
     access to the database specified with `<DATASOURCE URL>`.
   - `<DATABASE PASSWORD>` is the password for the database user.
   - `<DEFAULT USER USERNAME>` is the username for accessing the website.
   - `<DEFAULT USER PASSWORD>` is the password for accessing the website.