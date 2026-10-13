---
phase: 07
gate_status: passed
build_command: "mvn compile (feeds-service, maven:3.9-eclipse-temurin-21)"
test_command: "mvn test (feeds-service, final tree post code-review fixes)"
last_updated: 2026-10-13T03:13:09Z
tests_disabled_during_fixes: none
shadowed_sources: 0
review_blockers_open: 0
boot_smoke: pass
waves:
  - wave: 1
    build: pass
    tests: pass
    fix_attempts: 0
  - wave: 2
    build: pass
    tests: pass
    fix_attempts: 0
  - wave: 3
    build: pass
    tests: pass
    fix_attempts: 0
---

## Wave 1

- Build: `mvn -q compile -DskipTests (feeds-service, maven:3.9-eclipse-temurin-21)` → pass
- Tests: `mvn -q test (feeds-service, compose Postgres)` → pass
- Fix attempts: 0/3

### Gate output

```
[gate] wave 1 build+test in maven container (feeds-service)
02:32:16.312 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.feeds.ApplicationContextBootTest]: ApplicationContextBootTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
02:32:16.340 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.ApplicationContextBootTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T02:32:16.483Z  INFO 141 --- [feeds-service] [           main] c.b.feeds.ApplicationContextBootTest     : Starting ApplicationContextBootTest using Java 21.0.12.1 with PID 141 (started by root in /work)
2026-10-13T02:32:16.485Z  INFO 141 --- [feeds-service] [           main] c.b.feeds.ApplicationContextBootTest     : The following 1 profile is active: "test"
2026-10-13T02:32:16.911Z  INFO 141 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T02:32:16.928Z  INFO 141 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 14 ms. Found 2 JPA repository interfaces.
2026-10-13T02:32:17.291Z  INFO 141 --- [feeds-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-13T02:32:17.299Z  INFO 141 --- [feeds-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-13T02:32:17.299Z  INFO 141 --- [feeds-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-13T02:32:17.320Z  INFO 141 --- [feeds-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-13T02:32:17.321Z  INFO 141 --- [feeds-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 828 ms
2026-10-13T02:32:17.559Z  INFO 141 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-13T02:32:17.655Z  INFO 141 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@49a06087
2026-10-13T02:32:17.656Z  INFO 141 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-13T02:32:17.670Z  INFO 141 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T02:32:17.695Z  INFO 141 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.017s)
2026-10-13T02:32:17.710Z  INFO 141 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-13T02:32:17.712Z  INFO 141 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T02:32:17.772Z  INFO 141 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T02:32:17.800Z  INFO 141 --- [feeds-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-13T02:32:17.811Z  INFO 141 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T02:32:17.900Z  INFO 141 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T02:32:17.916Z  WARN 141 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T02:32:18.168Z  INFO 141 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T02:32:18.180Z  INFO 141 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T02:32:18.300Z  WARN 141 --- [feeds-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-13T02:32:18.864Z  INFO 141 --- [feeds-service] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-10-13T02:32:18.981Z  INFO 141 --- [feeds-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-13T02:32:19.024Z  INFO 141 --- [feeds-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 35101 (http) with context path '/'
2026-10-13T02:32:19.048Z  INFO 141 --- [feeds-service] [           main] c.b.feeds.ApplicationContextBootTest     : Started ApplicationContextBootTest in 2.667 seconds (process running for 3.054)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
2026-10-13T02:32:19.359Z  INFO 141 --- [feeds-service] [o-auto-1-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-13T02:32:19.359Z  INFO 141 --- [feeds-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-13T02:32:19.360Z  INFO 141 --- [feeds-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2026-10-13T02:32:19.420Z  INFO 141 --- [feeds-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.feeds.repository.FeedBookingSchemaTest]: FeedBookingSchemaTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-13T02:32:19.431Z  INFO 141 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.repository.FeedBookingSchemaTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T02:32:19.448Z  INFO 141 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : Starting FeedBookingSchemaTest using Java 21.0.12.1 with PID 141 (started by root in /work)
2026-10-13T02:32:19.448Z  INFO 141 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : The following 1 profile is active: "test"
2026-10-13T02:32:19.506Z  INFO 141 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T02:32:19.508Z  INFO 141 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2 ms. Found 2 JPA repository interfaces.
2026-10-13T02:32:19.531Z  INFO 141 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-13T02:32:19.536Z  INFO 141 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@4c669afb
2026-10-13T02:32:19.537Z  INFO 141 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-13T02:32:19.537Z  INFO 141 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T02:32:19.546Z  INFO 141 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.007s)
2026-10-13T02:32:19.553Z  INFO 141 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-13T02:32:19.553Z  INFO 141 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T02:32:19.586Z  INFO 141 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T02:32:19.587Z  INFO 141 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T02:32:19.590Z  INFO 141 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T02:32:19.592Z  WARN 141 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T02:32:19.613Z  INFO 141 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T02:32:19.619Z  INFO 141 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T02:32:19.640Z  INFO 141 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : Started FeedBookingSchemaTest in 0.207 seconds (process running for 3.647)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: update feed_bookings fb1_0 set location_name=? where fb1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.status=? and fb1_0.start_time>=? order by fb1_0.start_time
Hibernate: select pe1_0.idempotency_key,pe1_0.processed_at from processed_events pe1_0 where pe1_0.idempotency_key=?
Hibernate: insert into processed_events (idempotency_key) values (?)
Hibernate: select pe1_0.idempotency_key from processed_events pe1_0 where pe1_0.idempotency_key=? fetch first ? rows only
Hibernate: select pe1_0.idempotency_key from processed_events pe1_0 where pe1_0.idempotency_key=? fetch first ? rows only
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.status=? and fb1_0.location_id=? and fb1_0.start_time>=? order by fb1_0.start_time
```

