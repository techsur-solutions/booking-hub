---
phase: 03
gate_status: failed
build_command: "(cd services/users-permissions-service && mvn -q compile -DskipTests)"
test_command: "(cd services/users-permissions-service && mvn -q test)"
last_updated: 2026-10-08T15:38:55Z
tests_disabled_during_fixes: none
shadowed_sources: 0
waves:
  - wave: 1
    build: pass
    tests: pass
    fix_attempts: 1
  - wave: 2
    build: pass
    tests: pass
    fix_attempts: 0
  - wave: 3
    build: pass
    tests: fail
    fix_attempts: 3
  - wave: 4
    build: pass
    tests: fail
    fix_attempts: 0
---

## Wave 1

- Build: `(cd services/users-permissions-service && mvn -q compile -DskipTests)` → pass
- Tests: `(cd services/users-permissions-service && mvn -q test)` → pass
- Fix attempts: 1/3 — Fix 1: Testcontainers Docker API incompatibility (switch to test profile DB) + email uniqueness test collision (unique timestamp-based email) → 64d7e54

### Gate output

```
14:55:34.895 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.userspermissions.ApplicationContextBootTest]: ApplicationContextBootTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
14:55:34.930 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.ApplicationContextBootTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T14:55:35.126Z  INFO 51637 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : Starting ApplicationContextBootTest using Java 21.0.12.1 with PID 51637 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T14:55:35.127Z  INFO 51637 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : The following 1 profile is active: "test"
2026-10-08T14:55:35.822Z  INFO 51637 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T14:55:35.923Z  INFO 51637 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 97 ms. Found 4 JPA repository interfaces.
2026-10-08T14:55:36.534Z  INFO 51637 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T14:55:36.539Z  INFO 51637 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T14:55:36.540Z  INFO 51637 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T14:55:36.575Z  INFO 51637 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-08T14:55:36.575Z  INFO 51637 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1443 ms
2026-10-08T14:55:36.731Z  INFO 51637 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-08T14:55:36.808Z  INFO 51637 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@526893f
2026-10-08T14:55:36.809Z  INFO 51637 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-08T14:55:36.820Z  INFO 51637 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T14:55:36.834Z  INFO 51637 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.008s)
2026-10-08T14:55:36.869Z  INFO 51637 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T14:55:36.870Z  INFO 51637 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T14:55:36.903Z  INFO 51637 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T14:55:36.920Z  INFO 51637 --- [users-permissions-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-08T14:55:36.929Z  INFO 51637 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T14:55:37.035Z  INFO 51637 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T14:55:37.441Z  INFO 51637 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T14:55:37.468Z  INFO 51637 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T14:55:37.938Z  INFO 51637 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T14:55:38.066Z  INFO 51637 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 34655 (http) with context path '/'
2026-10-08T14:55:38.075Z  INFO 51637 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : Started ApplicationContextBootTest in 3.087 seconds (process running for 3.552)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
2026-10-08T14:55:38.448Z  INFO 51637 --- [users-permissions-service] [o-auto-1-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-08T14:55:38.448Z  INFO 51637 --- [users-permissions-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-08T14:55:38.449Z  INFO 51637 --- [users-permissions-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2026-10-08T14:55:38.531Z  INFO 51637 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.repository.PermissionSeedDataTest]: PermissionSeedDataTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T14:55:38.538Z  INFO 51637 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.repository.PermissionSeedDataTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T14:55:38.575Z  INFO 51637 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : Starting PermissionSeedDataTest using Java 21.0.12.1 with PID 51637 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T14:55:38.575Z  INFO 51637 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : The following 1 profile is active: "test"
2026-10-08T14:55:38.629Z  INFO 51637 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T14:55:38.632Z  INFO 51637 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2 ms. Found 4 JPA repository interfaces.
2026-10-08T14:55:38.721Z  INFO 51637 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-08T14:55:38.726Z  INFO 51637 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@763b279
2026-10-08T14:55:38.726Z  INFO 51637 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-08T14:55:38.727Z  INFO 51637 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T14:55:38.730Z  INFO 51637 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.001s)
2026-10-08T14:55:38.736Z  INFO 51637 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T14:55:38.736Z  INFO 51637 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T14:55:38.742Z  INFO 51637 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T14:55:38.743Z  INFO 51637 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T14:55:38.745Z  INFO 51637 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T14:55:38.784Z  INFO 51637 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T14:55:38.790Z  INFO 51637 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T14:55:38.827Z  INFO 51637 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : Started PermissionSeedDataTest in 0.287 seconds (process running for 4.305)
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0
2026-10-08T14:55:38.931Z  INFO 51637 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.repository.OutboxAndResetTokenRepositoryTest]: OutboxAndResetTokenRepositoryTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T14:55:38.934Z  INFO 51637 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.repository.OutboxAndResetTokenRepositoryTest
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into outbox (aggregate_id,aggregate_type,attempt_count,created_at,exchange,idempotency_key,payload,published_at,routing_key,status,id) values (?,?,?,?,?,?,?,?,?,?,?)
Hibernate: select oe1_0.id,oe1_0.aggregate_id,oe1_0.aggregate_type,oe1_0.attempt_count,oe1_0.created_at,oe1_0.exchange,oe1_0.idempotency_key,oe1_0.payload,oe1_0.published_at,oe1_0.routing_key,oe1_0.status from outbox oe1_0 where oe1_0.status=? order by oe1_0.created_at fetch first ? rows only
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into password_reset_tokens (created_at,expires_at,token_hash,used_at,user_id,id) values (?,?,?,?,?,?)
Hibernate: select prt1_0.id,prt1_0.created_at,prt1_0.expires_at,prt1_0.token_hash,prt1_0.used_at,prt1_0.user_id from password_reset_tokens prt1_0 where prt1_0.token_hash=?
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where upper(u1_0.email)=upper(?)
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
2026-10-08T14:55:38.980Z  WARN 51637 --- [users-permissions-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-10-08T14:55:38.980Z ERROR 51637 --- [users-permissions-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uq_users_email_lower"
  Detail: Key (lower(email::text))=(case-test-1791471338976@example.com) already exists.
```

## Wave 2

- Build: `(cd services/users-permissions-service && mvn -q compile -DskipTests)` → pass
- Tests: `(cd services/users-permissions-service && mvn -q test)` → pass
- Fix attempts: 0/3

### Gate output

