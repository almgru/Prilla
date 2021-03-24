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
   ```
   
   Where `<DATASOURCE URL>` is the datasource URL to the database, for
   example `jdbc:h2:file:data`.