## Wave 2

- Build: `mvn compile (feeds-service, maven:3.9-eclipse-temurin-21)` → pass
- Tests: `mvn test (feeds-service combined, compose Postgres+RabbitMQ)` → pass
- Fix attempts: 0/3 — wave 2 combined 07-02+07-03: 31 tests green, 0 skipped

### Gate output

```
[gate] wave 2 full build+test (feeds-service combined)
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< com.bookinghub:feeds-service >--------------------
[INFO] Building feeds-service 0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ feeds-service ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] Copying 3 resources from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ feeds-service ---
[INFO] Recompiling the module because of added or removed source files.
[INFO] Compiling 21 source files with javac [debug parameters release 21] to target/classes
[INFO] Annotation processing is enabled because one or more processors were found
  on the class path. A future release of javac may disable annotation processing
  unless at least one processor is specified by name (-processor), or a search
  path is specified (--processor-path, --processor-module-path), or annotation
  processing is enabled explicitly (-proc:only, -proc:full).
  Use -Xlint:-options to suppress this message.
  Use -proc:none to disable annotation processing.
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ feeds-service ---
[INFO] Copying 1 resource from src/test/resources to target/test-classes
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ feeds-service ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 6 source files with javac [debug parameters release 21] to target/test-classes
[INFO] Annotation processing is enabled because one or more processors were found
  on the class path. A future release of javac may disable annotation processing
  unless at least one processor is specified by name (-processor), or a search
  path is specified (--processor-path, --processor-module-path), or annotation
  processing is enabled explicitly (-proc:only, -proc:full).
  Use -Xlint:-options to suppress this message.
  Use -proc:none to disable annotation processing.
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ feeds-service ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.bookinghub.feeds.controller.FeedControllerIntegrationTest
03:01:36.640 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.feeds.controller.FeedControllerIntegrationTest]: FeedControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
03:01:36.677 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.controller.FeedControllerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T03:01:36.827Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedControllerIntegrationTest    : Starting FeedControllerIntegrationTest using Java 21.0.12.1 with PID 147 (started by root in /work)
2026-10-13T03:01:36.827Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedControllerIntegrationTest    : The following 1 profile is active: "test"
2026-10-13T03:01:37.356Z  INFO 147 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T03:01:37.400Z  INFO 147 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 41 ms. Found 3 JPA repository interfaces.
2026-10-13T03:01:37.953Z  INFO 147 --- [feeds-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-13T03:01:37.961Z  INFO 147 --- [feeds-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-13T03:01:37.961Z  INFO 147 --- [feeds-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-13T03:01:38.000Z  INFO 147 --- [feeds-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-13T03:01:38.000Z  INFO 147 --- [feeds-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1166 ms
2026-10-13T03:01:38.322Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-13T03:01:38.417Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@6ffbf0ac
2026-10-13T03:01:38.418Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-13T03:01:38.430Z  INFO 147 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T03:01:38.452Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 3 migrations (execution time 00:00.014s)
2026-10-13T03:01:38.487Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 3
2026-10-13T03:01:38.488Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T03:01:38.561Z  INFO 147 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T03:01:38.592Z  INFO 147 --- [feeds-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-13T03:01:38.603Z  INFO 147 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T03:01:38.715Z  INFO 147 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T03:01:38.731Z  WARN 147 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T03:01:38.976Z  INFO 147 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T03:01:38.986Z  INFO 147 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T03:01:39.112Z  INFO 147 --- [feeds-service] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-10-13T03:01:39.451Z  WARN 147 --- [feeds-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-13T03:01:40.058Z  INFO 147 --- [feeds-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-13T03:01:40.099Z  INFO 147 --- [feeds-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 38491 (http) with context path '/'
2026-10-13T03:01:40.101Z  INFO 147 --- [feeds-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [rabbitmq:5672]
2026-10-13T03:01:40.118Z  INFO 147 --- [feeds-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#5a2e4e77:0/SimpleConnection@11f4d2b8 [delegate=amqp://guest@172.20.1.2:5672/, localPort=39002]
2026-10-13T03:01:40.141Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedControllerIntegrationTest    : Started FeedControllerIntegrationTest in 3.426 seconds (process running for 3.832)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
2026-10-13T03:01:40.468Z  INFO 147 --- [feeds-service] [o-auto-1-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-13T03:01:40.468Z  INFO 147 --- [feeds-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-13T03:01:40.468Z  INFO 147 --- [feeds-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.093 s -- in com.bookinghub.feeds.controller.FeedControllerIntegrationTest
[INFO] Running com.bookinghub.feeds.feed.FeedRendererTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.bookinghub.feeds.feed.FeedRendererTest
[INFO] Running com.bookinghub.feeds.ApplicationContextBootTest
2026-10-13T03:01:40.676Z  INFO 147 --- [feeds-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.feeds.ApplicationContextBootTest]: ApplicationContextBootTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-13T03:01:40.686Z  INFO 147 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.ApplicationContextBootTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.033 s -- in com.bookinghub.feeds.ApplicationContextBootTest
[INFO] Running com.bookinghub.feeds.consumer.FeedProjectionServiceTest
2026-10-13T03:01:40.747Z  INFO 147 --- [feeds-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.feeds.consumer.FeedProjectionServiceTest]: FeedProjectionServiceTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-13T03:01:40.756Z  INFO 147 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.consumer.FeedProjectionServiceTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T03:01:40.768Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedProjectionServiceTest        : Starting FeedProjectionServiceTest using Java 21.0.12.1 with PID 147 (started by root in /work)
2026-10-13T03:01:40.768Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedProjectionServiceTest        : The following 1 profile is active: "test"
2026-10-13T03:01:40.804Z  INFO 147 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T03:01:40.806Z  INFO 147 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2 ms. Found 3 JPA repository interfaces.
2026-10-13T03:01:40.824Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-13T03:01:40.829Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@627b0e71
2026-10-13T03:01:40.830Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-13T03:01:40.830Z  INFO 147 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T03:01:40.833Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 3 migrations (execution time 00:00.001s)
2026-10-13T03:01:40.850Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 3
2026-10-13T03:01:40.851Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T03:01:40.856Z  INFO 147 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T03:01:40.857Z  INFO 147 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T03:01:40.860Z  INFO 147 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T03:01:40.861Z  WARN 147 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T03:01:40.904Z  INFO 147 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T03:01:40.910Z  INFO 147 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T03:01:40.954Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedProjectionServiceTest        : Started FeedProjectionServiceTest in 0.197 seconds (process running for 4.644)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: update feed_bookings fb1_0 set location_name=? where fb1_0.location_id=?
Hibernate: insert into location_names (name,location_id) values (?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: update feed_bookings set end_time=?,location_id=?,location_name=?,start_time=?,status=?,title=? where booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: delete from feed_bookings where booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: update feed_bookings fb1_0 set location_name=? where fb1_0.location_id=?
Hibernate: insert into location_names (name,location_id) values (?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: delete from feed_bookings where booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.277 s -- in com.bookinghub.feeds.consumer.FeedProjectionServiceTest
[INFO] Running com.bookinghub.feeds.consumer.FeedEventConsumerIntegrationTest
2026-10-13T03:01:40.989Z  INFO 147 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.consumer.FeedEventConsumerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T03:01:40.998Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedEventConsumerIntegrationTest : Starting FeedEventConsumerIntegrationTest using Java 21.0.12.1 with PID 147 (started by root in /work)
2026-10-13T03:01:40.998Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedEventConsumerIntegrationTest : The following 1 profile is active: "test"
2026-10-13T03:01:41.059Z  INFO 147 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T03:01:41.062Z  INFO 147 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2 ms. Found 3 JPA repository interfaces.
2026-10-13T03:01:41.114Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Starting...
2026-10-13T03:01:41.118Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-3 - Added connection org.postgresql.jdbc.PgConnection@1532b1d
2026-10-13T03:01:41.118Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Start completed.
2026-10-13T03:01:41.118Z  INFO 147 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T03:01:41.121Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 3 migrations (execution time 00:00.001s)
2026-10-13T03:01:41.126Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 3
2026-10-13T03:01:41.126Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T03:01:41.135Z  INFO 147 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T03:01:41.136Z  INFO 147 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T03:01:41.138Z  INFO 147 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T03:01:41.140Z  WARN 147 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T03:01:41.161Z  INFO 147 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T03:01:41.165Z  INFO 147 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T03:01:41.230Z  INFO 147 --- [feeds-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [rabbitmq:5672]
2026-10-13T03:01:41.237Z  INFO 147 --- [feeds-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#15b8487f:0/SimpleConnection@950a833 [delegate=amqp://guest@172.20.1.2:5672/, localPort=39004]
2026-10-13T03:01:41.261Z  WARN 147 --- [feeds-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-13T03:01:41.341Z  INFO 147 --- [feeds-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-13T03:01:41.352Z  INFO 147 --- [feeds-service] [           main] c.b.f.c.FeedEventConsumerIntegrationTest : Started FeedEventConsumerIntegrationTest in 0.361 seconds (process running for 5.043)
2026-10-13T03:01:45.678Z  WARN 147 --- [feeds-service] [ntContainer#0-1] o.s.a.r.r.RejectAndDontRequeueRecoverer  : Retries exhausted for message (Body:'[B@29e795e9(byte[225])' MessageProperties [headers={}, contentType=application/json, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=booking.events, receivedRoutingKey=booking.created, deliveryTag=3, consumerTag=amq.ctag-ecgf2kc9SsQz6d0DOGdjUQ, consumerQueue=feeds.booking.q])

org.springframework.amqp.rabbit.support.ListenerExecutionFailedException: Listener method 'public void com.bookinghub.feeds.consumer.FeedEventConsumer.onBookingEvent(org.springframework.amqp.core.Message,java.lang.String,java.lang.String)' threw exception
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:287) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandlerAndProcessResult(MessagingMessageListenerAdapter.java:225) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.onMessage(MessagingMessageListenerAdapter.java:149) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doInvokeListener(AbstractMessageListenerContainer.java:1682) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.actualInvokeListener(AbstractMessageListenerContainer.java:1604) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:355) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor$1.doWithRetry(RetryOperationsInterceptor.java:114) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:344) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:233) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor.invoke(RetryOperationsInterceptor.java:135) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:223) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.$Proxy180.invokeListener(Unknown Source) ~[na:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.invokeListener(AbstractMessageListenerContainer.java:1592) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doExecuteListener(AbstractMessageListenerContainer.java:1583) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListenerAndHandleException(AbstractMessageListenerContainer.java:1528) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.lambda$executeListener$8(AbstractMessageListenerContainer.java:1506) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at io.micrometer.observation.Observation.observe(Observation.java:499) ~[micrometer-observation-1.13.4.jar:1.13.4]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListener(AbstractMessageListenerContainer.java:1506) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.doReceiveAndExecute(SimpleMessageListenerContainer.java:1082) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.receiveAndExecute(SimpleMessageListenerContainer.java:1018) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.mainLoop(SimpleMessageListenerContainer.java:1421) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.run(SimpleMessageListenerContainer.java:1322) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: com.bookinghub.feeds.rabbit.MissingIdempotencyKeyException: event missing idempotency_key header, routing_key=booking.created
	at com.bookinghub.feeds.consumer.FeedEventConsumer.requireKey(FeedEventConsumer.java:84) ~[classes/:na]
	at com.bookinghub.feeds.consumer.FeedEventConsumer.onBookingEvent(FeedEventConsumer.java:66) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:169) ~[spring-messaging-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.adapter.KotlinAwareInvocableHandlerMethod.doInvoke(KotlinAwareInvocableHandlerMethod.java:45) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:119) ~[spring-messaging-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter.invoke(HandlerAdapter.java:75) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:278) ~[spring-rabbit-3.1.7.jar:3.1.7]
	... 27 common frames omitted

2026-10-13T03:01:45.680Z  WARN 147 --- [feeds-service] [ntContainer#0-1] s.a.r.l.ConditionalRejectingErrorHandler : Execution of Rabbit message listener failed.

org.springframework.amqp.rabbit.support.ListenerExecutionFailedException: Retry Policy Exhausted
	at org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer.recover(RejectAndDontRequeueRecoverer.java:76) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptorFactoryBean.recover(StatelessRetryOperationsInterceptorFactoryBean.java:78) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor$ItemRecovererCallback.recover(RetryOperationsInterceptor.java:150) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.handleRetryExhausted(RetryTemplate.java:560) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:405) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:233) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor.invoke(RetryOperationsInterceptor.java:135) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:223) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.$Proxy180.invokeListener(Unknown Source) ~[na:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.invokeListener(AbstractMessageListenerContainer.java:1592) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doExecuteListener(AbstractMessageListenerContainer.java:1583) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListenerAndHandleException(AbstractMessageListenerContainer.java:1528) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.lambda$executeListener$8(AbstractMessageListenerContainer.java:1506) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at io.micrometer.observation.Observation.observe(Observation.java:499) ~[micrometer-observation-1.13.4.jar:1.13.4]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListener(AbstractMessageListenerContainer.java:1506) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.doReceiveAndExecute(SimpleMessageListenerContainer.java:1082) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.receiveAndExecute(SimpleMessageListenerContainer.java:1018) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.mainLoop(SimpleMessageListenerContainer.java:1421) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.run(SimpleMessageListenerContainer.java:1322) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: org.springframework.amqp.AmqpRejectAndDontRequeueException: null
	... 21 common frames omitted
Caused by: org.springframework.amqp.rabbit.support.ListenerExecutionFailedException: Listener method 'public void com.bookinghub.feeds.consumer.FeedEventConsumer.onBookingEvent(org.springframework.amqp.core.Message,java.lang.String,java.lang.String)' threw exception
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:287) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandlerAndProcessResult(MessagingMessageListenerAdapter.java:225) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.onMessage(MessagingMessageListenerAdapter.java:149) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doInvokeListener(AbstractMessageListenerContainer.java:1682) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.actualInvokeListener(AbstractMessageListenerContainer.java:1604) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:355) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor$1.doWithRetry(RetryOperationsInterceptor.java:114) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:344) ~[spring-retry-2.0.9.jar:na]
	... 16 common frames omitted
Caused by: com.bookinghub.feeds.rabbit.MissingIdempotencyKeyException: event missing idempotency_key header, routing_key=booking.created
	at com.bookinghub.feeds.consumer.FeedEventConsumer.requireKey(FeedEventConsumer.java:84) ~[classes/:na]
	at com.bookinghub.feeds.consumer.FeedEventConsumer.onBookingEvent(FeedEventConsumer.java:66) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:169) ~[spring-messaging-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.adapter.KotlinAwareInvocableHandlerMethod.doInvoke(KotlinAwareInvocableHandlerMethod.java:45) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:119) ~[spring-messaging-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter.invoke(HandlerAdapter.java:75) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:278) ~[spring-rabbit-3.1.7.jar:3.1.7]
	... 27 common frames omitted

[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.405 s -- in com.bookinghub.feeds.consumer.FeedEventConsumerIntegrationTest
[INFO] Running com.bookinghub.feeds.repository.FeedBookingSchemaTest
2026-10-13T03:01:46.394Z  INFO 147 --- [feeds-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.feeds.repository.FeedBookingSchemaTest]: FeedBookingSchemaTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-13T03:01:46.397Z  INFO 147 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.repository.FeedBookingSchemaTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T03:01:46.404Z  INFO 147 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : Starting FeedBookingSchemaTest using Java 21.0.12.1 with PID 147 (started by root in /work)
2026-10-13T03:01:46.404Z  INFO 147 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : The following 1 profile is active: "test"
2026-10-13T03:01:46.419Z  INFO 147 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T03:01:46.422Z  INFO 147 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 1 ms. Found 3 JPA repository interfaces.
2026-10-13T03:01:46.434Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Starting...
2026-10-13T03:01:46.438Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-4 - Added connection org.postgresql.jdbc.PgConnection@6378d7a1
2026-10-13T03:01:46.438Z  INFO 147 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Start completed.
2026-10-13T03:01:46.439Z  INFO 147 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T03:01:46.441Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 3 migrations (execution time 00:00.001s)
2026-10-13T03:01:46.445Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 3
2026-10-13T03:01:46.445Z  INFO 147 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T03:01:46.449Z  INFO 147 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T03:01:46.449Z  INFO 147 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T03:01:46.452Z  INFO 147 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T03:01:46.453Z  WARN 147 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T03:01:46.470Z  INFO 147 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T03:01:46.473Z  INFO 147 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T03:01:46.486Z  INFO 147 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : Started FeedBookingSchemaTest in 0.088 seconds (process running for 10.177)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: update feed_bookings fb1_0 set location_name=? where fb1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.status=? and fb1_0.start_time>=? order by fb1_0.start_time
Hibernate: select pe1_0.idempotency_key,pe1_0.processed_at from processed_events pe1_0 where pe1_0.idempotency_key=?
Hibernate: insert into processed_events (idempotency_key) values (?)
Hibernate: select pe1_0.idempotency_key from processed_events pe1_0 where pe1_0.idempotency_key=? fetch first ? rows only
Hibernate: select pe1_0.idempotency_key from processed_events pe1_0 where pe1_0.idempotency_key=? fetch first ? rows only
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.status=? and fb1_0.location_id=? and fb1_0.start_time>=? order by fb1_0.start_time
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.112 s -- in com.bookinghub.feeds.repository.FeedBookingSchemaTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  14.099 s
[INFO] Finished at: 2026-10-13T03:01:47Z
[INFO] ------------------------------------------------------------------------
```