```
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
15:10:28.669 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.userspermissions.ApplicationContextBootTest]: ApplicationContextBootTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
15:10:28.727 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.ApplicationContextBootTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:10:28.977Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : Starting ApplicationContextBootTest using Java 21.0.12.1 with PID 101119 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:10:28.978Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : The following 1 profile is active: "test"
2026-10-08T15:10:29.621Z  INFO 101119 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:10:29.644Z  INFO 101119 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 20 ms. Found 4 JPA repository interfaces.
2026-10-08T15:10:30.198Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:10:30.223Z  INFO 101119 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:10:30.223Z  INFO 101119 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:10:30.244Z  INFO 101119 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-08T15:10:30.245Z  INFO 101119 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1225 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:10:30.575Z  INFO 101119 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-08T15:10:30.684Z  INFO 101119 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@35ff072c
2026-10-08T15:10:30.685Z  INFO 101119 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-08T15:10:30.695Z  INFO 101119 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T15:10:30.730Z  INFO 101119 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.008s)
2026-10-08T15:10:30.769Z  INFO 101119 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:10:30.770Z  INFO 101119 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:10:30.836Z  INFO 101119 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:10:30.872Z  INFO 101119 --- [users-permissions-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-08T15:10:30.883Z  INFO 101119 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:10:31.043Z  INFO 101119 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:10:31.587Z  INFO 101119 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:10:31.621Z  INFO 101119 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:10:32.240Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:10:32.866Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 40625 (http) with context path '/'
2026-10-08T15:10:32.873Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : Started ApplicationContextBootTest in 4.09 seconds (process running for 5.151)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
2026-10-08T15:10:33.392Z  INFO 101119 --- [users-permissions-service] [o-auto-1-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-08T15:10:33.392Z  INFO 101119 --- [users-permissions-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-08T15:10:33.393Z  INFO 101119 --- [users-permissions-service] [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
2026-10-08T15:10:35.069Z  INFO 101119 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.repository.PermissionSeedDataTest]: PermissionSeedDataTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:10:35.078Z  INFO 101119 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.repository.PermissionSeedDataTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:10:35.124Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : Starting PermissionSeedDataTest using Java 21.0.12.1 with PID 101119 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:10:35.124Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : The following 1 profile is active: "test"
2026-10-08T15:10:35.167Z  INFO 101119 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:10:35.172Z  INFO 101119 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 4 ms. Found 4 JPA repository interfaces.
2026-10-08T15:10:35.234Z  INFO 101119 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-08T15:10:35.239Z  INFO 101119 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@68535137
2026-10-08T15:10:35.239Z  INFO 101119 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-08T15:10:35.240Z  INFO 101119 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T15:10:35.267Z  INFO 101119 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.025s)
2026-10-08T15:10:35.273Z  INFO 101119 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:10:35.273Z  INFO 101119 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:10:35.281Z  INFO 101119 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:10:35.282Z  INFO 101119 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:10:35.285Z  INFO 101119 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:10:35.336Z  INFO 101119 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:10:35.365Z  INFO 101119 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:10:35.384Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : Started PermissionSeedDataTest in 0.304 seconds (process running for 7.662)
2026-10-08T15:10:35.465Z  WARN 101119 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:10:35.466Z  WARN 101119 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:10:35.466Z  WARN 101119 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:10:35.466Z  WARN 101119 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:10:35.466Z  WARN 101119 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0
2026-10-08T15:10:35.570Z  INFO 101119 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.repository.OutboxAndResetTokenRepositoryTest]: OutboxAndResetTokenRepositoryTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:10:35.573Z  INFO 101119 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.repository.OutboxAndResetTokenRepositoryTest
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into outbox (aggregate_id,aggregate_type,attempt_count,created_at,exchange,idempotency_key,payload,published_at,routing_key,status,id) values (?,?,?,?,?,?,?,?,?,?,?)
Hibernate: select oe1_0.id,oe1_0.aggregate_id,oe1_0.aggregate_type,oe1_0.attempt_count,oe1_0.created_at,oe1_0.exchange,oe1_0.idempotency_key,oe1_0.payload,oe1_0.published_at,oe1_0.routing_key,oe1_0.status from outbox oe1_0 where oe1_0.status=? order by oe1_0.created_at fetch first ? rows only
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into password_reset_tokens (created_at,expires_at,token_hash,used_at,user_id,id) values (?,?,?,?,?,?)
Hibernate: select prt1_0.id,prt1_0.created_at,prt1_0.expires_at,prt1_0.token_hash,prt1_0.used_at,prt1_0.user_id from password_reset_tokens prt1_0 where prt1_0.token_hash=?
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where upper(u1_0.email)=upper(?)
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
2026-10-08T15:10:35.677Z  WARN 101119 --- [users-permissions-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-10-08T15:10:35.678Z ERROR 101119 --- [users-permissions-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uq_users_email_lower"
  Detail: Key (lower(email::text))=(case-test-1791472235673@example.com) already exists.
2026-10-08T15:10:35.683Z  INFO 101119 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:10:35.685Z  INFO 101119 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest
2026-10-08T15:10:35.688Z  INFO 101119 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:10:35.719Z  INFO 101119 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:10:35.737Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 101119 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:10:35.737Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.security.Tier2FailClosedTest       : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:10:35.871Z  WARN 101119 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:10:35.871Z  WARN 101119 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:10:36.044Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:10:36.045Z  INFO 101119 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:10:36.045Z  INFO 101119 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:10:36.071Z  INFO 101119 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:10:36.071Z  INFO 101119 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 333 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:10:36.219Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:10:36.321Z  INFO 101119 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-08T15:10:36.321Z  INFO 101119 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-08T15:10:36.321Z  INFO 101119 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 0 ms
2026-10-08T15:10:36.329Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 35937 (http) with context path '/'
2026-10-08T15:10:36.330Z  INFO 101119 --- [users-permissions-service] [           main] c.b.u.security.Tier2FailClosedTest       : Started Tier2FailClosedTest in 0.602 seconds (process running for 8.608)

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:10:36.340Z  INFO 101119 --- [users-permissions-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 101119 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:10:36.340Z  INFO 101119 --- [users-permissions-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:10:36.478Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:10:36.478Z  INFO 101119 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:10:36.478Z  INFO 101119 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:10:36.521Z  INFO 101119 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:10:36.521Z  INFO 101119 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 181 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:10:36.691Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:10:36.779Z  INFO 101119 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-08T15:10:36.779Z  INFO 101119 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-08T15:10:36.779Z  INFO 101119 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 0 ms
2026-10-08T15:10:36.785Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 43363 (http) with context path '/'
2026-10-08T15:10:36.787Z  INFO 101119 --- [users-permissions-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 0.455 seconds (process running for 9.065)
2026-10-08T15:10:36.867Z  INFO 101119 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:10:36.869Z  INFO 101119 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:10:36.884Z  INFO 101119 --- [users-permissions-service] [           main] u.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 101119 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:10:36.884Z  INFO 101119 --- [users-permissions-service] [           main] u.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:10:37.019Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:10:37.019Z  INFO 101119 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:10:37.020Z  INFO 101119 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:10:37.027Z  INFO 101119 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-3].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:10:37.027Z  INFO 101119 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 142 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:10:37.144Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:10:37.225Z  INFO 101119 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-3].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-08T15:10:37.225Z  INFO 101119 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-08T15:10:37.225Z  INFO 101119 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 0 ms
2026-10-08T15:10:37.231Z  INFO 101119 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 34019 (http) with context path '/'
2026-10-08T15:10:37.233Z  INFO 101119 --- [users-permissions-service] [           main] u.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 0.356 seconds (process running for 9.511)
2026-10-08T15:10:37.243Z  INFO 101119 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:10:37.244Z  INFO 101119 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification
```

## Wave 3

- Build: `(cd services/users-permissions-service && mvn -q compile -DskipTests)` → pass
- Tests: `(cd services/users-permissions-service && mvn -q test)` → fail
- Fix attempts: 3/3 — Fix 1: PostgreSQL connection pool exhaustion (reduced HikariCP to 2 max) → 4db34cb; Fix 2: JPA-dependency injection breakage (Tier2FailClosedTest + UserControllerIntegrationTest) → bea63b5; Still failing: Tier2FailClosedTest context loading fails with @MockBean controllers, needs service layer mocks or full JPA context

### Unresolved failure

```
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
15:24:52.881 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.userspermissions.controller.AuthControllerIntegrationTest]: AuthControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
15:24:52.968 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.controller.AuthControllerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:24:53.383Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.c.AuthControllerIntegrationTest    : Starting AuthControllerIntegrationTest using Java 21.0.12.1 with PID 157505 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:24:53.384Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.c.AuthControllerIntegrationTest    : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:24:54.278Z  INFO 157505 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:24:54.335Z  INFO 157505 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 53 ms. Found 4 JPA repository interfaces.
2026-10-08T15:24:55.124Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-08T15:24:55.468Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@586737ff
2026-10-08T15:24:55.469Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-08T15:24:55.565Z  INFO 157505 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_db (PostgreSQL 16.15)
2026-10-08T15:24:55.671Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.095s)
2026-10-08T15:24:55.721Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:24:55.723Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:24:56.072Z  INFO 157505 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:24:56.171Z  INFO 157505 --- [users-permissions-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-08T15:24:56.221Z  INFO 157505 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:24:56.522Z  INFO 157505 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:24:56.998Z  INFO 157505 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:24:57.011Z  INFO 157505 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:24:58.328Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:24:58.967Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.c.AuthControllerIntegrationTest    : Started AuthControllerIntegrationTest in 5.884 seconds (process running for 6.602)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
2026-10-08T15:24:59.322Z  INFO 157505 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.controller.UserControllerIntegrationTest]: UserControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:24:59.325Z  INFO 157505 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.controller.UserControllerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:24:59.552Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.c.UserControllerIntegrationTest    : Starting UserControllerIntegrationTest using Java 21.0.12.1 with PID 157505 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:24:59.552Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.c.UserControllerIntegrationTest    : The following 1 profile is active: "test"
2026-10-08T15:24:59.748Z  INFO 157505 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:24:59.753Z  INFO 157505 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 3 ms. Found 4 JPA repository interfaces.
2026-10-08T15:24:59.877Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:24:59.882Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:24:59.882Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:24:59.898Z  INFO 157505 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-08T15:24:59.898Z  INFO 157505 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 345 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:24:59.980Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-08T15:24:59.984Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@7c53d3c
2026-10-08T15:24:59.984Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-08T15:24:59.985Z  INFO 157505 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T15:24:59.988Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.001s)
2026-10-08T15:24:59.992Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:24:59.992Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:25:00.024Z  INFO 157505 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:25:00.025Z  INFO 157505 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:25:00.028Z  INFO 157505 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:25:00.079Z  INFO 157505 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:25:00.085Z  INFO 157505 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:25:00.255Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:25:00.373Z  INFO 157505 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-08T15:25:00.373Z  INFO 157505 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-08T15:25:00.374Z  INFO 157505 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-08T15:25:00.425Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 39085 (http) with context path '/'
2026-10-08T15:25:00.428Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.c.UserControllerIntegrationTest    : Started UserControllerIntegrationTest in 0.891 seconds (process running for 8.063)

MockHttpServletRequest:
      HTTP Method = PUT
      Request URI = /users/839974ba-96cf-4d7f-ab2e-fc63e213c448/roles
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"55"]
             Body = {
    "roles": ["role_user", "role_booking_creator"]
}

    Session Attrs = {}

Handler:
             Type = com.bookinghub.userspermissions.controller.UserController
           Method = com.bookinghub.userspermissions.controller.UserController#assignRoles(String, UserRolesUpdateRequest)

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 403
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"0", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"error_code":"AUTH_FORBIDDEN","message":"Insufficient permissions for this resource","timestamp":"2026-10-08T15:25:00.632962435Z","path":"/users/839974ba-96cf-4d7f-ab2e-fc63e213c448/roles"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /users
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"67"]
             Body = {
    "email": "Joe@example.com",
    "initialRole": "role_user"
}

    Session Attrs = {}

Handler:
             Type = com.bookinghub.userspermissions.controller.UserController
           Method = com.bookinghub.userspermissions.controller.UserController#createUser(UserCreateRequest)

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 403
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"0", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"error_code":"AUTH_FORBIDDEN","message":"Insufficient permissions for this resource","timestamp":"2026-10-08T15:25:00.647162573Z","path":"/users"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /users
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"67"]
             Body = {
    "email": "joe@example.com",
    "initialRole": "role_user"
}

    Session Attrs = {}

Handler:
             Type = com.bookinghub.userspermissions.controller.UserController
           Method = com.bookinghub.userspermissions.controller.UserController#createUser(UserCreateRequest)

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 403
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"0", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"error_code":"AUTH_FORBIDDEN","message":"Insufficient permissions for this resource","timestamp":"2026-10-08T15:25:00.667264112Z","path":"/users"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /users
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"67"]
             Body = {
    "email": "joe@example.com",
    "initialRole": "role_user"
}

    Session Attrs = {}

Handler:
             Type = com.bookinghub.userspermissions.controller.UserController
           Method = com.bookinghub.userspermissions.controller.UserController#createUser(UserCreateRequest)

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 403
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"0", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"error_code":"AUTH_FORBIDDEN","message":"Insufficient permissions for this resource","timestamp":"2026-10-08T15:25:00.675826674Z","path":"/users"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m8[m, [1;31mFailures: [0;1;31m4[m, Errors: 0, Skipped: 0, Time elapsed: 1.371 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.controller.[1mUserControllerIntegrationTest[m
[[1;31mERROR[m] com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.adminAssignsRoles_updatesKeycloakAndCreatesOutboxEvent -- Time elapsed: 0.042 s <<< FAILURE!
java.lang.AssertionError: Status expected:<200> but was:<403>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
	at com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.adminAssignsRoles_updatesKeycloakAndCreatesOutboxEvent(UserControllerIntegrationTest.java:309)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[[1;31mERROR[m] com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.createUserWithCaseVariantEmail_returns409 -- Time elapsed: 0.010 s <<< FAILURE!
java.lang.AssertionError: Status expected:<409> but was:<403>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
	at com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.createUserWithCaseVariantEmail_returns409(UserControllerIntegrationTest.java:181)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[[1;31mERROR[m] com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.createUserWithDuplicateEmail_returns409_keycloakNotCalled -- Time elapsed: 0.020 s <<< FAILURE!
java.lang.AssertionError: Status expected:<409> but was:<403>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
	at com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.createUserWithDuplicateEmail_returns409_keycloakNotCalled(UserControllerIntegrationTest.java:151)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[[1;31mERROR[m] com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.adminCreatesUser_provisionKeycloakAndLocalRowAndOutboxEvent -- Time elapsed: 0.008 s <<< FAILURE!
java.lang.AssertionError: Status expected:<201> but was:<403>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
	at com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.adminCreatesUser_provisionKeycloakAndLocalRowAndOutboxEvent(UserControllerIntegrationTest.java:113)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

2026-10-08T15:25:00.694Z  INFO 157505 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.ApplicationContextBootTest]: ApplicationContextBootTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:25:00.697Z  INFO 157505 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.ApplicationContextBootTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:25:00.727Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : Starting ApplicationContextBootTest using Java 21.0.12.1 with PID 157505 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:25:00.727Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : The following 1 profile is active: "test"
2026-10-08T15:25:00.802Z  INFO 157505 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:25:00.820Z  INFO 157505 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 16 ms. Found 4 JPA repository interfaces.
2026-10-08T15:25:00.853Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:25:00.854Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:25:00.854Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:25:00.870Z  INFO 157505 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:25:00.870Z  INFO 157505 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 142 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:25:00.926Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Starting...
2026-10-08T15:25:00.930Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-3 - Added connection org.postgresql.jdbc.PgConnection@1b12f58d
2026-10-08T15:25:00.930Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Start completed.
2026-10-08T15:25:00.930Z  INFO 157505 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T15:25:00.933Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.001s)
2026-10-08T15:25:00.937Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:25:00.937Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:25:00.945Z  INFO 157505 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:25:00.946Z  INFO 157505 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:25:00.948Z  INFO 157505 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:25:00.978Z  INFO 157505 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:25:00.983Z  INFO 157505 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:25:01.151Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:25:01.237Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 39141 (http) with context path '/'
2026-10-08T15:25:01.239Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : Started ApplicationContextBootTest in 0.52 seconds (process running for 8.875)
2026-10-08T15:25:01.275Z  INFO 157505 --- [users-permissions-service] [o-auto-2-exec-1] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-08T15:25:01.275Z  INFO 157505 --- [users-permissions-service] [o-auto-2-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-08T15:25:01.276Z  INFO 157505 --- [users-permissions-service] [o-auto-2-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2026-10-08T15:25:02.267Z  INFO 157505 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.repository.PermissionSeedDataTest]: PermissionSeedDataTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:25:02.273Z  INFO 157505 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.repository.PermissionSeedDataTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:25:02.323Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : Starting PermissionSeedDataTest using Java 21.0.12.1 with PID 157505 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:25:02.323Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : The following 1 profile is active: "test"
2026-10-08T15:25:02.377Z  INFO 157505 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:25:02.381Z  INFO 157505 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 3 ms. Found 4 JPA repository interfaces.
2026-10-08T15:25:02.426Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Starting...
2026-10-08T15:25:02.430Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-4 - Added connection org.postgresql.jdbc.PgConnection@18f28a93
2026-10-08T15:25:02.430Z  INFO 157505 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Start completed.
2026-10-08T15:25:02.431Z  INFO 157505 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T15:25:02.434Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.001s)
2026-10-08T15:25:02.438Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:25:02.438Z  INFO 157505 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:25:02.442Z  INFO 157505 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:25:02.443Z  INFO 157505 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:25:02.445Z  INFO 157505 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:25:02.483Z  INFO 157505 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:25:02.488Z  INFO 157505 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:25:02.504Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : Started PermissionSeedDataTest in 0.229 seconds (process running for 10.139)
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0
2026-10-08T15:25:02.541Z  INFO 157505 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.repository.OutboxAndResetTokenRepositoryTest]: OutboxAndResetTokenRepositoryTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:25:02.544Z  INFO 157505 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.repository.OutboxAndResetTokenRepositoryTest
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into outbox (aggregate_id,aggregate_type,attempt_count,created_at,exchange,idempotency_key,payload,published_at,routing_key,status,id) values (?,?,?,?,?,?,?,?,?,?,?)
Hibernate: select oe1_0.id,oe1_0.aggregate_id,oe1_0.aggregate_type,oe1_0.attempt_count,oe1_0.created_at,oe1_0.exchange,oe1_0.idempotency_key,oe1_0.payload,oe1_0.published_at,oe1_0.routing_key,oe1_0.status from outbox oe1_0 where oe1_0.status=? order by oe1_0.created_at fetch first ? rows only
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into password_reset_tokens (created_at,expires_at,token_hash,used_at,user_id,id) values (?,?,?,?,?,?)
Hibernate: select prt1_0.id,prt1_0.created_at,prt1_0.expires_at,prt1_0.token_hash,prt1_0.used_at,prt1_0.user_id from password_reset_tokens prt1_0 where prt1_0.token_hash=?
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where upper(u1_0.email)=upper(?)
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
2026-10-08T15:25:02.579Z  WARN 157505 --- [users-permissions-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-10-08T15:25:02.579Z ERROR 157505 --- [users-permissions-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uq_users_email_lower"
  Detail: Key (lower(email::text))=(case-test-1791473102575@example.com) already exists.
2026-10-08T15:25:02.584Z  INFO 157505 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:25:02.586Z  INFO 157505 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest
2026-10-08T15:25:02.588Z  INFO 157505 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:25:02.589Z  INFO 157505 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:25:02.601Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 157505 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:25:02.602Z  INFO 157505 --- [users-permissions-service] [           main] c.b.u.security.Tier2FailClosedTest       : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:25:03.068Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:25:03.069Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:25:03.069Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:25:03.090Z  INFO 157505 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:25:03.090Z  INFO 157505 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 488 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:25:03.405Z  WARN 157505 --- [users-permissions-service] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
2026-10-08T15:25:03.407Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-10-08T15:25:03.433Z  INFO 157505 --- [users-permissions-service] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-10-08T15:25:03.471Z ERROR 157505 --- [users-permissions-service] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 2 of constructor in com.bookinghub.userspermissions.service.AuthService required a bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' that could not be found.


Action:

Consider defining a bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' in your configuration.




============================
CONDITIONS EVALUATION REPORT
============================


Positive matches:
-----------------

    None


Negative matches:
-----------------

    None


Exclusions:
-----------

    None


Unconditional classes:
----------------------

    None



2026-10-08T15:25:03.474Z  WARN 157505 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest@4ea83953]

java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor.instantiateTestClass(NestedClassTestDescriptor.java:85) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:287) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152) ~[spring-test-6.1.13.jar:6.1.13]
	... 88 common frames omitted
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 113 common frames omitted


  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:25:03.495Z  INFO 157505 --- [users-permissions-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 157505 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:25:03.496Z  INFO 157505 --- [users-permissions-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:25:03.864Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:25:03.865Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:25:03.865Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:25:03.874Z  INFO 157505 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-3].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:25:03.874Z  INFO 157505 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 377 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:25:03.997Z  WARN 157505 --- [users-permissions-service] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
2026-10-08T15:25:03.997Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-10-08T15:25:04.011Z  INFO 157505 --- [users-permissions-service] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-10-08T15:25:04.014Z ERROR 157505 --- [users-permissions-service] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 2 of constructor in com.bookinghub.userspermissions.service.AuthService required a bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' that could not be found.


Action:

Consider defining a bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' in your configuration.




============================
CONDITIONS EVALUATION REPORT
============================


Positive matches:
-----------------

    None


Negative matches:
-----------------

    None


Exclusions:
-----------

    None


Unconditional classes:
----------------------

    None



2026-10-08T15:25:04.014Z  WARN 157505 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.test.context.support.DependencyInjectionTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest@27a71243]

java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@1e4c64e testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.InsufficientRoleTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@2e8edc36 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.test.context.support.DynamicPropertiesContextCustomizer@5f177b9d, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152) ~[spring-test-6.1.13.jar:6.1.13]
	... 84 common frames omitted
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 109 common frames omitted

[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 1.433 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.security.[1mTier2FailClosedTest$InsufficientRoleTest[m
[[1;31mERROR[m] com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest.insufficientRole_returns403ApiError -- Time elapsed: 0.001 s <<< ERROR!
java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180)
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63)
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	Suppressed: java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@1e4c64e testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.InsufficientRoleTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@2e8edc36 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.test.context.support.DynamicPropertiesContextCustomizer@5f177b9d, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180)
		at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98)
		at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
		at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
		at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
		at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
		at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
		at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
		at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
		at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
		... 4 more
	Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
		at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
		at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
		at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
		at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
		at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975)
		at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971)
		at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625)
		at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146)
		at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754)
		at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456)
		at org.springframework.boot.SpringApplication.run(SpringApplication.java:335)
		at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137)
		at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58)
		at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46)
		at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463)
		at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553)
		at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137)
		at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108)
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225)
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152)
		... 19 more
	Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
		at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
		... 44 more
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975)
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971)
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146)
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754)
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335)
	at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137)
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58)
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46)
	at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463)
	at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553)
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137)
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108)
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225)
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152)
	... 19 more
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
	... 44 more

2026-10-08T15:25:04.024Z  INFO 157505 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:25:04.027Z  INFO 157505 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest
2026-10-08T15:25:04.068Z  WARN 157505 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest@62cd7303]

java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor.instantiateTestClass(NestedClassTestDescriptor.java:85) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:287) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]


  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:25:04.086Z  INFO 157505 --- [users-permissions-service] [           main] u.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 157505 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:25:04.086Z  INFO 157505 --- [users-permissions-service] [           main] u.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:25:04.327Z  INFO 157505 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:25:04.328Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:25:04.328Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:25:04.336Z  INFO 157505 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-4].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:25:04.337Z  INFO 157505 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 250 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:25:04.451Z  WARN 157505 --- [users-permissions-service] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
2026-10-08T15:25:04.452Z  INFO 157505 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-10-08T15:25:04.477Z  INFO 157505 --- [users-permissions-service] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-10-08T15:25:04.479Z ERROR 157505 --- [users-permissions-service] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 2 of constructor in com.bookinghub.userspermissions.service.AuthService required a bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' that could not be found.


Action:

Consider defining a bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' in your configuration.




============================
CONDITIONS EVALUATION REPORT
============================


Positive matches:
-----------------

    None


Negative matches:
-----------------

    None


Exclusions:
-----------

    None


Unconditional classes:
----------------------

    None



2026-10-08T15:25:04.479Z  WARN 157505 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.test.context.support.DependencyInjectionTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest@e7a966e]

java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@15295502 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.MissingTokenTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@57adb6ee key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.test.context.support.DynamicPropertiesContextCustomizer@fcd39707, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152) ~[spring-test-6.1.13.jar:6.1.13]
	... 84 common frames omitted
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 109 common frames omitted

[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 0.459 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.security.[1mTier2FailClosedTest$MissingTokenTest[m
[[1;31mERROR[m] com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest.missingToken_returns401 -- Time elapsed: 0.001 s <<< ERROR!
java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145)
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63)
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	Suppressed: java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@15295502 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.MissingTokenTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@57adb6ee key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.test.context.support.DynamicPropertiesContextCustomizer@fcd39707, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180)
		at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98)
		at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
		at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
		at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
		at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
		at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
		at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
		at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
		at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
		... 4 more
	Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'authService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/AuthService.class]: Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
		at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
		at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
		at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
		at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
		at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975)
		at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971)
		at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625)
		at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146)
		at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754)
		at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456)
		at org.springframework.boot.SpringApplication.run(SpringApplication.java:335)
		at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137)
		at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58)
		at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46)
		at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463)
		at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553)
		at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137)
		at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108)
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225)
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152)
		... 19 more
	Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PasswordResetTokenRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
		at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
		... 44 more

2026-10-08T15:25:04.485Z  INFO 157505 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:25:04.486Z  INFO 157505 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification
2026-10-08T15:25:04.488Z  WARN 157505 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest@2f2b638]

java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor.instantiateTestClass(NestedClassTestDescriptor.java:85) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:287) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]

2026-10-08T15:25:04.488Z  WARN 157505 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.test.context.support.DependencyInjectionTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification@3709c652]

java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@640672a4 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.JwksFailClosedVerification, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@1013b36a key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]

[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 0.006 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.security.[1mTier2FailClosedTest$JwksFailClosedVerification[m
[[1;31mERROR[m] com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification.jwksOutageHandlerExists -- Time elapsed: 0 s <<< ERROR!
java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145)
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63)
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	Suppressed: java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@640672a4 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.JwksFailClosedVerification, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@1013b36a key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145)
		at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98)
		at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
		at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
		at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
		at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
		at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
		at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
		at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
		at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
		... 4 more

[[1;31mERROR[m] [1;31mFailures: [m
[[1;31mERROR[m] [1;31m  UserControllerIntegrationTest.adminAssignsRoles_updatesKeycloakAndCreatesOutboxEvent:309 Status expected:<200> but was:<403>[m
[[1;31mERROR[m] [1;31m  UserControllerIntegrationTest.adminCreatesUser_provisionKeycloakAndLocalRowAndOutboxEvent:113 Status expected:<201> but was:<403>[m
[[1;31mERROR[m] [1;31m  UserControllerIntegrationTest.createUserWithCaseVariantEmail_returns409:181 Status expected:<409> but was:<403>[m
[[1;31mERROR[m] [1;31m  UserControllerIntegrationTest.createUserWithDuplicateEmail_returns409_keycloakNotCalled:151 Status expected:<409> but was:<403>[m
[[1;31mERROR[m] [1;31mErrors: [m
[[1;31mERROR[m] [1;31m  Tier2FailClosedTest$InsufficientRoleTest.insufficientRole_returns403ApiError ? IllegalState Failed to load ApplicationContext for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null][m
[[1;31mERROR[m] [1;31m  Tier2FailClosedTest$JwksFailClosedVerification.jwksOutageHandlerExists ? IllegalState ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null][m
[[1;31mERROR[m] [1;31m  Tier2FailClosedTest$MissingTokenTest.missingToken_returns401 ? IllegalState ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@7bad503f testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@75ebd25b key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@27adc16e, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@4066c471, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@682d51be, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@16b2bb0c, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@727eb8cb, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@21129f1f, org.springframework.boot.test.context.SpringBootTestAnnotation@3983215c], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null][m
[[1;31mERROR[m] [1;31mTests run: 35, Failures: 4, Errors: 3, Skipped: 0[m
[[1;31mERROR[m] Failed to execute goal [32morg.apache.maven.plugins:maven-surefire-plugin:3.2.5:test[m [1m(default-test)[m on project [36musers-permissions-service[m: [1;31mThere are test failures.[m
[[1;31mERROR[m] [1;31m[m
[[1;31mERROR[m] [1;31mPlease refer to /home/daytona/project/services/users-permissions-service/target/surefire-reports for the individual test results.[m
[[1;31mERROR[m] [1;31mPlease refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.[m
[[1;31mERROR[m] -> [1m[Help 1][m
[[1;31mERROR[m] 
[[1;31mERROR[m] To see the full stack trace of the errors, re-run Maven with the [1m-e[m switch.
[[1;31mERROR[m] Re-run Maven using the [1m-X[m switch to enable full debug logging.
[[1;31mERROR[m] 
[[1;31mERROR[m] For more information about the errors and possible solutions, please read the following articles:
[[1;31mERROR[m] [1m[Help 1][m http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
```