## Wave 3

- Build: `mvn compile (feeds-service, maven:3.9-eclipse-temurin-21)` → pass
- Tests: `mvn test (feeds-service, final tree post code-review fixes)` → pass
- Fix attempts: 0/3 — Phase-level regression gate: 32 tests green on final tree including code-review fixer commits ec0ec1e/a17ec1a

### Gate output

```
[final gate] feeds-service full build+test on final tree
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< com.bookinghub:feeds-service >--------------------
[INFO] Building feeds-service 0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ feeds-service ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] Copying 3 resources from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ feeds-service ---
[INFO] Nothing to compile - all classes are up to date.
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ feeds-service ---
[INFO] Copying 1 resource from src/test/resources to target/test-classes
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ feeds-service ---
[INFO] Nothing to compile - all classes are up to date.
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ feeds-service ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.bookinghub.feeds.controller.FeedControllerIntegrationTest
03:10:38.805 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.feeds.controller.FeedControllerIntegrationTest]: FeedControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
03:10:38.842 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.controller.FeedControllerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T03:10:38.986Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedControllerIntegrationTest    : Starting FeedControllerIntegrationTest using Java 21.0.12.1 with PID 146 (started by root in /work)
2026-10-13T03:10:38.987Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedControllerIntegrationTest    : The following 1 profile is active: "test"
2026-10-13T03:10:39.389Z  INFO 146 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T03:10:39.407Z  INFO 146 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 15 ms. Found 3 JPA repository interfaces.
2026-10-13T03:10:39.850Z  INFO 146 --- [feeds-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-13T03:10:39.857Z  INFO 146 --- [feeds-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-13T03:10:39.857Z  INFO 146 --- [feeds-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-13T03:10:39.891Z  INFO 146 --- [feeds-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-13T03:10:39.891Z  INFO 146 --- [feeds-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 897 ms
2026-10-13T03:10:40.203Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-13T03:10:40.292Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@2739ecc0
2026-10-13T03:10:40.293Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-13T03:10:40.305Z  INFO 146 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T03:10:40.322Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 3 migrations (execution time 00:00.010s)
2026-10-13T03:10:40.332Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 3
2026-10-13T03:10:40.333Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T03:10:40.406Z  INFO 146 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T03:10:40.423Z  INFO 146 --- [feeds-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-13T03:10:40.432Z  INFO 146 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T03:10:40.508Z  INFO 146 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T03:10:40.522Z  WARN 146 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T03:10:40.771Z  INFO 146 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T03:10:40.782Z  INFO 146 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T03:10:40.952Z  INFO 146 --- [feeds-service] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-10-13T03:10:41.297Z  WARN 146 --- [feeds-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-13T03:10:41.705Z  INFO 146 --- [feeds-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-13T03:10:41.746Z  INFO 146 --- [feeds-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 46733 (http) with context path '/'
2026-10-13T03:10:41.748Z  INFO 146 --- [feeds-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [rabbitmq:5672]
2026-10-13T03:10:41.763Z  INFO 146 --- [feeds-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#482652f7:0/SimpleConnection@1f5faa89 [delegate=amqp://guest@172.20.1.2:5672/, localPort=38650]
2026-10-13T03:10:41.785Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedControllerIntegrationTest    : Started FeedControllerIntegrationTest in 2.901 seconds (process running for 3.275)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
2026-10-13T03:10:42.117Z  INFO 146 --- [feeds-service] [o-auto-1-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-13T03:10:42.117Z  INFO 146 --- [feeds-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-13T03:10:42.118Z  INFO 146 --- [feeds-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.649 s -- in com.bookinghub.feeds.controller.FeedControllerIntegrationTest
[INFO] Running com.bookinghub.feeds.feed.FeedRendererTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.bookinghub.feeds.feed.FeedRendererTest
[INFO] Running com.bookinghub.feeds.ApplicationContextBootTest
2026-10-13T03:10:42.407Z  INFO 146 --- [feeds-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.feeds.ApplicationContextBootTest]: ApplicationContextBootTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-13T03:10:42.409Z  INFO 146 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.ApplicationContextBootTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.085 s -- in com.bookinghub.feeds.ApplicationContextBootTest
[INFO] Running com.bookinghub.feeds.consumer.FeedProjectionServiceTest
2026-10-13T03:10:42.495Z  INFO 146 --- [feeds-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.feeds.consumer.FeedProjectionServiceTest]: FeedProjectionServiceTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-13T03:10:42.505Z  INFO 146 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.consumer.FeedProjectionServiceTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T03:10:42.518Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedProjectionServiceTest        : Starting FeedProjectionServiceTest using Java 21.0.12.1 with PID 146 (started by root in /work)
2026-10-13T03:10:42.518Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedProjectionServiceTest        : The following 1 profile is active: "test"
2026-10-13T03:10:42.647Z  INFO 146 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T03:10:42.650Z  INFO 146 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 3 ms. Found 3 JPA repository interfaces.
2026-10-13T03:10:42.705Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-13T03:10:42.711Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@7190b5ce
2026-10-13T03:10:42.711Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-13T03:10:42.747Z  INFO 146 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T03:10:42.751Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 3 migrations (execution time 00:00.002s)
2026-10-13T03:10:42.761Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 3
2026-10-13T03:10:42.761Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T03:10:42.790Z  INFO 146 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T03:10:42.791Z  INFO 146 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T03:10:42.794Z  INFO 146 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T03:10:42.795Z  WARN 146 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T03:10:42.887Z  INFO 146 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T03:10:42.892Z  INFO 146 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T03:10:42.911Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedProjectionServiceTest        : Started FeedProjectionServiceTest in 0.404 seconds (process running for 4.4)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: update feed_bookings fb1_0 set location_name=? where fb1_0.location_id=?
Hibernate: insert into location_names (name,location_id) values (?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: update feed_bookings set end_time=?,location_id=?,location_name=?,start_time=?,status=?,title=? where booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: delete from feed_bookings where booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: update feed_bookings fb1_0 set location_name=? where fb1_0.location_id=?
Hibernate: insert into location_names (name,location_id) values (?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: update feed_bookings fb1_0 set location_name=? where fb1_0.location_id=?
Hibernate: insert into location_names (name,location_id) values (?,?)
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: delete from feed_bookings where booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select ln1_0.location_id,ln1_0.name,ln1_0.updated_at from location_names ln1_0 where ln1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.454 s -- in com.bookinghub.feeds.consumer.FeedProjectionServiceTest
[INFO] Running com.bookinghub.feeds.consumer.FeedEventConsumerIntegrationTest
2026-10-13T03:10:42.949Z  INFO 146 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.consumer.FeedEventConsumerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T03:10:42.959Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedEventConsumerIntegrationTest : Starting FeedEventConsumerIntegrationTest using Java 21.0.12.1 with PID 146 (started by root in /work)
2026-10-13T03:10:42.959Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedEventConsumerIntegrationTest : The following 1 profile is active: "test"
2026-10-13T03:10:43.011Z  INFO 146 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T03:10:43.013Z  INFO 146 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2 ms. Found 3 JPA repository interfaces.
2026-10-13T03:10:43.066Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Starting...
2026-10-13T03:10:43.070Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-3 - Added connection org.postgresql.jdbc.PgConnection@56c6256a
2026-10-13T03:10:43.070Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Start completed.
2026-10-13T03:10:43.071Z  INFO 146 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T03:10:43.074Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 3 migrations (execution time 00:00.001s)
2026-10-13T03:10:43.079Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 3
2026-10-13T03:10:43.079Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T03:10:43.089Z  INFO 146 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T03:10:43.090Z  INFO 146 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T03:10:43.092Z  INFO 146 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T03:10:43.094Z  WARN 146 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T03:10:43.117Z  INFO 146 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T03:10:43.121Z  INFO 146 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T03:10:43.194Z  INFO 146 --- [feeds-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [rabbitmq:5672]
2026-10-13T03:10:43.195Z  INFO 146 --- [feeds-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#610f3f1b:0/SimpleConnection@6b42829 [delegate=amqp://guest@172.20.1.2:5672/, localPort=38658]
2026-10-13T03:10:43.218Z  WARN 146 --- [feeds-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-13T03:10:43.362Z  INFO 146 --- [feeds-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-13T03:10:43.373Z  INFO 146 --- [feeds-service] [           main] c.b.f.c.FeedEventConsumerIntegrationTest : Started FeedEventConsumerIntegrationTest in 0.421 seconds (process running for 4.863)
2026-10-13T03:10:47.700Z  WARN 146 --- [feeds-service] [ntContainer#0-1] o.s.a.r.r.RejectAndDontRequeueRecoverer  : Retries exhausted for message (Body:'[B@5b0f21a(byte[225])' MessageProperties [headers={}, contentType=application/json, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=booking.events, receivedRoutingKey=booking.created, deliveryTag=3, consumerTag=amq.ctag-hxOjuHT-zRxWPyEkJELMow, consumerQueue=feeds.booking.q])

org.springframework.amqp.rabbit.support.ListenerExecutionFailedException: Listener method 'public void com.bookinghub.feeds.consumer.FeedEventConsumer.onBookingEvent(org.springframework.amqp.core.Message,java.lang.String,java.lang.String)' threw exception
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:287) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandlerAndProcessResult(MessagingMessageListenerAdapter.java:225) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.onMessage(MessagingMessageListenerAdapter.java:149) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doInvokeListener(AbstractMessageListenerContainer.java:1682) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.actualInvokeListener(AbstractMessageListenerContainer.java:1604) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:355) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor$1.doWithRetry(RetryOperationsInterceptor.java:114) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:344) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:233) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor.invoke(RetryOperationsInterceptor.java:135) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:223) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.$Proxy180.invokeListener(Unknown Source) ~[na:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.invokeListener(AbstractMessageListenerContainer.java:1592) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doExecuteListener(AbstractMessageListenerContainer.java:1583) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListenerAndHandleException(AbstractMessageListenerContainer.java:1528) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.lambda$executeListener$8(AbstractMessageListenerContainer.java:1506) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at io.micrometer.observation.Observation.observe(Observation.java:499) ~[micrometer-observation-1.13.4.jar:1.13.4]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListener(AbstractMessageListenerContainer.java:1506) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.doReceiveAndExecute(SimpleMessageListenerContainer.java:1082) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.receiveAndExecute(SimpleMessageListenerContainer.java:1018) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.mainLoop(SimpleMessageListenerContainer.java:1421) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.run(SimpleMessageListenerContainer.java:1322) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: com.bookinghub.feeds.rabbit.MissingIdempotencyKeyException: event missing idempotency_key header, routing_key=booking.created
	at com.bookinghub.feeds.consumer.FeedEventConsumer.requireKey(FeedEventConsumer.java:84) ~[classes/:na]
	at com.bookinghub.feeds.consumer.FeedEventConsumer.onBookingEvent(FeedEventConsumer.java:66) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:169) ~[spring-messaging-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.adapter.KotlinAwareInvocableHandlerMethod.doInvoke(KotlinAwareInvocableHandlerMethod.java:45) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:119) ~[spring-messaging-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter.invoke(HandlerAdapter.java:75) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:278) ~[spring-rabbit-3.1.7.jar:3.1.7]
	... 27 common frames omitted

2026-10-13T03:10:47.701Z  WARN 146 --- [feeds-service] [ntContainer#0-1] s.a.r.l.ConditionalRejectingErrorHandler : Execution of Rabbit message listener failed.

org.springframework.amqp.rabbit.support.ListenerExecutionFailedException: Retry Policy Exhausted
	at org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer.recover(RejectAndDontRequeueRecoverer.java:76) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptorFactoryBean.recover(StatelessRetryOperationsInterceptorFactoryBean.java:78) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor$ItemRecovererCallback.recover(RetryOperationsInterceptor.java:150) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.handleRetryExhausted(RetryTemplate.java:560) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:405) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:233) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor.invoke(RetryOperationsInterceptor.java:135) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:223) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.$Proxy180.invokeListener(Unknown Source) ~[na:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.invokeListener(AbstractMessageListenerContainer.java:1592) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doExecuteListener(AbstractMessageListenerContainer.java:1583) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListenerAndHandleException(AbstractMessageListenerContainer.java:1528) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.lambda$executeListener$8(AbstractMessageListenerContainer.java:1506) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at io.micrometer.observation.Observation.observe(Observation.java:499) ~[micrometer-observation-1.13.4.jar:1.13.4]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListener(AbstractMessageListenerContainer.java:1506) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.doReceiveAndExecute(SimpleMessageListenerContainer.java:1082) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.receiveAndExecute(SimpleMessageListenerContainer.java:1018) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.mainLoop(SimpleMessageListenerContainer.java:1421) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.run(SimpleMessageListenerContainer.java:1322) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: org.springframework.amqp.AmqpRejectAndDontRequeueException: null
	... 21 common frames omitted
Caused by: org.springframework.amqp.rabbit.support.ListenerExecutionFailedException: Listener method 'public void com.bookinghub.feeds.consumer.FeedEventConsumer.onBookingEvent(org.springframework.amqp.core.Message,java.lang.String,java.lang.String)' threw exception
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:287) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandlerAndProcessResult(MessagingMessageListenerAdapter.java:225) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.onMessage(MessagingMessageListenerAdapter.java:149) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doInvokeListener(AbstractMessageListenerContainer.java:1682) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.actualInvokeListener(AbstractMessageListenerContainer.java:1604) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:355) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.13.jar:6.1.13]
	at org.springframework.retry.interceptor.RetryOperationsInterceptor$1.doWithRetry(RetryOperationsInterceptor.java:114) ~[spring-retry-2.0.9.jar:na]
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:344) ~[spring-retry-2.0.9.jar:na]
	... 16 common frames omitted
Caused by: com.bookinghub.feeds.rabbit.MissingIdempotencyKeyException: event missing idempotency_key header, routing_key=booking.created
	at com.bookinghub.feeds.consumer.FeedEventConsumer.requireKey(FeedEventConsumer.java:84) ~[classes/:na]
	at com.bookinghub.feeds.consumer.FeedEventConsumer.onBookingEvent(FeedEventConsumer.java:66) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:169) ~[spring-messaging-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.adapter.KotlinAwareInvocableHandlerMethod.doInvoke(KotlinAwareInvocableHandlerMethod.java:45) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:119) ~[spring-messaging-6.1.13.jar:6.1.13]
	at org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter.invoke(HandlerAdapter.java:75) ~[spring-rabbit-3.1.7.jar:3.1.7]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:278) ~[spring-rabbit-3.1.7.jar:3.1.7]
	... 27 common frames omitted

[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.467 s -- in com.bookinghub.feeds.consumer.FeedEventConsumerIntegrationTest
[INFO] Running com.bookinghub.feeds.repository.FeedBookingSchemaTest
2026-10-13T03:10:48.415Z  INFO 146 --- [feeds-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.feeds.repository.FeedBookingSchemaTest]: FeedBookingSchemaTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-13T03:10:48.418Z  INFO 146 --- [feeds-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.feeds.FeedsServiceApplication for test class com.bookinghub.feeds.repository.FeedBookingSchemaTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-13T03:10:48.425Z  INFO 146 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : Starting FeedBookingSchemaTest using Java 21.0.12.1 with PID 146 (started by root in /work)
2026-10-13T03:10:48.425Z  INFO 146 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : The following 1 profile is active: "test"
2026-10-13T03:10:48.439Z  INFO 146 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-13T03:10:48.441Z  INFO 146 --- [feeds-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 1 ms. Found 3 JPA repository interfaces.
2026-10-13T03:10:48.453Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Starting...
2026-10-13T03:10:48.457Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-4 - Added connection org.postgresql.jdbc.PgConnection@4c4bff52
2026-10-13T03:10:48.457Z  INFO 146 --- [feeds-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Start completed.
2026-10-13T03:10:48.458Z  INFO 146 --- [feeds-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/feeds_db_test (PostgreSQL 16.15)
2026-10-13T03:10:48.460Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 3 migrations (execution time 00:00.001s)
2026-10-13T03:10:48.464Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 3
2026-10-13T03:10:48.464Z  INFO 146 --- [feeds-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-13T03:10:48.467Z  INFO 146 --- [feeds-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-13T03:10:48.468Z  INFO 146 --- [feeds-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-13T03:10:48.470Z  INFO 146 --- [feeds-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-13T03:10:48.471Z  WARN 146 --- [feeds-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-13T03:10:48.491Z  INFO 146 --- [feeds-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-13T03:10:48.495Z  INFO 146 --- [feeds-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-13T03:10:48.507Z  INFO 146 --- [feeds-service] [           main] c.b.f.repository.FeedBookingSchemaTest   : Started FeedBookingSchemaTest in 0.088 seconds (process running for 9.996)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: update feed_bookings fb1_0 set location_name=? where fb1_0.location_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.status=? and fb1_0.start_time>=? order by fb1_0.start_time
Hibernate: select pe1_0.idempotency_key,pe1_0.processed_at from processed_events pe1_0 where pe1_0.idempotency_key=?
Hibernate: insert into processed_events (idempotency_key) values (?)
Hibernate: select pe1_0.idempotency_key from processed_events pe1_0 where pe1_0.idempotency_key=? fetch first ? rows only
Hibernate: select pe1_0.idempotency_key from processed_events pe1_0 where pe1_0.idempotency_key=? fetch first ? rows only
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.booking_id=?
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: insert into feed_bookings (end_time,location_id,location_name,start_time,status,title,booking_id) values (?,?,?,?,?,?,?)
Hibernate: select fb1_0.booking_id,fb1_0.end_time,fb1_0.location_id,fb1_0.location_name,fb1_0.start_time,fb1_0.status,fb1_0.title,fb1_0.updated_at from feed_bookings fb1_0 where fb1_0.status=? and fb1_0.location_id=? and fb1_0.start_time>=? order by fb1_0.start_time
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.113 s -- in com.bookinghub.feeds.repository.FeedBookingSchemaTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.595 s
[INFO] Finished at: 2026-10-13T03:10:49Z
[INFO] ------------------------------------------------------------------------
```


## Backend pre-push gate

- Status: skipped
- Result marker + failing output tail:
```
__GATE__ build_exit=-1 test_exit=-1 build_cmd=[none] test_cmd=[none] head=6e8030b957fc614abe4ffe06613a41abcbdb11e7 test_files=80 skip_marks=2 shadow_files=0
```