## Wave 4

- Build: `(cd services/users-permissions-service && mvn -q compile -DskipTests)` → pass
- Tests: `(cd services/users-permissions-service && mvn -q test)` → fail
- Fix attempts: 0/3 — Inherited wave 3 failures: Tier2FailClosedTest context loading. No new failures introduced by wave 4

### Unresolved failure

```
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
15:38:35.734 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.userspermissions.controller.AuthControllerIntegrationTest]: AuthControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
15:38:35.788 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.controller.AuthControllerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:38:36.086Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.c.AuthControllerIntegrationTest    : Starting AuthControllerIntegrationTest using Java 21.0.12.1 with PID 206632 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:38:36.119Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.c.AuthControllerIntegrationTest    : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:38:37.173Z  INFO 206632 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:38:37.267Z  INFO 206632 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 91 ms. Found 4 JPA repository interfaces.
2026-10-08T15:38:38.424Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-08T15:38:38.627Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@54e0f76f
2026-10-08T15:38:38.628Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-08T15:38:38.668Z  INFO 206632 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_db (PostgreSQL 16.15)
2026-10-08T15:38:38.685Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.010s)
2026-10-08T15:38:38.724Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:38:38.725Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:38:38.867Z  INFO 206632 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:38:38.885Z  INFO 206632 --- [users-permissions-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-08T15:38:38.929Z  INFO 206632 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:38:39.126Z  INFO 206632 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:38:39.604Z  INFO 206632 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:38:39.617Z  INFO 206632 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:38:41.091Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:38:41.680Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.c.AuthControllerIntegrationTest    : Started AuthControllerIntegrationTest in 5.809 seconds (process running for 6.411)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
2026-10-08T15:38:42.126Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.controller.UserControllerIntegrationTest]: UserControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:42.130Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.controller.UserControllerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:38:42.356Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.c.UserControllerIntegrationTest    : Starting UserControllerIntegrationTest using Java 21.0.12.1 with PID 206632 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:38:42.356Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.c.UserControllerIntegrationTest    : The following 1 profile is active: "test"
2026-10-08T15:38:42.534Z  INFO 206632 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:38:42.538Z  INFO 206632 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 3 ms. Found 4 JPA repository interfaces.
2026-10-08T15:38:42.684Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:38:42.721Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:38:42.721Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:38:42.771Z  INFO 206632 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-08T15:38:42.771Z  INFO 206632 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 414 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:38:42.922Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-08T15:38:42.927Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@10f868dd
2026-10-08T15:38:42.927Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-08T15:38:42.928Z  INFO 206632 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T15:38:42.931Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.001s)
2026-10-08T15:38:42.936Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:38:42.936Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:38:42.946Z  INFO 206632 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:38:42.947Z  INFO 206632 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:38:42.950Z  INFO 206632 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:38:43.031Z  INFO 206632 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:38:43.065Z  INFO 206632 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:38:43.569Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:38:43.925Z  INFO 206632 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-08T15:38:43.925Z  INFO 206632 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-08T15:38:43.926Z  INFO 206632 --- [users-permissions-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-08T15:38:44.024Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 34525 (http) with context path '/'
2026-10-08T15:38:44.029Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.c.UserControllerIntegrationTest    : Started UserControllerIntegrationTest in 1.687 seconds (process running for 8.76)

MockHttpServletRequest:
      HTTP Method = PUT
      Request URI = /users/d6b1334f-5bf2-429d-aa86-d2b38871a7bc/roles
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"55"]
             Body = {
    "roles": ["role_user", "role_booking_creator"]
}

    Session Attrs = {}

Handler:
             Type = com.bookinghub.userspermissions.controller.UserController
           Method = com.bookinghub.userspermissions.controller.UserController#assignRoles(String, UserRolesUpdateRequest)

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 403
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"0", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"error_code":"AUTH_FORBIDDEN","message":"Insufficient permissions for this resource","timestamp":"2026-10-08T15:38:44.439089464Z","path":"/users/d6b1334f-5bf2-429d-aa86-d2b38871a7bc/roles"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /users
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"67"]
             Body = {
    "email": "Joe@example.com",
    "initialRole": "role_user"
}

    Session Attrs = {}

Handler:
             Type = com.bookinghub.userspermissions.controller.UserController
           Method = com.bookinghub.userspermissions.controller.UserController#createUser(UserCreateRequest)

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 403
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"0", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"error_code":"AUTH_FORBIDDEN","message":"Insufficient permissions for this resource","timestamp":"2026-10-08T15:38:44.450546078Z","path":"/users"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /users
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"67"]
             Body = {
    "email": "joe@example.com",
    "initialRole": "role_user"
}

    Session Attrs = {}

Handler:
             Type = com.bookinghub.userspermissions.controller.UserController
           Method = com.bookinghub.userspermissions.controller.UserController#createUser(UserCreateRequest)

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 403
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"0", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"error_code":"AUTH_FORBIDDEN","message":"Insufficient permissions for this resource","timestamp":"2026-10-08T15:38:44.456924328Z","path":"/users"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /users
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"67"]
             Body = {
    "email": "joe@example.com",
    "initialRole": "role_user"
}

    Session Attrs = {}

Handler:
             Type = com.bookinghub.userspermissions.controller.UserController
           Method = com.bookinghub.userspermissions.controller.UserController#createUser(UserCreateRequest)

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 403
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"0", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"error_code":"AUTH_FORBIDDEN","message":"Insufficient permissions for this resource","timestamp":"2026-10-08T15:38:44.465160037Z","path":"/users"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m8[m, [1;31mFailures: [0;1;31m4[m, Errors: 0, Skipped: 0, Time elapsed: 2.357 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.controller.[1mUserControllerIntegrationTest[m
[[1;31mERROR[m] com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.adminAssignsRoles_updatesKeycloakAndCreatesOutboxEvent -- Time elapsed: 0.063 s <<< FAILURE!
java.lang.AssertionError: Status expected:<200> but was:<403>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
	at com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.adminAssignsRoles_updatesKeycloakAndCreatesOutboxEvent(UserControllerIntegrationTest.java:309)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[[1;31mERROR[m] com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.createUserWithCaseVariantEmail_returns409 -- Time elapsed: 0.008 s <<< FAILURE!
java.lang.AssertionError: Status expected:<409> but was:<403>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
	at com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.createUserWithCaseVariantEmail_returns409(UserControllerIntegrationTest.java:181)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[[1;31mERROR[m] com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.createUserWithDuplicateEmail_returns409_keycloakNotCalled -- Time elapsed: 0.006 s <<< FAILURE!
java.lang.AssertionError: Status expected:<409> but was:<403>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
	at com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.createUserWithDuplicateEmail_returns409_keycloakNotCalled(UserControllerIntegrationTest.java:151)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[[1;31mERROR[m] com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.adminCreatesUser_provisionKeycloakAndLocalRowAndOutboxEvent -- Time elapsed: 0.008 s <<< FAILURE!
java.lang.AssertionError: Status expected:<201> but was:<403>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
	at com.bookinghub.userspermissions.controller.UserControllerIntegrationTest.adminCreatesUser_provisionKeycloakAndLocalRowAndOutboxEvent(UserControllerIntegrationTest.java:113)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

2026-10-08T15:38:44.523Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.controller.PermissionControllerIntegrationTest]: PermissionControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:44.524Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.controller.PermissionControllerIntegrationTest
2026-10-08T15:38:44.534Z  INFO 206632 --- [users-permissions-service] [           main] org.testcontainers.images.PullPolicy     : Image pull policy will be performed by: DefaultPullPolicy()
2026-10-08T15:38:44.535Z  INFO 206632 --- [users-permissions-service] [           main] o.t.utility.ImageNameSubstitutor         : Image name substitution will be performed by: DefaultImageNameSubstitutor (composite of 'ConfigurationFileImageNameSubstitutor' and 'PrefixingImageNameSubstitutor')
2026-10-08T15:38:44.537Z  INFO 206632 --- [users-permissions-service] [           main] org.testcontainers.DockerClientFactory   : Testcontainers version: 1.20.4
2026-10-08T15:38:44.639Z  INFO 206632 --- [users-permissions-service] [           main] .t.d.DockerMachineClientProviderStrategy : docker-machine executable was not found on PATH ([/root/.local/bin, /usr/local/sbin, /usr/local/bin, /usr/sbin, /usr/bin, /sbin, /bin])
2026-10-08T15:38:44.639Z ERROR 206632 --- [users-permissions-service] [           main] o.t.d.DockerClientProviderStrategy       : Could not find a valid Docker environment. Please check configuration. Attempted configurations were:
	UnixSocketClientProviderStrategy: failed with exception BadRequestException (Status 400: {"message":"client version 1.32 is too old. Minimum supported API version is 1.40, please upgrade your client to a newer version"}
)
	DockerDesktopClientProviderStrategy: failed with exception NullPointerException (Cannot invoke "java.nio.file.Path.toString()" because the return value of "org.testcontainers.dockerclient.DockerDesktopClientProviderStrategy.getSocketPath()" is null)As no valid configuration was found, execution cannot continue.
See https://java.testcontainers.org/on_failure.html for more details.
[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 0.118 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.controller.[1mPermissionControllerIntegrationTest[m
[[1;31mERROR[m] com.bookinghub.userspermissions.controller.PermissionControllerIntegrationTest -- Time elapsed: 0.118 s <<< ERROR!
java.lang.IllegalStateException: Could not find a valid Docker environment. Please see logs and check configuration
	at org.testcontainers.dockerclient.DockerClientProviderStrategy.lambda$getFirstValidStrategy$7(DockerClientProviderStrategy.java:274)
	at java.base/java.util.Optional.orElseThrow(Optional.java:403)
	at org.testcontainers.dockerclient.DockerClientProviderStrategy.getFirstValidStrategy(DockerClientProviderStrategy.java:265)
	at org.testcontainers.DockerClientFactory.getOrInitializeStrategy(DockerClientFactory.java:154)
	at org.testcontainers.DockerClientFactory.client(DockerClientFactory.java:196)
	at org.testcontainers.DockerClientFactory$1.getDockerClient(DockerClientFactory.java:108)
	at com.github.dockerjava.api.DockerClientDelegate.authConfig(DockerClientDelegate.java:109)
	at org.testcontainers.containers.GenericContainer.start(GenericContainer.java:321)
	at org.testcontainers.junit.jupiter.TestcontainersExtension$StoreAdapter.start(TestcontainersExtension.java:276)
	at org.testcontainers.junit.jupiter.TestcontainersExtension$StoreAdapter.access$200(TestcontainersExtension.java:263)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.lambda$null$4(TestcontainersExtension.java:83)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.lambda$startContainers$5(TestcontainersExtension.java:83)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.startContainers(TestcontainersExtension.java:83)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.beforeAll(TestcontainersExtension.java:57)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

2026-10-08T15:38:44.641Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.ApplicationContextBootTest]: ApplicationContextBootTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:44.643Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.ApplicationContextBootTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:38:44.652Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : Starting ApplicationContextBootTest using Java 21.0.12.1 with PID 206632 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:38:44.652Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : The following 1 profile is active: "test"
2026-10-08T15:38:44.747Z  INFO 206632 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:38:44.751Z  INFO 206632 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 3 ms. Found 4 JPA repository interfaces.
2026-10-08T15:38:44.834Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:38:44.834Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:38:44.834Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:38:44.866Z  INFO 206632 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:38:44.866Z  INFO 206632 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 213 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:38:44.977Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Starting...
2026-10-08T15:38:44.981Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-3 - Added connection org.postgresql.jdbc.PgConnection@25d45101
2026-10-08T15:38:44.981Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Start completed.
2026-10-08T15:38:44.981Z  INFO 206632 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T15:38:44.984Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.001s)
2026-10-08T15:38:45.020Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:38:45.020Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:38:45.028Z  INFO 206632 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:38:45.029Z  INFO 206632 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:38:45.031Z  INFO 206632 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:38:45.071Z  INFO 206632 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:38:45.077Z  INFO 206632 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:38:45.286Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-08T15:38:45.473Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 37609 (http) with context path '/'
2026-10-08T15:38:45.520Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.ApplicationContextBootTest         : Started ApplicationContextBootTest in 0.875 seconds (process running for 10.25)
2026-10-08T15:38:45.554Z  INFO 206632 --- [users-permissions-service] [o-auto-2-exec-1] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-08T15:38:45.554Z  INFO 206632 --- [users-permissions-service] [o-auto-2-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-08T15:38:45.554Z  INFO 206632 --- [users-permissions-service] [o-auto-2-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
2026-10-08T15:38:45.577Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.outbox.OutboxPublisherTest]: OutboxPublisherTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:45.579Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.outbox.OutboxPublisherTest
2026-10-08T15:38:45.581Z  INFO 206632 --- [users-permissions-service] [           main] org.testcontainers.DockerClientFactory   : Testcontainers version: 1.20.4
[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 0.006 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.outbox.[1mOutboxPublisherTest[m
[[1;31mERROR[m] com.bookinghub.userspermissions.outbox.OutboxPublisherTest -- Time elapsed: 0.006 s <<< ERROR!
java.lang.IllegalStateException: Previous attempts to find a Docker environment failed. Will not retry. Please see logs and check configuration
	at org.testcontainers.dockerclient.DockerClientProviderStrategy.getFirstValidStrategy(DockerClientProviderStrategy.java:229)
	at org.testcontainers.DockerClientFactory.getOrInitializeStrategy(DockerClientFactory.java:154)
	at org.testcontainers.DockerClientFactory.client(DockerClientFactory.java:196)
	at org.testcontainers.DockerClientFactory$1.getDockerClient(DockerClientFactory.java:108)
	at com.github.dockerjava.api.DockerClientDelegate.authConfig(DockerClientDelegate.java:109)
	at org.testcontainers.containers.GenericContainer.start(GenericContainer.java:321)
	at org.testcontainers.junit.jupiter.TestcontainersExtension$StoreAdapter.start(TestcontainersExtension.java:276)
	at org.testcontainers.junit.jupiter.TestcontainersExtension$StoreAdapter.access$200(TestcontainersExtension.java:263)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.lambda$null$4(TestcontainersExtension.java:83)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.lambda$startContainers$5(TestcontainersExtension.java:83)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.startContainers(TestcontainersExtension.java:83)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.beforeAll(TestcontainersExtension.java:57)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

2026-10-08T15:38:46.091Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.repository.PermissionSeedDataTest]: PermissionSeedDataTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:46.096Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.repository.PermissionSeedDataTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:38:46.128Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : Starting PermissionSeedDataTest using Java 21.0.12.1 with PID 206632 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:38:46.128Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : The following 1 profile is active: "test"
2026-10-08T15:38:46.146Z  INFO 206632 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-08T15:38:46.166Z  INFO 206632 --- [users-permissions-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 19 ms. Found 4 JPA repository interfaces.
2026-10-08T15:38:46.182Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Starting...
2026-10-08T15:38:46.186Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-4 - Added connection org.postgresql.jdbc.PgConnection@6d223cb2
2026-10-08T15:38:46.186Z  INFO 206632 --- [users-permissions-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Start completed.
2026-10-08T15:38:46.187Z  INFO 206632 --- [users-permissions-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/userperm_test (PostgreSQL 16.15)
2026-10-08T15:38:46.190Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.001s)
2026-10-08T15:38:46.219Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-08T15:38:46.219Z  INFO 206632 --- [users-permissions-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-08T15:38:46.224Z  INFO 206632 --- [users-permissions-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-08T15:38:46.224Z  INFO 206632 --- [users-permissions-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-08T15:38:46.227Z  INFO 206632 --- [users-permissions-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-08T15:38:46.279Z  INFO 206632 --- [users-permissions-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-08T15:38:46.284Z  INFO 206632 --- [users-permissions-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-08T15:38:46.330Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.repository.PermissionSeedDataTest  : Started PermissionSeedDataTest in 0.211 seconds (process running for 11.061)
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0 where p1_0.legacy_flag=?
Hibernate: select p1_0.id,p1_0.confirmed,p1_0.gated_actions,p1_0.keycloak_role,p1_0.legacy_flag from permissions p1_0
2026-10-08T15:38:46.365Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.repository.OutboxAndResetTokenRepositoryTest]: OutboxAndResetTokenRepositoryTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:46.367Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.repository.OutboxAndResetTokenRepositoryTest
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into outbox (aggregate_id,aggregate_type,attempt_count,created_at,exchange,idempotency_key,payload,published_at,routing_key,status,id) values (?,?,?,?,?,?,?,?,?,?,?)
Hibernate: select oe1_0.id,oe1_0.aggregate_id,oe1_0.aggregate_type,oe1_0.attempt_count,oe1_0.created_at,oe1_0.exchange,oe1_0.idempotency_key,oe1_0.payload,oe1_0.published_at,oe1_0.routing_key,oe1_0.status from outbox oe1_0 where oe1_0.status=? order by oe1_0.created_at fetch first ? rows only
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into password_reset_tokens (created_at,expires_at,token_hash,used_at,user_id,id) values (?,?,?,?,?,?)
Hibernate: select prt1_0.id,prt1_0.created_at,prt1_0.expires_at,prt1_0.token_hash,prt1_0.used_at,prt1_0.user_id from password_reset_tokens prt1_0 where prt1_0.token_hash=?
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where upper(u1_0.email)=upper(?)
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: select u1_0.id,u1_0.created_at,u1_0.deleted_at,u1_0.display_name,u1_0.email,u1_0.updated_at from users u1_0 where u1_0.id=?
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
Hibernate: insert into users (created_at,deleted_at,display_name,email,updated_at,id) values (?,?,?,?,?,?)
2026-10-08T15:38:46.383Z  WARN 206632 --- [users-permissions-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-10-08T15:38:46.383Z ERROR 206632 --- [users-permissions-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uq_users_email_lower"
  Detail: Key (lower(email::text))=(case-test-1791473926380@example.com) already exists.
2026-10-08T15:38:46.425Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:46.430Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest
2026-10-08T15:38:46.433Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:46.434Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:38:46.470Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 206632 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:38:46.470Z  INFO 206632 --- [users-permissions-service] [           main] c.b.u.security.Tier2FailClosedTest       : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:38:47.032Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:38:47.033Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:38:47.033Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:38:47.079Z  WARN 206632 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:38:47.080Z  WARN 206632 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:38:47.080Z  WARN 206632 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:38:47.081Z  WARN 206632 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:38:47.081Z  WARN 206632 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:38:47.081Z  INFO 206632 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:38:47.081Z  WARN 206632 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:38:47.081Z  INFO 206632 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 610 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:38:47.332Z  WARN 206632 --- [users-permissions-service] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
2026-10-08T15:38:47.334Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-10-08T15:38:47.368Z  INFO 206632 --- [users-permissions-service] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-10-08T15:38:47.381Z ERROR 206632 --- [users-permissions-service] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 0 of constructor in com.bookinghub.userspermissions.service.PermissionService required a bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' that could not be found.


Action:

Consider defining a bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' in your configuration.




============================
CONDITIONS EVALUATION REPORT
============================


Positive matches:
-----------------

    None


Negative matches:
-----------------

    None


Exclusions:
-----------

    None


Unconditional classes:
----------------------

    None



2026-10-08T15:38:47.385Z  WARN 206632 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest@33945d80]

java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor.instantiateTestClass(NestedClassTestDescriptor.java:85) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:287) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152) ~[spring-test-6.1.13.jar:6.1.13]
	... 88 common frames omitted
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 113 common frames omitted
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 127 common frames omitted


  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:38:47.465Z  INFO 206632 --- [users-permissions-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 206632 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:38:47.465Z  INFO 206632 --- [users-permissions-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:38:47.880Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:38:47.880Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:38:47.881Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:38:47.923Z  INFO 206632 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-3].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:38:47.923Z  INFO 206632 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 456 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:38:47.983Z  WARN 206632 --- [users-permissions-service] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
2026-10-08T15:38:47.984Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-10-08T15:38:47.986Z  INFO 206632 --- [users-permissions-service] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-10-08T15:38:47.989Z ERROR 206632 --- [users-permissions-service] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 0 of constructor in com.bookinghub.userspermissions.service.PermissionService required a bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' that could not be found.


Action:

Consider defining a bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' in your configuration.




============================
CONDITIONS EVALUATION REPORT
============================


Positive matches:
-----------------

    None


Negative matches:
-----------------

    None


Exclusions:
-----------

    None


Unconditional classes:
----------------------

    None



2026-10-08T15:38:47.989Z  WARN 206632 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.test.context.support.DependencyInjectionTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest@3179f8eb]

java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@6d0c95d3 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.InsufficientRoleTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@73051634 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.test.context.support.DynamicPropertiesContextCustomizer@5f177b9d, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152) ~[spring-test-6.1.13.jar:6.1.13]
	... 84 common frames omitted
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 109 common frames omitted
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 123 common frames omitted

[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 1.590 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.security.[1mTier2FailClosedTest$InsufficientRoleTest[m
[[1;31mERROR[m] com.bookinghub.userspermissions.security.Tier2FailClosedTest$InsufficientRoleTest.insufficientRole_returns403ApiError -- Time elapsed: 0.029 s <<< ERROR!
java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180)
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63)
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	Suppressed: java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@6d0c95d3 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.InsufficientRoleTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@73051634 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.test.context.support.DynamicPropertiesContextCustomizer@5f177b9d, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180)
		at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98)
		at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
		at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
		at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
		at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
		at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
		at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
		at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
		at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
		... 4 more
	Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
		at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
		at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
		at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
		at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
		at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975)
		at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971)
		at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625)
		at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146)
		at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754)
		at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456)
		at org.springframework.boot.SpringApplication.run(SpringApplication.java:335)
		at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137)
		at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58)
		at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46)
		at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463)
		at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553)
		at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137)
		at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108)
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225)
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152)
		... 19 more
	Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
		at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
		at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
		at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
		at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
		at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
		at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
		at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
		... 44 more
	Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
		at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
		... 58 more
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975)
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971)
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146)
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754)
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335)
	at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137)
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58)
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46)
	at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463)
	at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553)
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137)
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108)
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225)
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152)
	... 19 more
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
	... 44 more
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
	... 58 more

2026-10-08T15:38:48.025Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:48.027Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest
2026-10-08T15:38:48.070Z  WARN 206632 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest@2172104e]

java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor.instantiateTestClass(NestedClassTestDescriptor.java:85) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:287) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]


  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-08T15:38:48.123Z  INFO 206632 --- [users-permissions-service] [           main] u.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 206632 (started by root in /home/daytona/project/services/users-permissions-service)
2026-10-08T15:38:48.123Z  INFO 206632 --- [users-permissions-service] [           main] u.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-08T15:38:48.378Z  INFO 206632 --- [users-permissions-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-08T15:38:48.378Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-08T15:38:48.378Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-08T15:38:48.422Z  INFO 206632 --- [users-permissions-service] [           main] o.a.c.c.C.[Tomcat-4].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-08T15:38:48.423Z  INFO 206632 --- [users-permissions-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 299 ms
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
2026-10-08T15:38:48.525Z  WARN 206632 --- [users-permissions-service] [      Cleaner-0] org.jboss.resteasy.client.jaxrs.i18n     : RESTEASY004687: Closing a class org.jboss.resteasy.client.jaxrs.engines.ManualClosingApacheHttpClient43Engine$CleanupAction instance for you. Please close clients yourself.
2026-10-08T15:38:48.630Z  WARN 206632 --- [users-permissions-service] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
2026-10-08T15:38:48.630Z  INFO 206632 --- [users-permissions-service] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-10-08T15:38:48.633Z  INFO 206632 --- [users-permissions-service] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-10-08T15:38:48.666Z ERROR 206632 --- [users-permissions-service] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 0 of constructor in com.bookinghub.userspermissions.service.PermissionService required a bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' that could not be found.


Action:

Consider defining a bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' in your configuration.




============================
CONDITIONS EVALUATION REPORT
============================


Positive matches:
-----------------

    None


Negative matches:
-----------------

    None


Exclusions:
-----------

    None


Unconditional classes:
----------------------

    None



2026-10-08T15:38:48.667Z  WARN 206632 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.test.context.support.DependencyInjectionTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest@2f719c9d]

java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@5c536b2e testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.MissingTokenTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@357d5c80 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.test.context.support.DynamicPropertiesContextCustomizer@fcd39707, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625) ~[spring-context-6.1.13.jar:6.1.13]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46) ~[spring-core-6.1.13.jar:6.1.13]
	at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463) ~[spring-boot-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152) ~[spring-test-6.1.13.jar:6.1.13]
	... 84 common frames omitted
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 109 common frames omitted
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.13.jar:6.1.13]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.13.jar:6.1.13]
	... 123 common frames omitted

[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 0.647 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.security.[1mTier2FailClosedTest$MissingTokenTest[m
[[1;31mERROR[m] com.bookinghub.userspermissions.security.Tier2FailClosedTest$MissingTokenTest.missingToken_returns401 -- Time elapsed: 0.001 s <<< ERROR!
java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145)
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63)
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	Suppressed: java.lang.IllegalStateException: Failed to load ApplicationContext for [WebMergedContextConfiguration@5c536b2e testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.MissingTokenTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@357d5c80 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.test.context.support.DynamicPropertiesContextCustomizer@fcd39707, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:180)
		at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98)
		at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
		at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
		at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
		at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
		at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
		at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
		at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
		at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
		... 4 more
	Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionController' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/controller/PermissionController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
		at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
		at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
		at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
		at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
		at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975)
		at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971)
		at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625)
		at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146)
		at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754)
		at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456)
		at org.springframework.boot.SpringApplication.run(SpringApplication.java:335)
		at org.springframework.boot.test.context.SpringBootContextLoader.lambda$loadContext$3(SpringBootContextLoader.java:137)
		at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:58)
		at org.springframework.util.function.ThrowingSupplier.get(ThrowingSupplier.java:46)
		at org.springframework.boot.SpringApplication.withHook(SpringApplication.java:1463)
		at org.springframework.boot.test.context.SpringBootContextLoader$ContextLoaderHook.run(SpringBootContextLoader.java:553)
		at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:137)
		at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:108)
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:225)
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:152)
		... 19 more
	Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'permissionService' defined in file [/home/daytona/project/services/users-permissions-service/target/classes/com/bookinghub/userspermissions/service/PermissionService.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795)
		at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562)
		at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522)
		at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337)
		at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
		at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335)
		at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200)
		at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
		at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
		... 44 more
	Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.bookinghub.userspermissions.repository.PermissionRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1880)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1406)
		at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353)
		at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904)
		at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782)
		... 58 more

2026-10-08T15:38:48.673Z  INFO 206632 --- [users-permissions-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-08T15:38:48.674Z  INFO 206632 --- [users-permissions-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.userspermissions.UsersPermissionsServiceApplication for test class com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification
2026-10-08T15:38:48.675Z  WARN 206632 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest@33536319]

java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63) ~[spring-boot-test-3.3.4.jar:3.3.4]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor.instantiateTestClass(NestedClassTestDescriptor.java:85) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:287) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]

2026-10-08T15:38:48.676Z  WARN 206632 --- [users-permissions-service] [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.test.context.support.DependencyInjectionTestExecutionListener] to prepare test instance [com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification@36b2cb6d]

java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@5649af04 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.JwksFailClosedVerification, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@7a52cdce key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260) ~[spring-test-6.1.13.jar:6.1.13]
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163) ~[spring-test-6.1.13.jar:6.1.13]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$10(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.executeAndMaskThrowable(ClassBasedTestDescriptor.java:383) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeTestInstancePostProcessors$11(ClassBasedTestDescriptor.java:378) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeTestInstancePostProcessors(ClassBasedTestDescriptor.java:377) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$instantiateAndPostProcessTestInstance$6(ClassBasedTestDescriptor.java:290) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.instantiateAndPostProcessTestInstance(ClassBasedTestDescriptor.java:289) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$4(ClassBasedTestDescriptor.java:279) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at java.base/java.util.Optional.orElseGet(Optional.java:364) ~[na:na]
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$testInstancesProvider$5(ClassBasedTestDescriptor.java:278) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.execution.TestInstancesProvider.getTestInstances(TestInstancesProvider.java:31) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$prepare$0(TestMethodTestDescriptor.java:106) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:105) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.prepare(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.3.jar:5.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$prepare$2(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.prepare(NodeTestTask.java:123) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:90) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.3.jar:1.10.3]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.2.5.jar:3.2.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.2.5.jar:3.2.5]

[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 0.005 s[1;31m <<< FAILURE![m -- in com.bookinghub.userspermissions.security.[1mTier2FailClosedTest$JwksFailClosedVerification[m
[[1;31mERROR[m] com.bookinghub.userspermissions.security.Tier2FailClosedTest$JwksFailClosedVerification.jwksOutageHandlerExists -- Time elapsed: 0.001 s <<< ERROR!
java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145)
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.postProcessFields(MockitoTestExecutionListener.java:122)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.injectFields(MockitoTestExecutionListener.java:106)
	at org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.prepareTestInstance(MockitoTestExecutionListener.java:63)
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
	at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	Suppressed: java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@5649af04 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest.JwksFailClosedVerification, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@7a52cdce key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null]
		at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145)
		at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:142)
		at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:98)
		at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:260)
		at org.springframework.test.context.junit.jupiter.SpringExtension.postProcessTestInstance(SpringExtension.java:163)
		at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
		at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:179)
		at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1708)
		at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
		at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
		at java.base/java.util.stream.StreamSpliterators$WrappingSpliterator.forEachRemaining(StreamSpliterators.java:310)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
		at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
		... 4 more

[[1;31mERROR[m] [1;31mFailures: [m
[[1;31mERROR[m] [1;31m  UserControllerIntegrationTest.adminAssignsRoles_updatesKeycloakAndCreatesOutboxEvent:309 Status expected:<200> but was:<403>[m
[[1;31mERROR[m] [1;31m  UserControllerIntegrationTest.adminCreatesUser_provisionKeycloakAndLocalRowAndOutboxEvent:113 Status expected:<201> but was:<403>[m
[[1;31mERROR[m] [1;31m  UserControllerIntegrationTest.createUserWithCaseVariantEmail_returns409:181 Status expected:<409> but was:<403>[m
[[1;31mERROR[m] [1;31m  UserControllerIntegrationTest.createUserWithDuplicateEmail_returns409_keycloakNotCalled:151 Status expected:<409> but was:<403>[m
[[1;31mERROR[m] [1;31mErrors: [m
[[1;31mERROR[m] [1;31m  PermissionControllerIntegrationTest ? IllegalState Could not find a valid Docker environment. Please see logs and check configuration[m
[[1;31mERROR[m] [1;31m  OutboxPublisherTest ? IllegalState Previous attempts to find a Docker environment failed. Will not retry. Please see logs and check configuration[m
[[1;31mERROR[m] [1;31m  Tier2FailClosedTest$InsufficientRoleTest.insufficientRole_returns403ApiError ? IllegalState Failed to load ApplicationContext for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null][m
[[1;31mERROR[m] [1;31m  Tier2FailClosedTest$JwksFailClosedVerification.jwksOutageHandlerExists ? IllegalState ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null][m
[[1;31mERROR[m] [1;31m  Tier2FailClosedTest$MissingTokenTest.missingToken_returns401 ? IllegalState ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration@49d75bb0 testClass = com.bookinghub.userspermissions.security.Tier2FailClosedTest, locations = [], classes = [com.bookinghub.userspermissions.UsersPermissionsServiceApplication], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration", "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true", "server.port=0"], contextCustomizers = [[ImportsContextCustomizer@744b5bd7 key = [org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityConfiguration, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration, org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration]], org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@3406472c, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@40317ba2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@2281abf4, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@5082d622, org.springframework.boot.test.web.reactor.netty.DisableReactorResourceFactoryGlobalResourcesContextCustomizerFactory$DisableReactorResourceFactoryGlobalResourcesContextCustomizerCustomizer@258d79be, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@4b3fa0b3, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@7ceb3185, org.springframework.boot.test.context.SpringBootTestAnnotation@f2860776], resourceBasePath = "src/main/webapp", contextLoader = org.springframework.boot.test.context.SpringBootContextLoader, parent = null][m
[[1;31mERROR[m] [1;31mTests run: 38, Failures: 4, Errors: 5, Skipped: 0[m
[[1;31mERROR[m] Failed to execute goal [32morg.apache.maven.plugins:maven-surefire-plugin:3.2.5:test[m [1m(default-test)[m on project [36musers-permissions-service[m: [1;31mThere are test failures.[m
[[1;31mERROR[m] [1;31m[m
[[1;31mERROR[m] [1;31mPlease refer to /home/daytona/project/services/users-permissions-service/target/surefire-reports for the individual test results.[m
[[1;31mERROR[m] [1;31mPlease refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.[m
[[1;31mERROR[m] -> [1m[Help 1][m
[[1;31mERROR[m] 
[[1;31mERROR[m] To see the full stack trace of the errors, re-run Maven with the [1m-e[m switch.
[[1;31mERROR[m] Re-run Maven using the [1m-X[m switch to enable full debug logging.
[[1;31mERROR[m] 
[[1;31mERROR[m] For more information about the errors and possible solutions, please read the following articles:
[[1;31mERROR[m] [1m[Help 1][m http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
```


## Backend pre-push gate

- Status: skipped
- Result marker + failing output tail:
```
__GATE__ build_exit=-1 test_exit=-1 build_cmd=[none] test_cmd=[none] head=fad146e9516d0e79890b7132350eea01b35e3066 test_files=27 skip_marks=2 shadow_files=0
```
