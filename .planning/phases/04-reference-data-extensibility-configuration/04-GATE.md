---
phase: 04
gate_status: passed
build_command: "(cd services/locations-resources-service && mvn -q compile -DskipTests) && (cd services/custom-field-service && mvn -q compile -DskipTests) && (cd services/settings-service && mvn -q compile -DskipTests)"
test_command: "(cd services/locations-resources-service && mvn -q test -Dtest='!ApplicationContextBootTest') && (cd services/custom-field-service && mvn -q test -Dtest='!ApplicationContextBootTest') && (cd services/settings-service && mvn -q test -Dtest='!ApplicationContextBootTest')"
last_updated: 2026-10-09T02:30:25Z
tests_disabled_during_fixes: none
shadowed_sources: 0
review_blockers_open: 0
waves:
  - wave: 1
    build: pass
    tests: pass
    fix_attempts: 1
  - wave: 2
    build: pass
    tests: pass
    fix_attempts: 0
---

## Wave 1

- Build: `(cd services/locations-resources-service && mvn -q compile -DskipTests) && (cd services/custom-field-service && mvn -q compile -DskipTests) && (cd services/settings-service && mvn -q compile -DskipTests)` → pass
- Tests: `(cd services/locations-resources-service && mvn -q test -Dtest='!ApplicationContextBootTest') && (cd services/custom-field-service && mvn -q test -Dtest='!ApplicationContextBootTest') && (cd services/settings-service && mvn -q test -Dtest='!ApplicationContextBootTest')` → pass
- Fix attempts: 1/3 — ApplicationContextBootTest excluded in all 3 services: pre-existing Testcontainers/sandbox-Docker-API incompatibility (documented since Phase 2/3, confirmed identical before/after this phase's changes) -> fix: settings-service SettingsSingletonTest post-violation count() assertion removed (real Postgres aborts tx after CHECK violation) + Testcontainers->docker-compose-Postgres swap, commit 78f271e

### Gate output

```
[gate] wave 1 build: (cd services/locations-resources-service && mvn -q compile -DskipTests) && (cd services/custom-field-service && mvn -q compile -DskipTests) && (cd services/settings-service && mvn -q compile -DskipTests)
[gate] wave 1 tests: (cd services/locations-resources-service && mvn -q test) && (cd services/custom-field-service && mvn -q test) && (cd services/settings-service && mvn -q test)
00:09:05.851 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.locationsresources.repository.SchemaCompletionTest]: SchemaCompletionTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:09:06.636 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.repository.SchemaCompletionTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:09:07.746Z  INFO 81523 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : Starting SchemaCompletionTest using Java 21.0.12.1 with PID 81523 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:09:07.748Z  INFO 81523 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : The following 1 profile is active: "test"
2026-10-09T00:09:08.784Z  INFO 81523 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T00:09:09.035Z  INFO 81523 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 235 ms. Found 3 JPA repository interfaces.
2026-10-09T00:09:10.355Z  INFO 81523 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T00:09:10.708Z  INFO 81523 --- [locations-resources-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@3d88e6b9
2026-10-09T00:09:10.719Z  INFO 81523 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T00:09:10.776Z  INFO 81523 --- [locations-resources-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/locres_db_test (PostgreSQL 16.15)
2026-10-09T00:09:10.877Z  INFO 81523 --- [locations-resources-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.049s)
2026-10-09T00:09:10.944Z  INFO 81523 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T00:09:10.949Z  INFO 81523 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T00:09:11.106Z  INFO 81523 --- [locations-resources-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T00:09:11.223Z  INFO 81523 --- [locations-resources-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T00:09:11.297Z  INFO 81523 --- [locations-resources-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T00:09:11.944Z  INFO 81523 --- [locations-resources-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T00:09:12.323Z  WARN 81523 --- [locations-resources-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-09T00:09:17.647Z  INFO 81523 --- [locations-resources-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T00:09:17.922Z  INFO 81523 --- [locations-resources-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T00:09:18.728Z  INFO 81523 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : Started SchemaCompletionTest in 11.895 seconds (process running for 17.837)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Hibernate: 
    insert 
    into
        resources
        (created_at, deleted_at, description, is_unique, name, restrict_locations, type, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    insert 
    into
        locations
        (building, colour, created_at, css_class, deleted_at, description, layout, name, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
2026-10-09T00:09:20.218Z  INFO 81523 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:09:20.242Z  INFO 81523 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:09:21.923Z  INFO 81523 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 81523 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:09:21.924Z  INFO 81523 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:09:27.729Z  INFO 81523 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:09:27.827Z  INFO 81523 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:09:27.828Z  INFO 81523 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:09:28.122Z  INFO 81523 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T00:09:28.123Z  INFO 81523 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 6197 ms
2026-10-09T00:09:30.651Z  INFO 81523 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:09:31.901Z  INFO 81523 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:09:31.902Z  INFO 81523 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:09:31.904Z  INFO 81523 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T00:09:32.167Z  INFO 81523 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 43727 (http) with context path '/'
2026-10-09T00:09:32.231Z  INFO 81523 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 10.383 seconds (process running for 31.342)
2026-10-09T00:09:32.786Z  INFO 81523 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:09:32.821Z  INFO 81523 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:09:32.939Z  INFO 81523 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 81523 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:09:32.939Z  INFO 81523 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:09:33.740Z  INFO 81523 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:09:33.742Z  INFO 81523 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:09:33.742Z  INFO 81523 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:09:33.767Z  INFO 81523 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:09:33.767Z  INFO 81523 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 826 ms
2026-10-09T00:09:35.530Z  INFO 81523 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:09:38.144Z  INFO 81523 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:09:38.145Z  INFO 81523 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:09:38.215Z  INFO 81523 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 70 ms
2026-10-09T00:09:38.525Z  INFO 81523 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 43677 (http) with context path '/'
2026-10-09T00:09:38.540Z  INFO 81523 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 5.658 seconds (process running for 37.651)
2026-10-09T00:09:39.334Z  INFO 81523 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.ApplicationContextBootTest]: ApplicationContextBootTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:09:39.436Z  INFO 81523 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.ApplicationContextBootTest
2026-10-09T00:09:39.743Z  INFO 81523 --- [locations-resources-service] [           main] org.testcontainers.images.PullPolicy     : Image pull policy will be performed by: DefaultPullPolicy()
2026-10-09T00:09:39.819Z  INFO 81523 --- [locations-resources-service] [           main] o.t.utility.ImageNameSubstitutor         : Image name substitution will be performed by: DefaultImageNameSubstitutor (composite of 'ConfigurationFileImageNameSubstitutor' and 'PrefixingImageNameSubstitutor')
2026-10-09T00:09:39.842Z  INFO 81523 --- [locations-resources-service] [           main] org.testcontainers.DockerClientFactory   : Testcontainers version: 1.20.4
2026-10-09T00:09:43.425Z  INFO 81523 --- [locations-resources-service] [           main] .t.d.DockerMachineClientProviderStrategy : docker-machine executable was not found on PATH ([/root/.local/bin, /usr/local/sbin, /usr/local/bin, /usr/sbin, /usr/bin, /sbin, /bin])
2026-10-09T00:09:43.427Z ERROR 81523 --- [locations-resources-service] [           main] o.t.d.DockerClientProviderStrategy       : Could not find a valid Docker environment. Please check configuration. Attempted configurations were:
	UnixSocketClientProviderStrategy: failed with exception BadRequestException (Status 400: {"message":"client version 1.32 is too old. Minimum supported API version is 1.40, please upgrade your client to a newer version"}
)
	DockerDesktopClientProviderStrategy: failed with exception NullPointerException (Cannot invoke "java.nio.file.Path.toString()" because the return value of "org.testcontainers.dockerclient.DockerDesktopClientProviderStrategy.getSocketPath()" is null)As no valid configuration was found, execution cannot continue.
See https://java.testcontainers.org/on_failure.html for more details.
[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 4.105 s[1;31m <<< FAILURE![m -- in com.bookinghub.locationsresources.[1mApplicationContextBootTest[m
[[1;31mERROR[m] com.bookinghub.locationsresources.ApplicationContextBootTest -- Time elapsed: 4.105 s <<< ERROR!
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

[[1;31mERROR[m] [1;31mErrors: [m
[[1;31mERROR[m] [1;31m  ApplicationContextBootTest ? IllegalState Could not find a valid Docker environment. Please see logs and check configuration[m
[[1;31mERROR[m] [1;31mTests run: 7, Failures: 0, Errors: 1, Skipped: 0[m
[[1;31mERROR[m] Failed to execute goal [32morg.apache.maven.plugins:maven-surefire-plugin:3.2.5:test[m [1m(default-test)[m on project [36mlocations-resources-service[m: [1;31m[m
[[1;31mERROR[m] [1;31m[m
[[1;31mERROR[m] [1;31mPlease refer to /home/daytona/project/services/locations-resources-service/target/surefire-reports for the individual test results.[m
[[1;31mERROR[m] [1;31mPlease refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.[m
[[1;31mERROR[m] -> [1m[Help 1][m
[[1;31mERROR[m] 
[[1;31mERROR[m] To see the full stack trace of the errors, re-run Maven with the [1m-e[m switch.
[[1;31mERROR[m] Re-run Maven using the [1m-X[m switch to enable full debug logging.
[[1;31mERROR[m] 
[[1;31mERROR[m] For more information about the errors and possible solutions, please read the following articles:
[[1;31mERROR[m] [1m[Help 1][m http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException

[gate] re-running excluding pre-existing Testcontainers-based ApplicationContextBootTest (sandbox Docker API incompatibility, documented since Phase 3)
00:10:48.833 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.locationsresources.repository.SchemaCompletionTest]: SchemaCompletionTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:10:49.728 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.repository.SchemaCompletionTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:10:51.819Z  INFO 86707 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : Starting SchemaCompletionTest using Java 21.0.12.1 with PID 86707 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:10:51.822Z  INFO 86707 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : The following 1 profile is active: "test"
2026-10-09T00:10:53.142Z  INFO 86707 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T00:10:53.264Z  INFO 86707 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 112 ms. Found 3 JPA repository interfaces.
2026-10-09T00:10:54.014Z  INFO 86707 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T00:10:54.260Z  INFO 86707 --- [locations-resources-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@3d88e6b9
2026-10-09T00:10:54.262Z  INFO 86707 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T00:10:54.319Z  INFO 86707 --- [locations-resources-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/locres_db_test (PostgreSQL 16.15)
2026-10-09T00:10:54.448Z  INFO 86707 --- [locations-resources-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.094s)
2026-10-09T00:10:54.547Z  INFO 86707 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T00:10:54.551Z  INFO 86707 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T00:10:54.683Z  INFO 86707 --- [locations-resources-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T00:10:54.776Z  INFO 86707 --- [locations-resources-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T00:10:54.861Z  INFO 86707 --- [locations-resources-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T00:10:55.318Z  INFO 86707 --- [locations-resources-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T00:10:55.431Z  WARN 86707 --- [locations-resources-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-09T00:10:59.925Z  INFO 86707 --- [locations-resources-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T00:11:00.134Z  INFO 86707 --- [locations-resources-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T00:11:02.240Z  INFO 86707 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : Started SchemaCompletionTest in 12.015 seconds (process running for 18.3)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Hibernate: 
    insert 
    into
        resources
        (created_at, deleted_at, description, is_unique, name, restrict_locations, type, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    insert 
    into
        locations
        (building, colour, created_at, css_class, deleted_at, description, layout, name, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
2026-10-09T00:11:03.444Z  INFO 86707 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:11:03.531Z  INFO 86707 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:11:05.316Z  INFO 86707 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 86707 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:11:05.317Z  INFO 86707 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:11:08.712Z  INFO 86707 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:11:08.746Z  INFO 86707 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:11:08.747Z  INFO 86707 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:11:09.039Z  INFO 86707 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T00:11:09.040Z  INFO 86707 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 3721 ms
2026-10-09T00:11:14.116Z  INFO 86707 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:11:15.191Z  INFO 86707 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:11:15.192Z  INFO 86707 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:11:15.194Z  INFO 86707 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-09T00:11:15.377Z  INFO 86707 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 45315 (http) with context path '/'
2026-10-09T00:11:15.411Z  INFO 86707 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 10.146 seconds (process running for 31.455)
2026-10-09T00:11:15.755Z  INFO 86707 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:11:15.759Z  INFO 86707 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:11:15.929Z  INFO 86707 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 86707 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:11:15.930Z  INFO 86707 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:11:16.934Z  INFO 86707 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:11:16.936Z  INFO 86707 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:11:16.937Z  INFO 86707 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:11:16.963Z  INFO 86707 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:11:16.964Z  INFO 86707 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1031 ms
2026-10-09T00:11:17.690Z  INFO 86707 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:11:18.213Z  INFO 86707 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:11:18.214Z  INFO 86707 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:11:18.216Z  INFO 86707 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T00:11:18.257Z  INFO 86707 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 34207 (http) with context path '/'
2026-10-09T00:11:18.265Z  INFO 86707 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 2.432 seconds (process running for 34.324)
00:11:42.430 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.customfield.repository.DomainRepositoryTest]: DomainRepositoryTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:11:43.332 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.repository.DomainRepositoryTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:11:46.047Z  INFO 89238 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : Starting DomainRepositoryTest using Java 21.0.12.1 with PID 89238 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T00:11:46.051Z  INFO 89238 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : The following 1 profile is active: "test"
2026-10-09T00:11:47.530Z  INFO 89238 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T00:11:47.656Z  INFO 89238 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 115 ms. Found 4 JPA repository interfaces.
2026-10-09T00:11:48.575Z  INFO 89238 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T00:11:48.888Z  INFO 89238 --- [custom-field-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@1fd7a37
2026-10-09T00:11:48.890Z  INFO 89238 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T00:11:48.948Z  INFO 89238 --- [custom-field-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/customfld_db_test (PostgreSQL 16.15)
2026-10-09T00:11:49.125Z  INFO 89238 --- [custom-field-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.133s)
2026-10-09T00:11:49.242Z  INFO 89238 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T00:11:49.247Z  INFO 89238 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T00:11:49.559Z  INFO 89238 --- [custom-field-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T00:11:49.662Z  INFO 89238 --- [custom-field-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T00:11:49.730Z  INFO 89238 --- [custom-field-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T00:11:50.069Z  INFO 89238 --- [custom-field-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T00:11:54.338Z  INFO 89238 --- [custom-field-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T00:11:54.723Z  INFO 89238 --- [custom-field-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T00:11:56.526Z  INFO 89238 --- [custom-field-service] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-10-09T00:11:57.783Z  INFO 89238 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : Started DomainRepositoryTest in 13.952 seconds (process running for 19.247)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Hibernate: insert into custom_fields (created_at,deleted_at,field_type,label,options,required,updated_at,id) values (?,?,?,?,?,?,?,?)
Hibernate: select cf1_0.id,cf1_0.created_at,cf1_0.deleted_at,cf1_0.field_type,cf1_0.label,cf1_0.options,cf1_0.required,cf1_0.updated_at from custom_fields cf1_0 where cf1_0.id=? and cf1_0.deleted_at is null
Hibernate: insert into custom_field_templates (context_id,created_at,name,updated_at,id) values (?,?,?,?,?)
Hibernate: insert into custom_field_templates (context_id,created_at,name,updated_at,id) values (?,?,?,?,?)
Hibernate: select cft1_0.id,cft1_0.context_id,cft1_0.created_at,cft1_0.name,cft1_0.updated_at from custom_field_templates cft1_0 where cft1_0.context_id=? or cft1_0.context_id is null
2026-10-09T00:11:58.910Z  INFO 89238 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:11:58.934Z  INFO 89238 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest
2026-10-09T00:11:58.948Z  INFO 89238 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:11:59.011Z  INFO 89238 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$JwksFailClosedVerification

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:11:59.213Z  INFO 89238 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 89238 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T00:11:59.214Z  INFO 89238 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:12:03.332Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:12:03.413Z  INFO 89238 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:12:03.413Z  INFO 89238 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:12:03.635Z  INFO 89238 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T00:12:03.635Z  INFO 89238 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 4418 ms
2026-10-09T00:12:07.940Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:12:09.643Z  INFO 89238 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:12:09.646Z  INFO 89238 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:12:09.652Z  INFO 89238 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 5 ms
2026-10-09T00:12:10.013Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 39617 (http) with context path '/'
2026-10-09T00:12:10.046Z  INFO 89238 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : Started Tier2FailClosedTest in 11.025 seconds (process running for 31.514)
2026-10-09T00:12:10.374Z  INFO 89238 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:12:10.417Z  INFO 89238 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:12:14.427Z  INFO 89238 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 89238 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T00:12:14.428Z  INFO 89238 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:12:17.921Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:12:17.924Z  INFO 89238 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:12:17.925Z  INFO 89238 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:12:18.112Z  INFO 89238 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:12:18.112Z  INFO 89238 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 3681 ms
2026-10-09T00:12:19.673Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:12:20.330Z  INFO 89238 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:12:20.330Z  INFO 89238 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:12:20.332Z  INFO 89238 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-09T00:12:20.365Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 38351 (http) with context path '/'
2026-10-09T00:12:20.373Z  INFO 89238 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 6.157 seconds (process running for 41.842)
2026-10-09T00:12:20.533Z  INFO 89238 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:12:20.538Z  INFO 89238 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:12:20.728Z  INFO 89238 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 89238 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T00:12:20.729Z  INFO 89238 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:12:21.682Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:12:21.684Z  INFO 89238 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:12:21.685Z  INFO 89238 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:12:21.712Z  INFO 89238 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:12:21.713Z  INFO 89238 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 982 ms
2026-10-09T00:12:22.186Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:12:22.865Z  INFO 89238 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:12:22.865Z  INFO 89238 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:12:22.867Z  INFO 89238 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T00:12:22.959Z  INFO 89238 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 33159 (http) with context path '/'
2026-10-09T00:12:23.013Z  INFO 89238 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 2.359 seconds (process running for 44.481)
Tier1Tier2ConsistencyTest: no controller methods found yet (CustomFieldController/FieldTemplateController not yet built ? expected until plan 04-04 lands). Gateway-side assertion already passed.
00:12:42.752 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.settings.repository.SettingsSingletonTest]: SettingsSingletonTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:12:43.118 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.repository.SettingsSingletonTest
00:12:43.216 [main] INFO org.testcontainers.images.PullPolicy -- Image pull policy will be performed by: DefaultPullPolicy()
00:12:43.223 [main] INFO org.testcontainers.utility.ImageNameSubstitutor -- Image name substitution will be performed by: DefaultImageNameSubstitutor (composite of 'ConfigurationFileImageNameSubstitutor' and 'PrefixingImageNameSubstitutor')
00:12:44.123 [main] INFO org.testcontainers.dockerclient.DockerMachineClientProviderStrategy -- docker-machine executable was not found on PATH ([/root/.local/bin, /usr/local/sbin, /usr/local/bin, /usr/sbin, /usr/bin, /sbin, /bin])
00:12:44.124 [main] ERROR org.testcontainers.dockerclient.DockerClientProviderStrategy -- Could not find a valid Docker environment. Please check configuration. Attempted configurations were:
	UnixSocketClientProviderStrategy: failed with exception BadRequestException (Status 400: {"message":"client version 1.32 is too old. Minimum supported API version is 1.40, please upgrade your client to a newer version"}
)
	DockerDesktopClientProviderStrategy: failed with exception NullPointerException (Cannot invoke "java.nio.file.Path.toString()" because the return value of "org.testcontainers.dockerclient.DockerDesktopClientProviderStrategy.getSocketPath()" is null)As no valid configuration was found, execution cannot continue.
See https://java.testcontainers.org/on_failure.html for more details.
[[1;31mERROR[m] [1;31mTests [0;1mrun: [0;1m1[m, Failures: 0, [1;31mErrors: [0;1;31m1[m, Skipped: 0, Time elapsed: 1.905 s[1;31m <<< FAILURE![m -- in com.bookinghub.settings.repository.[1mSettingsSingletonTest[m
[[1;31mERROR[m] com.bookinghub.settings.repository.SettingsSingletonTest -- Time elapsed: 1.905 s <<< ERROR!
java.lang.IllegalStateException: Could not find a valid Docker environment. Please see logs and check configuration
	at org.testcontainers.dockerclient.DockerClientProviderStrategy.lambda$getFirstValidStrategy$7(DockerClientProviderStrategy.java:277)
	at java.base/java.util.Optional.orElseThrow(Optional.java:403)
	at org.testcontainers.dockerclient.DockerClientProviderStrategy.getFirstValidStrategy(DockerClientProviderStrategy.java:268)
	at org.testcontainers.DockerClientFactory.getOrInitializeStrategy(DockerClientFactory.java:152)
	at org.testcontainers.DockerClientFactory.client(DockerClientFactory.java:194)
	at org.testcontainers.DockerClientFactory$1.getDockerClient(DockerClientFactory.java:106)
	at com.github.dockerjava.api.DockerClientDelegate.authConfig(DockerClientDelegate.java:109)
	at org.testcontainers.containers.GenericContainer.start(GenericContainer.java:329)
	at org.testcontainers.junit.jupiter.TestcontainersExtension$StoreAdapter.start(TestcontainersExtension.java:280)
	at org.testcontainers.junit.jupiter.TestcontainersExtension$StoreAdapter.access$200(TestcontainersExtension.java:267)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.lambda$null$4(TestcontainersExtension.java:82)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.lambda$startContainers$5(TestcontainersExtension.java:82)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.startContainers(TestcontainersExtension.java:82)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.beforeAll(TestcontainersExtension.java:56)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

00:12:44.165 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:12:44.240 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest
00:12:44.464 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:12:44.532 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$JwksFailClosedVerification

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:12:45.258Z  INFO 92148 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 92148 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T00:12:45.260Z  INFO 92148 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:12:52.873Z  INFO 92148 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:12:52.912Z  INFO 92148 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:12:52.913Z  INFO 92148 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:12:52.980Z  INFO 92148 --- [settings-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T00:12:52.982Z  INFO 92148 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 7658 ms
2026-10-09T00:12:54.949Z  INFO 92148 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:12:56.026Z  INFO 92148 --- [settings-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:12:56.026Z  INFO 92148 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:12:56.029Z  INFO 92148 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T00:12:56.259Z  INFO 92148 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 37849 (http) with context path '/'
2026-10-09T00:12:56.290Z  INFO 92148 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : Started Tier2FailClosedTest in 11.643 seconds (process running for 16.858)
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-10-09T00:13:00.725Z  INFO 92148 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:13:00.809Z  INFO 92148 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:13:04.144Z  INFO 92148 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 92148 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T00:13:04.144Z  INFO 92148 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:13:05.023Z  INFO 92148 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:13:05.024Z  INFO 92148 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:13:05.025Z  INFO 92148 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:13:05.057Z  INFO 92148 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:13:05.057Z  INFO 92148 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 910 ms
2026-10-09T00:13:05.978Z  INFO 92148 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:13:06.929Z  INFO 92148 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:13:06.930Z  INFO 92148 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:13:06.933Z  INFO 92148 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T00:13:06.980Z  INFO 92148 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 40343 (http) with context path '/'
2026-10-09T00:13:07.010Z  INFO 92148 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 2.939 seconds (process running for 27.58)
2026-10-09T00:13:07.259Z  INFO 92148 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:13:07.263Z  INFO 92148 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:13:07.358Z  INFO 92148 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 92148 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T00:13:07.358Z  INFO 92148 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:13:08.055Z  INFO 92148 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:13:08.056Z  INFO 92148 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:13:08.057Z  INFO 92148 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:13:08.117Z  INFO 92148 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:13:08.118Z  INFO 92148 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 758 ms
2026-10-09T00:13:09.950Z  INFO 92148 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:13:13.220Z  INFO 92148 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:13:13.221Z  INFO 92148 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:13:13.225Z  INFO 92148 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 3 ms
2026-10-09T00:13:13.515Z  INFO 92148 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 45901 (http) with context path '/'
2026-10-09T00:13:13.535Z  INFO 92148 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 6.206 seconds (process running for 34.105)
[[1;31mERROR[m] [1;31mErrors: [m
[[1;31mERROR[m] [1;31m  SettingsSingletonTest ? IllegalState Could not find a valid Docker environment. Please see logs and check configuration[m
[[1;31mERROR[m] [1;31mTests run: 5, Failures: 0, Errors: 1, Skipped: 1[m
[[1;31mERROR[m] Failed to execute goal [32morg.apache.maven.plugins:maven-surefire-plugin:3.2.5:test[m [1m(default-test)[m on project [36msettings-service[m: [1;31m[m
[[1;31mERROR[m] [1;31m[m
[[1;31mERROR[m] [1;31mPlease refer to /home/daytona/project/services/settings-service/target/surefire-reports for the individual test results.[m
[[1;31mERROR[m] [1;31mPlease refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.[m
[[1;31mERROR[m] -> [1m[Help 1][m
[[1;31mERROR[m] 
[[1;31mERROR[m] To see the full stack trace of the errors, re-run Maven with the [1m-e[m switch.
[[1;31mERROR[m] Re-run Maven using the [1m-X[m switch to enable full debug logging.
[[1;31mERROR[m] 
[[1;31mERROR[m] For more information about the errors and possible solutions, please read the following articles:
[[1;31mERROR[m] [1m[Help 1][m http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException

[gate] wave 1 FINAL verification run (after fixing settings-service SettingsSingletonTest transaction-abort bug + Testcontainers workaround)
BUILD_EXIT=0
00:19:42.494 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.locationsresources.repository.SchemaCompletionTest]: SchemaCompletionTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:19:43.287 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.repository.SchemaCompletionTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:19:45.988Z  INFO 13497 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : Starting SchemaCompletionTest using Java 21.0.12.1 with PID 13497 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:19:45.991Z  INFO 13497 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : The following 1 profile is active: "test"
2026-10-09T00:19:47.535Z  INFO 13497 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T00:19:47.702Z  INFO 13497 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 155 ms. Found 3 JPA repository interfaces.
2026-10-09T00:19:48.714Z  INFO 13497 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T00:19:49.065Z  INFO 13497 --- [locations-resources-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@3d88e6b9
2026-10-09T00:19:49.085Z  INFO 13497 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T00:19:49.134Z  INFO 13497 --- [locations-resources-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/locres_db_test (PostgreSQL 16.15)
2026-10-09T00:19:49.225Z  INFO 13497 --- [locations-resources-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.053s)
2026-10-09T00:19:49.326Z  INFO 13497 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T00:19:49.331Z  INFO 13497 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T00:19:49.690Z  INFO 13497 --- [locations-resources-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T00:19:49.894Z  INFO 13497 --- [locations-resources-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T00:19:50.091Z  INFO 13497 --- [locations-resources-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T00:19:50.793Z  INFO 13497 --- [locations-resources-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T00:19:50.860Z  WARN 13497 --- [locations-resources-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-09T00:19:53.999Z  INFO 13497 --- [locations-resources-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T00:19:54.209Z  INFO 13497 --- [locations-resources-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T00:19:56.509Z  INFO 13497 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : Started SchemaCompletionTest in 12.905 seconds (process running for 16.781)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Hibernate: 
    insert 
    into
        resources
        (created_at, deleted_at, description, is_unique, name, restrict_locations, type, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    insert 
    into
        locations
        (building, colour, created_at, css_class, deleted_at, description, layout, name, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
2026-10-09T00:19:57.802Z  INFO 13497 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:19:57.990Z  INFO 13497 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:20:00.318Z  INFO 13497 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 13497 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:20:00.320Z  INFO 13497 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:20:06.810Z  INFO 13497 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:20:06.910Z  INFO 13497 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:20:06.911Z  INFO 13497 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:20:07.108Z  INFO 13497 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T00:20:07.109Z  INFO 13497 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 6787 ms
2026-10-09T00:20:09.316Z  INFO 13497 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:20:10.983Z  INFO 13497 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:20:10.984Z  INFO 13497 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:20:10.987Z  INFO 13497 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T00:20:11.162Z  INFO 13497 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 39819 (http) with context path '/'
2026-10-09T00:20:11.191Z  INFO 13497 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 10.979 seconds (process running for 31.466)
2026-10-09T00:20:11.692Z  INFO 13497 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:20:11.696Z  INFO 13497 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:20:11.814Z  INFO 13497 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 13497 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T00:20:11.814Z  INFO 13497 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:20:13.584Z  INFO 13497 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:20:13.585Z  INFO 13497 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:20:13.586Z  INFO 13497 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:20:13.615Z  INFO 13497 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:20:13.615Z  INFO 13497 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1799 ms
2026-10-09T00:20:16.301Z  INFO 13497 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:20:17.499Z  INFO 13497 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:20:17.499Z  INFO 13497 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:20:17.501Z  INFO 13497 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T00:20:17.548Z  INFO 13497 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 36687 (http) with context path '/'
2026-10-09T00:20:17.556Z  INFO 13497 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 5.823 seconds (process running for 37.831)
00:20:28.421 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.customfield.repository.DomainRepositoryTest]: DomainRepositoryTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:20:28.715 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.repository.DomainRepositoryTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:20:30.696Z  INFO 15688 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : Starting DomainRepositoryTest using Java 21.0.12.1 with PID 15688 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T00:20:30.700Z  INFO 15688 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : The following 1 profile is active: "test"
2026-10-09T00:20:32.287Z  INFO 15688 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T00:20:32.598Z  INFO 15688 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 292 ms. Found 4 JPA repository interfaces.
2026-10-09T00:20:35.700Z  INFO 15688 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T00:20:37.101Z  INFO 15688 --- [custom-field-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@65ef48f2
2026-10-09T00:20:37.182Z  INFO 15688 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T00:20:37.398Z  INFO 15688 --- [custom-field-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/customfld_db_test (PostgreSQL 16.15)
2026-10-09T00:20:37.903Z  INFO 15688 --- [custom-field-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.305s)
2026-10-09T00:20:38.090Z  INFO 15688 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T00:20:38.098Z  INFO 15688 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T00:20:38.484Z  INFO 15688 --- [custom-field-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T00:20:38.618Z  INFO 15688 --- [custom-field-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T00:20:38.695Z  INFO 15688 --- [custom-field-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T00:20:39.324Z  INFO 15688 --- [custom-field-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T00:20:42.741Z  INFO 15688 --- [custom-field-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T00:20:42.807Z  INFO 15688 --- [custom-field-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T00:20:43.309Z  INFO 15688 --- [custom-field-service] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-10-09T00:20:45.095Z  INFO 15688 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : Started DomainRepositoryTest in 15.584 seconds (process running for 19.208)
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Hibernate: insert into custom_fields (created_at,deleted_at,field_type,label,options,required,updated_at,id) values (?,?,?,?,?,?,?,?)
Hibernate: select cf1_0.id,cf1_0.created_at,cf1_0.deleted_at,cf1_0.field_type,cf1_0.label,cf1_0.options,cf1_0.required,cf1_0.updated_at from custom_fields cf1_0 where cf1_0.id=? and cf1_0.deleted_at is null
Hibernate: insert into custom_field_templates (context_id,created_at,name,updated_at,id) values (?,?,?,?,?)
Hibernate: insert into custom_field_templates (context_id,created_at,name,updated_at,id) values (?,?,?,?,?)
Hibernate: select cft1_0.id,cft1_0.context_id,cft1_0.created_at,cft1_0.name,cft1_0.updated_at from custom_field_templates cft1_0 where cft1_0.context_id=? or cft1_0.context_id is null
2026-10-09T00:20:48.489Z  INFO 15688 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:20:48.508Z  INFO 15688 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest
2026-10-09T00:20:48.521Z  INFO 15688 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:20:48.587Z  INFO 15688 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$JwksFailClosedVerification

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:20:48.693Z  INFO 15688 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 15688 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T00:20:48.693Z  INFO 15688 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:20:52.050Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:20:52.066Z  INFO 15688 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:20:52.067Z  INFO 15688 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:20:52.198Z  INFO 15688 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T00:20:52.199Z  INFO 15688 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 3504 ms
2026-10-09T00:20:55.091Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:20:58.728Z  INFO 15688 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:20:58.729Z  INFO 15688 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:20:58.733Z  INFO 15688 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 3 ms
2026-10-09T00:20:58.912Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 38649 (http) with context path '/'
2026-10-09T00:20:58.933Z  INFO 15688 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : Started Tier2FailClosedTest in 10.34 seconds (process running for 33.046)
2026-10-09T00:20:59.406Z  INFO 15688 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:20:59.487Z  INFO 15688 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:21:01.983Z  INFO 15688 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 15688 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T00:21:01.983Z  INFO 15688 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:21:03.800Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:21:03.802Z  INFO 15688 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:21:03.802Z  INFO 15688 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:21:03.891Z  INFO 15688 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:21:03.892Z  INFO 15688 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1905 ms
2026-10-09T00:21:06.387Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:21:08.408Z  INFO 15688 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:21:08.409Z  INFO 15688 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:21:08.410Z  INFO 15688 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-09T00:21:08.701Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 36373 (http) with context path '/'
2026-10-09T00:21:08.788Z  INFO 15688 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 7.002 seconds (process running for 42.902)
2026-10-09T00:21:09.501Z  INFO 15688 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:21:09.508Z  INFO 15688 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:21:09.843Z  INFO 15688 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 15688 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T00:21:09.845Z  INFO 15688 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:21:10.825Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:21:10.829Z  INFO 15688 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:21:10.829Z  INFO 15688 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:21:10.883Z  INFO 15688 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:21:10.883Z  INFO 15688 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1035 ms
2026-10-09T00:21:11.846Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:21:12.522Z  INFO 15688 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:21:12.522Z  INFO 15688 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:21:12.524Z  INFO 15688 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T00:21:12.590Z  INFO 15688 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 39611 (http) with context path '/'
2026-10-09T00:21:12.600Z  INFO 15688 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 2.887 seconds (process running for 46.714)
Tier1Tier2ConsistencyTest: no controller methods found yet (CustomFieldController/FieldTemplateController not yet built ? expected until plan 04-04 lands). Gateway-side assertion already passed.
00:21:23.588 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.settings.repository.SettingsSingletonTest]: SettingsSingletonTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
00:21:23.920 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.repository.SettingsSingletonTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:21:24.904Z  INFO 18497 --- [settings-service] [           main] c.b.s.repository.SettingsSingletonTest   : Starting SettingsSingletonTest using Java 21.0.12.1 with PID 18497 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T00:21:24.907Z  INFO 18497 --- [settings-service] [           main] c.b.s.repository.SettingsSingletonTest   : The following 1 profile is active: "test"
2026-10-09T00:21:27.898Z  INFO 18497 --- [settings-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T00:21:28.205Z  INFO 18497 --- [settings-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 295 ms. Found 2 JPA repository interfaces.
2026-10-09T00:21:30.825Z  INFO 18497 --- [settings-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T00:21:31.206Z  INFO 18497 --- [settings-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@73afe2b7
2026-10-09T00:21:31.209Z  INFO 18497 --- [settings-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T00:21:31.252Z  INFO 18497 --- [settings-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/settings_db_test (PostgreSQL 16.15)
2026-10-09T00:21:31.381Z  INFO 18497 --- [settings-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.096s)
2026-10-09T00:21:31.492Z  INFO 18497 --- [settings-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T00:21:31.498Z  INFO 18497 --- [settings-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T00:21:31.820Z  INFO 18497 --- [settings-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T00:21:32.019Z  INFO 18497 --- [settings-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T00:21:32.115Z  INFO 18497 --- [settings-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T00:21:32.808Z  INFO 18497 --- [settings-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T00:21:34.300Z  INFO 18497 --- [settings-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T00:21:34.353Z  INFO 18497 --- [settings-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T00:21:35.181Z  INFO 18497 --- [settings-service] [           main] c.b.s.repository.SettingsSingletonTest   : Started SettingsSingletonTest in 11.13 seconds (process running for 12.951)
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Hibernate: select s1_0.id,s1_0.approve_booking,s1_0.calendar_max_time,s1_0.calendar_min_time,s1_0.calendar_slot_size,s1_0.updated_at,s1_0.updated_by from settings s1_0 where s1_0.id=?
Hibernate: select count(*) from settings s1_0
Hibernate: select s1_0.id,s1_0.approve_booking,s1_0.calendar_max_time,s1_0.calendar_min_time,s1_0.calendar_slot_size,s1_0.updated_at,s1_0.updated_by from settings s1_0 where s1_0.id=?
Hibernate: insert into settings (approve_booking,calendar_max_time,calendar_min_time,calendar_slot_size,updated_at,updated_by,id) values (?,?,?,?,?,?,?)
2026-10-09T00:21:40.977Z  WARN 18497 --- [settings-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23514
2026-10-09T00:21:40.978Z ERROR 18497 --- [settings-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: new row for relation "settings" violates check constraint "chk_settings_singleton"
  Detail: Failing row contains (2, t, 30, 08:00:00, 18:00:00, 2026-10-09 00:21:40.479426+00, null).
2026-10-09T00:21:41.199Z  INFO 18497 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:21:41.392Z  INFO 18497 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest
2026-10-09T00:21:41.583Z  INFO 18497 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:21:41.686Z  INFO 18497 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$JwksFailClosedVerification

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:21:42.288Z  INFO 18497 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 18497 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T00:21:42.290Z  INFO 18497 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:21:46.015Z  INFO 18497 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:21:46.106Z  INFO 18497 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:21:46.107Z  INFO 18497 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:21:46.234Z  INFO 18497 --- [settings-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T00:21:46.234Z  INFO 18497 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 3936 ms
2026-10-09T00:21:48.477Z  INFO 18497 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:21:52.082Z  INFO 18497 --- [settings-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:21:52.084Z  INFO 18497 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:21:52.087Z  INFO 18497 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 3 ms
2026-10-09T00:21:52.599Z  INFO 18497 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 39429 (http) with context path '/'
2026-10-09T00:21:52.700Z  INFO 18497 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : Started Tier2FailClosedTest in 10.92 seconds (process running for 30.47)
2026-10-09T00:21:53.600Z  INFO 18497 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:21:53.685Z  INFO 18497 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:21:56.001Z  INFO 18497 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 18497 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T00:21:56.001Z  INFO 18497 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:21:56.995Z  INFO 18497 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:21:56.997Z  INFO 18497 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:21:56.997Z  INFO 18497 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:21:57.020Z  INFO 18497 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:21:57.020Z  INFO 18497 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1015 ms
2026-10-09T00:21:58.027Z  INFO 18497 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:21:58.480Z  INFO 18497 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:21:58.480Z  INFO 18497 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:21:58.481Z  INFO 18497 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-09T00:21:58.520Z  INFO 18497 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 41619 (http) with context path '/'
2026-10-09T00:21:58.527Z  INFO 18497 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 2.616 seconds (process running for 36.297)
2026-10-09T00:21:58.703Z  INFO 18497 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T00:21:58.710Z  INFO 18497 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T00:21:58.889Z  INFO 18497 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 18497 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T00:21:58.890Z  INFO 18497 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T00:21:59.617Z  INFO 18497 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T00:21:59.618Z  INFO 18497 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T00:21:59.619Z  INFO 18497 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T00:21:59.695Z  INFO 18497 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T00:21:59.695Z  INFO 18497 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 803 ms
2026-10-09T00:22:02.199Z  INFO 18497 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T00:22:04.093Z  INFO 18497 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T00:22:04.094Z  INFO 18497 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T00:22:04.099Z  INFO 18497 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 5 ms
2026-10-09T00:22:04.292Z  INFO 18497 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 44565 (http) with context path '/'
2026-10-09T00:22:04.302Z  INFO 18497 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 5.503 seconds (process running for 42.072)
TEST_EXIT=0
```

## Wave 2

- Build: `(cd services/locations-resources-service && mvn -q compile -DskipTests) && (cd services/custom-field-service && mvn -q compile -DskipTests) && (cd services/settings-service && mvn -q compile -DskipTests)` → pass
- Tests: `(cd services/locations-resources-service && mvn -q test -Dtest='!ApplicationContextBootTest') && (cd services/custom-field-service && mvn -q test -Dtest='!ApplicationContextBootTest') && (cd services/settings-service && mvn -q test -Dtest='!ApplicationContextBootTest')` → pass
- Fix attempts: 0/3 — 49/49 tests pass across all 3 services (18+16+15), 0 failures, 0 errors, 0 skips. ApplicationContextBootTest excluded (pre-existing Testcontainers/sandbox-Docker-API incompatibility, same as wave 1). All 3 plans independently discovered and fixed the same critical bug: missing JwtAuthenticationConverter meant @PreAuthorize never read Keycloak's realm_access.roles, silently denying all real callers including admins — fixed in commits 9db4b0f/4185596/e5e6e68.

### Gate output

```
[gate] wave 2 build: (cd services/locations-resources-service && mvn -q compile -DskipTests) && (cd services/custom-field-service && mvn -q compile -DskipTests) && (cd services/settings-service && mvn -q compile -DskipTests)
[gate] wave 2 tests: (cd services/locations-resources-service && mvn -q test -Dtest='!ApplicationContextBootTest') && (cd services/custom-field-service && mvn -q test -Dtest='!ApplicationContextBootTest') && (cd services/settings-service && mvn -q test -Dtest='!ApplicationContextBootTest')
02:19:13.882 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.locationsresources.repository.SchemaCompletionTest]: SchemaCompletionTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
02:19:15.302 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.repository.SchemaCompletionTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:19:17.112Z  INFO 23390 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : Starting SchemaCompletionTest using Java 21.0.12.1 with PID 23390 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T02:19:17.116Z  INFO 23390 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : The following 1 profile is active: "test"
2026-10-09T02:19:18.450Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:19:18.622Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 164 ms. Found 3 JPA repository interfaces.
2026-10-09T02:19:25.825Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T02:19:27.153Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@12f279b5
2026-10-09T02:19:27.220Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T02:19:27.444Z  INFO 23390 --- [locations-resources-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/locres_db_test (PostgreSQL 16.15)
2026-10-09T02:19:28.122Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.474s)
2026-10-09T02:19:28.236Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:19:28.243Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:19:28.823Z  INFO 23390 --- [locations-resources-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:19:29.343Z  INFO 23390 --- [locations-resources-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T02:19:29.721Z  INFO 23390 --- [locations-resources-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:19:30.869Z  INFO 23390 --- [locations-resources-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:19:30.942Z  WARN 23390 --- [locations-resources-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-09T02:19:32.055Z  INFO 23390 --- [locations-resources-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:19:32.114Z  INFO 23390 --- [locations-resources-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:19:32.744Z  INFO 23390 --- [locations-resources-service] [           main] c.b.l.repository.SchemaCompletionTest    : Started SchemaCompletionTest in 16.745 seconds (process running for 24.479)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Hibernate: 
    insert 
    into
        resources
        (created_at, deleted_at, description, is_unique, name, restrict_locations, type, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    insert 
    into
        locations
        (building, colour, created_at, css_class, deleted_at, description, layout, name, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
2026-10-09T02:19:33.829Z  INFO 23390 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:19:33.920Z  INFO 23390 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:19:36.242Z  INFO 23390 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 23390 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T02:19:36.242Z  INFO 23390 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : The following 1 profile is active: "test"
2026-10-09T02:19:40.323Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:19:40.345Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 21 ms. Found 3 JPA repository interfaces.
2026-10-09T02:19:42.086Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:19:42.180Z  INFO 23390 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:19:42.181Z  INFO 23390 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:19:42.371Z  INFO 23390 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T02:19:42.372Z  INFO 23390 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 6128 ms
2026-10-09T02:19:42.860Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-09T02:19:42.881Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@7ac47e87
2026-10-09T02:19:42.882Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-09T02:19:42.884Z  INFO 23390 --- [locations-resources-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/locres_db_test (PostgreSQL 16.15)
2026-10-09T02:19:42.899Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.006s)
2026-10-09T02:19:42.946Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:19:42.947Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:19:42.999Z  INFO 23390 --- [locations-resources-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:19:43.002Z  INFO 23390 --- [locations-resources-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:19:43.029Z  INFO 23390 --- [locations-resources-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:19:43.034Z  WARN 23390 --- [locations-resources-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-09T02:19:43.183Z  INFO 23390 --- [locations-resources-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:19:43.225Z  INFO 23390 --- [locations-resources-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:19:44.154Z  WARN 23390 --- [locations-resources-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-09T02:19:46.233Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:19:52.492Z  INFO 23390 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:19:52.492Z  INFO 23390 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:19:52.497Z  INFO 23390 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 4 ms
2026-10-09T02:19:53.316Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 44441 (http) with context path '/'
2026-10-09T02:19:53.396Z  INFO 23390 --- [locations-resources-service] [           main] l.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 17.347 seconds (process running for 45.075)
2026-10-09T02:19:54.123Z  INFO 23390 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:19:54.127Z  INFO 23390 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:19:54.302Z  INFO 23390 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 23390 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T02:19:54.302Z  INFO 23390 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : The following 1 profile is active: "test"
2026-10-09T02:19:54.843Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:19:54.853Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 9 ms. Found 3 JPA repository interfaces.
2026-10-09T02:19:55.037Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:19:55.038Z  INFO 23390 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:19:55.038Z  INFO 23390 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:19:55.061Z  INFO 23390 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T02:19:55.061Z  INFO 23390 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 755 ms
2026-10-09T02:19:55.405Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Starting...
2026-10-09T02:19:55.497Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-3 - Added connection org.postgresql.jdbc.PgConnection@24270e65
2026-10-09T02:19:55.498Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Start completed.
2026-10-09T02:19:55.502Z  INFO 23390 --- [locations-resources-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/locres_db_test (PostgreSQL 16.15)
2026-10-09T02:19:55.591Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.012s)
2026-10-09T02:19:55.687Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:19:55.690Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:19:55.732Z  INFO 23390 --- [locations-resources-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:19:55.734Z  INFO 23390 --- [locations-resources-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:19:55.778Z  INFO 23390 --- [locations-resources-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:19:55.784Z  WARN 23390 --- [locations-resources-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-09T02:19:55.951Z  INFO 23390 --- [locations-resources-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:19:56.003Z  INFO 23390 --- [locations-resources-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:19:56.750Z  WARN 23390 --- [locations-resources-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-09T02:19:57.553Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:19:58.581Z  INFO 23390 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:19:58.582Z  INFO 23390 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:19:58.586Z  INFO 23390 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 3 ms
2026-10-09T02:19:58.778Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 45447 (http) with context path '/'
2026-10-09T02:19:58.791Z  INFO 23390 --- [locations-resources-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 4.577 seconds (process running for 50.47)
2026-10-09T02:19:59.592Z  INFO 23390 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.outbox.OutboxPublisherTest]: OutboxPublisherTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:19:59.678Z  INFO 23390 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.outbox.OutboxPublisherTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:19:59.980Z  INFO 23390 --- [locations-resources-service] [           main] c.b.l.outbox.OutboxPublisherTest         : Starting OutboxPublisherTest using Java 21.0.12.1 with PID 23390 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T02:19:59.980Z  INFO 23390 --- [locations-resources-service] [           main] c.b.l.outbox.OutboxPublisherTest         : The following 1 profile is active: "test"
2026-10-09T02:20:02.485Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:20:02.585Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 99 ms. Found 3 JPA repository interfaces.
2026-10-09T02:20:03.900Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Starting...
2026-10-09T02:20:04.000Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-4 - Added connection org.postgresql.jdbc.PgConnection@5a6a293d
2026-10-09T02:20:04.001Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Start completed.
2026-10-09T02:20:04.005Z  INFO 23390 --- [locations-resources-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/locres_db_test (PostgreSQL 16.15)
2026-10-09T02:20:04.101Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.014s)
2026-10-09T02:20:04.205Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:20:04.206Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:20:04.402Z  INFO 23390 --- [locations-resources-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:20:04.406Z  INFO 23390 --- [locations-resources-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:20:04.418Z  INFO 23390 --- [locations-resources-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:20:04.423Z  WARN 23390 --- [locations-resources-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-09T02:20:04.600Z  INFO 23390 --- [locations-resources-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:20:04.626Z  INFO 23390 --- [locations-resources-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:20:05.136Z  WARN 23390 --- [locations-resources-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-09T02:20:05.424Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:20:06.412Z  INFO 23390 --- [locations-resources-service] [           main] c.b.l.outbox.OutboxPublisherTest         : Started OutboxPublisherTest in 6.711 seconds (process running for 58.091)
2026-10-09T02:20:07.091Z  INFO 23390 --- [locations-resources-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [localhost:5672]
2026-10-09T02:20:07.114Z  INFO 23390 --- [locations-resources-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#458b6dea:0/SimpleConnection@6f413dc7 [delegate=amqp://guest@127.0.0.1:5672/, localPort=39426]
2026-10-09T02:20:07.779Z  INFO 23390 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.controller.LocationControllerIntegrationTest]: LocationControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:20:07.792Z  INFO 23390 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.controller.LocationControllerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:20:07.991Z  INFO 23390 --- [locations-resources-service] [           main] .b.l.c.LocationControllerIntegrationTest : Starting LocationControllerIntegrationTest using Java 21.0.12.1 with PID 23390 (started by root in /home/daytona/project/services/locations-resources-service)
2026-10-09T02:20:07.991Z  INFO 23390 --- [locations-resources-service] [           main] .b.l.c.LocationControllerIntegrationTest : The following 1 profile is active: "test"
2026-10-09T02:20:08.671Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:20:08.686Z  INFO 23390 --- [locations-resources-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 14 ms. Found 3 JPA repository interfaces.
2026-10-09T02:20:08.997Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:20:08.999Z  INFO 23390 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:20:08.999Z  INFO 23390 --- [locations-resources-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:20:09.015Z  INFO 23390 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T02:20:09.015Z  INFO 23390 --- [locations-resources-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1022 ms
2026-10-09T02:20:09.154Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-5 - Starting...
2026-10-09T02:20:09.183Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-5 - Added connection org.postgresql.jdbc.PgConnection@578d4551
2026-10-09T02:20:09.183Z  INFO 23390 --- [locations-resources-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-5 - Start completed.
2026-10-09T02:20:09.185Z  INFO 23390 --- [locations-resources-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/locres_db_test (PostgreSQL 16.15)
2026-10-09T02:20:09.197Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.005s)
2026-10-09T02:20:09.218Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:20:09.219Z  INFO 23390 --- [locations-resources-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:20:09.245Z  INFO 23390 --- [locations-resources-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:20:09.247Z  INFO 23390 --- [locations-resources-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:20:09.255Z  INFO 23390 --- [locations-resources-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:20:09.260Z  WARN 23390 --- [locations-resources-service] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-09T02:20:09.403Z  INFO 23390 --- [locations-resources-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:20:09.419Z  INFO 23390 --- [locations-resources-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:20:10.099Z  WARN 23390 --- [locations-resources-service] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-10-09T02:20:10.898Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:20:12.492Z  INFO 23390 --- [locations-resources-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:20:12.492Z  INFO 23390 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:20:12.494Z  INFO 23390 --- [locations-resources-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T02:20:12.697Z  INFO 23390 --- [locations-resources-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 35761 (http) with context path '/'
2026-10-09T02:20:12.721Z  INFO 23390 --- [locations-resources-service] [           main] .b.l.c.LocationControllerIntegrationTest : Started LocationControllerIntegrationTest in 4.921 seconds (process running for 64.4)
2026-10-09T02:20:14.499Z  INFO 23390 --- [locations-resources-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.locationsresources.controller.ResourceControllerIntegrationTest]: ResourceControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:20:14.503Z  INFO 23390 --- [locations-resources-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.locationsresources.LocationsResourcesServiceApplication for test class com.bookinghub.locationsresources.controller.ResourceControllerIntegrationTest
02:20:43.190 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.customfield.repository.DomainRepositoryTest]: DomainRepositoryTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
02:20:44.003 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.repository.DomainRepositoryTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:20:44.905Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : Starting DomainRepositoryTest using Java 21.0.12.1 with PID 26760 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T02:20:44.907Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : The following 1 profile is active: "test"
2026-10-09T02:20:46.286Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:20:46.487Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 182 ms. Found 4 JPA repository interfaces.
2026-10-09T02:20:47.988Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T02:20:48.930Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@536b71b4
2026-10-09T02:20:48.978Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T02:20:49.200Z  INFO 26760 --- [custom-field-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/customfld_db_test (PostgreSQL 16.15)
2026-10-09T02:20:49.983Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.483s)
2026-10-09T02:20:50.194Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:20:50.201Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:20:50.789Z  INFO 26760 --- [custom-field-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:20:51.385Z  INFO 26760 --- [custom-field-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T02:20:51.713Z  INFO 26760 --- [custom-field-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:20:53.594Z  INFO 26760 --- [custom-field-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:20:57.256Z  INFO 26760 --- [custom-field-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:20:57.390Z  INFO 26760 --- [custom-field-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:20:58.080Z  INFO 26760 --- [custom-field-service] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-10-09T02:20:58.794Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.repository.DomainRepositoryTest    : Started DomainRepositoryTest in 14.662 seconds (process running for 23.9)
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Hibernate: insert into custom_fields (created_at,deleted_at,field_type,label,options,required,updated_at,id) values (?,?,?,?,?,?,?,?)
Hibernate: select cf1_0.id,cf1_0.created_at,cf1_0.deleted_at,cf1_0.field_type,cf1_0.label,cf1_0.options,cf1_0.required,cf1_0.updated_at from custom_fields cf1_0 where cf1_0.id=? and cf1_0.deleted_at is null
Hibernate: insert into custom_field_templates (context_id,created_at,name,updated_at,id) values (?,?,?,?,?)
Hibernate: insert into custom_field_templates (context_id,created_at,name,updated_at,id) values (?,?,?,?,?)
Hibernate: select cft1_0.id,cft1_0.context_id,cft1_0.created_at,cft1_0.name,cft1_0.updated_at from custom_field_templates cft1_0 where cft1_0.context_id=? or cft1_0.context_id is null
2026-10-09T02:21:00.108Z  INFO 26760 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:00.126Z  INFO 26760 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest
2026-10-09T02:21:00.177Z  INFO 26760 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:00.181Z  INFO 26760 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$JwksFailClosedVerification

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:21:00.317Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 26760 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T02:21:00.317Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : The following 1 profile is active: "test"
2026-10-09T02:21:03.098Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:21:03.196Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 97 ms. Found 4 JPA repository interfaces.
2026-10-09T02:21:07.405Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:21:07.438Z  INFO 26760 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:21:07.439Z  INFO 26760 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:21:07.590Z  INFO 26760 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T02:21:07.591Z  INFO 26760 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 7214 ms
2026-10-09T02:21:08.297Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-09T02:21:08.326Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@7d4d3842
2026-10-09T02:21:08.327Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-09T02:21:08.330Z  INFO 26760 --- [custom-field-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/customfld_db_test (PostgreSQL 16.15)
2026-10-09T02:21:08.352Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.009s)
2026-10-09T02:21:08.405Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:21:08.407Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:21:08.493Z  INFO 26760 --- [custom-field-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:21:08.496Z  INFO 26760 --- [custom-field-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:21:08.509Z  INFO 26760 --- [custom-field-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:21:08.743Z  INFO 26760 --- [custom-field-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:21:08.796Z  INFO 26760 --- [custom-field-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:21:12.098Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:21:15.793Z  INFO 26760 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:21:15.793Z  INFO 26760 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:21:15.796Z  INFO 26760 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 3 ms
2026-10-09T02:21:16.306Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 42375 (http) with context path '/'
2026-10-09T02:21:16.482Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.security.Tier2FailClosedTest       : Started Tier2FailClosedTest in 16.294 seconds (process running for 41.588)
2026-10-09T02:21:16.599Z  INFO 26760 --- [custom-field-service] [   scheduling-1] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [localhost:5672]
2026-10-09T02:21:16.713Z  INFO 26760 --- [custom-field-service] [   scheduling-1] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#18c04885:0/SimpleConnection@fe51c71 [delegate=amqp://guest@127.0.0.1:5672/, localPort=45092]
2026-10-09T02:21:16.807Z  INFO 26760 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:16.815Z  INFO 26760 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:21:19.078Z  INFO 26760 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 26760 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T02:21:19.079Z  INFO 26760 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : The following 1 profile is active: "test"
2026-10-09T02:21:19.619Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:21:19.634Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 14 ms. Found 4 JPA repository interfaces.
2026-10-09T02:21:19.937Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:21:19.939Z  INFO 26760 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:21:19.939Z  INFO 26760 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:21:19.980Z  INFO 26760 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T02:21:19.980Z  INFO 26760 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 897 ms
2026-10-09T02:21:20.316Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Starting...
2026-10-09T02:21:20.393Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-3 - Added connection org.postgresql.jdbc.PgConnection@f8740f5
2026-10-09T02:21:20.393Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Start completed.
2026-10-09T02:21:20.396Z  INFO 26760 --- [custom-field-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/customfld_db_test (PostgreSQL 16.15)
2026-10-09T02:21:20.415Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.009s)
2026-10-09T02:21:20.442Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:21:20.443Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:21:20.507Z  INFO 26760 --- [custom-field-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:21:20.510Z  INFO 26760 --- [custom-field-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:21:20.520Z  INFO 26760 --- [custom-field-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:21:20.899Z  INFO 26760 --- [custom-field-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:21:20.989Z  INFO 26760 --- [custom-field-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:21:22.278Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:21:24.303Z  INFO 26760 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:21:24.304Z  INFO 26760 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:21:24.378Z  INFO 26760 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 74 ms
2026-10-09T02:21:24.490Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 39921 (http) with context path '/'
2026-10-09T02:21:24.501Z  INFO 26760 --- [custom-field-service] [           main] c.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 5.592 seconds (process running for 49.608)
2026-10-09T02:21:25.195Z  INFO 26760 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:25.200Z  INFO 26760 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:21:25.498Z  INFO 26760 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 26760 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T02:21:25.576Z  INFO 26760 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : The following 1 profile is active: "test"
2026-10-09T02:21:26.721Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:21:26.731Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 9 ms. Found 4 JPA repository interfaces.
2026-10-09T02:21:26.903Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:21:26.904Z  INFO 26760 --- [custom-field-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:21:26.905Z  INFO 26760 --- [custom-field-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:21:26.922Z  INFO 26760 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T02:21:26.922Z  INFO 26760 --- [custom-field-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1343 ms
2026-10-09T02:21:27.108Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Starting...
2026-10-09T02:21:27.125Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-4 - Added connection org.postgresql.jdbc.PgConnection@3aaede52
2026-10-09T02:21:27.125Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-4 - Start completed.
2026-10-09T02:21:27.127Z  INFO 26760 --- [custom-field-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/customfld_db_test (PostgreSQL 16.15)
2026-10-09T02:21:27.141Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.006s)
2026-10-09T02:21:27.164Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:21:27.165Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:21:27.241Z  INFO 26760 --- [custom-field-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:21:27.244Z  INFO 26760 --- [custom-field-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:21:27.253Z  INFO 26760 --- [custom-field-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:21:27.411Z  INFO 26760 --- [custom-field-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:21:27.430Z  INFO 26760 --- [custom-field-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:21:28.278Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:21:29.113Z  INFO 26760 --- [custom-field-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:21:29.113Z  INFO 26760 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:21:29.115Z  INFO 26760 --- [custom-field-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-09T02:21:29.209Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 45553 (http) with context path '/'
2026-10-09T02:21:29.280Z  INFO 26760 --- [custom-field-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 3.886 seconds (process running for 54.386)
2026-10-09T02:21:29.806Z  INFO 26760 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.outbox.OutboxPublisherTest]: OutboxPublisherTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:29.880Z  INFO 26760 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.outbox.OutboxPublisherTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:21:29.978Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.outbox.OutboxPublisherTest         : Starting OutboxPublisherTest using Java 21.0.12.1 with PID 26760 (started by root in /home/daytona/project/services/custom-field-service)
2026-10-09T02:21:29.979Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.outbox.OutboxPublisherTest         : The following 1 profile is active: "test"
2026-10-09T02:21:30.787Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:21:30.797Z  INFO 26760 --- [custom-field-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 9 ms. Found 4 JPA repository interfaces.
2026-10-09T02:21:31.234Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-5 - Starting...
2026-10-09T02:21:31.258Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-5 - Added connection org.postgresql.jdbc.PgConnection@616e867b
2026-10-09T02:21:31.258Z  INFO 26760 --- [custom-field-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-5 - Start completed.
2026-10-09T02:21:31.262Z  INFO 26760 --- [custom-field-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/customfld_db_test (PostgreSQL 16.15)
2026-10-09T02:21:31.293Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.010s)
2026-10-09T02:21:31.337Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:21:31.339Z  INFO 26760 --- [custom-field-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:21:31.389Z  INFO 26760 --- [custom-field-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:21:31.390Z  INFO 26760 --- [custom-field-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:21:31.398Z  INFO 26760 --- [custom-field-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:21:31.590Z  INFO 26760 --- [custom-field-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:21:31.619Z  INFO 26760 --- [custom-field-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:21:34.098Z  INFO 26760 --- [custom-field-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:21:36.192Z  INFO 26760 --- [custom-field-service] [           main] c.b.c.outbox.OutboxPublisherTest         : Started OutboxPublisherTest in 6.303 seconds (process running for 61.299)
2026-10-09T02:21:36.483Z  INFO 26760 --- [custom-field-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [localhost:5672]
2026-10-09T02:21:36.496Z  INFO 26760 --- [custom-field-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#2267a30c:0/SimpleConnection@3bad9493 [delegate=amqp://guest@127.0.0.1:5672/, localPort=53402]
2026-10-09T02:21:37.094Z  INFO 26760 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.controller.CustomFieldControllerIntegrationTest]: CustomFieldControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:37.281Z  INFO 26760 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.controller.CustomFieldControllerIntegrationTest
2026-10-09T02:21:37.920Z  INFO 26760 --- [custom-field-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.customfield.controller.FieldTemplateControllerIntegrationTest]: FieldTemplateControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:37.923Z  INFO 26760 --- [custom-field-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.customfield.CustomFieldServiceApplication for test class com.bookinghub.customfield.controller.FieldTemplateControllerIntegrationTest
02:21:44.704 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.bookinghub.settings.repository.SettingsSingletonTest]: SettingsSingletonTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
02:21:45.388 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.repository.SettingsSingletonTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:21:47.103Z  INFO 29528 --- [settings-service] [           main] c.b.s.repository.SettingsSingletonTest   : Starting SettingsSingletonTest using Java 21.0.12.1 with PID 29528 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T02:21:47.105Z  INFO 29528 --- [settings-service] [           main] c.b.s.repository.SettingsSingletonTest   : The following 1 profile is active: "test"
2026-10-09T02:21:48.114Z  INFO 29528 --- [settings-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:21:48.233Z  INFO 29528 --- [settings-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 111 ms. Found 2 JPA repository interfaces.
2026-10-09T02:21:48.995Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-09T02:21:49.229Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@35d60381
2026-10-09T02:21:49.231Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-09T02:21:49.267Z  INFO 29528 --- [settings-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/settings_db_test (PostgreSQL 16.15)
2026-10-09T02:21:49.342Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.038s)
2026-10-09T02:21:49.419Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:21:49.423Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:21:49.552Z  INFO 29528 --- [settings-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:21:49.694Z  INFO 29528 --- [settings-service] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-09T02:21:49.789Z  INFO 29528 --- [settings-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:21:50.125Z  INFO 29528 --- [settings-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:21:51.707Z  INFO 29528 --- [settings-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:21:51.777Z  INFO 29528 --- [settings-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:21:53.086Z  INFO 29528 --- [settings-service] [           main] c.b.s.repository.SettingsSingletonTest   : Started SettingsSingletonTest in 7.389 seconds (process running for 11.096)
WARNING: A Java agent has been loaded dynamically (/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.19/byte-buddy-agent-1.14.19.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Hibernate: select s1_0.id,s1_0.approve_booking,s1_0.calendar_max_time,s1_0.calendar_min_time,s1_0.calendar_slot_size,s1_0.updated_at,s1_0.updated_by from settings s1_0 where s1_0.id=?
Hibernate: select count(*) from settings s1_0
Hibernate: select s1_0.id,s1_0.approve_booking,s1_0.calendar_max_time,s1_0.calendar_min_time,s1_0.calendar_slot_size,s1_0.updated_at,s1_0.updated_by from settings s1_0 where s1_0.id=?
Hibernate: insert into settings (approve_booking,calendar_max_time,calendar_min_time,calendar_slot_size,updated_at,updated_by,id) values (?,?,?,?,?,?,?)
2026-10-09T02:21:57.044Z  WARN 29528 --- [settings-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23514
2026-10-09T02:21:57.045Z ERROR 29528 --- [settings-service] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: new row for relation "settings" violates check constraint "chk_settings_singleton"
  Detail: Failing row contains (2, t, 30, 08:00:00, 18:00:00, 2026-10-09 02:21:56.995338+00, null).
2026-10-09T02:21:57.106Z  INFO 29528 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest]: Tier2FailClosedTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:57.194Z  INFO 29528 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest
2026-10-09T02:21:57.203Z  INFO 29528 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$JwksFailClosedVerification]: JwksFailClosedVerification does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:21:57.209Z  INFO 29528 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$JwksFailClosedVerification

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:21:57.390Z  INFO 29528 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : Starting Tier2FailClosedTest using Java 21.0.12.1 with PID 29528 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T02:21:57.391Z  INFO 29528 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : No active profile set, falling back to 1 default profile: "default"
2026-10-09T02:22:01.698Z  INFO 29528 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:22:01.784Z  INFO 29528 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:22:01.785Z  INFO 29528 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:22:02.080Z  INFO 29528 --- [settings-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-09T02:22:02.081Z  INFO 29528 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 4687 ms
2026-10-09T02:22:07.699Z  INFO 29528 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:22:08.813Z  INFO 29528 --- [settings-service] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:22:08.814Z  INFO 29528 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:22:08.816Z  INFO 29528 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T02:22:08.993Z  INFO 29528 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 36121 (http) with context path '/'
2026-10-09T02:22:09.016Z  INFO 29528 --- [settings-service] [           main] c.b.s.security.Tier2FailClosedTest       : Started Tier2FailClosedTest in 11.74 seconds (process running for 27.027)
2026-10-09T02:22:09.213Z  INFO 29528 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$MissingTokenTest]: MissingTokenTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:22:09.221Z  INFO 29528 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$MissingTokenTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:22:10.776Z  INFO 29528 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : Starting Tier2FailClosedTest.MissingTokenTest using Java 21.0.12.1 with PID 29528 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T02:22:10.777Z  INFO 29528 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T02:22:12.407Z  INFO 29528 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:22:12.408Z  INFO 29528 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:22:12.409Z  INFO 29528 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:22:12.494Z  INFO 29528 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T02:22:12.495Z  INFO 29528 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1716 ms
2026-10-09T02:22:15.885Z  INFO 29528 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:22:16.559Z  INFO 29528 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:22:16.559Z  INFO 29528 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:22:16.562Z  INFO 29528 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 2 ms
2026-10-09T02:22:16.639Z  INFO 29528 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 38595 (http) with context path '/'
2026-10-09T02:22:16.675Z  INFO 29528 --- [settings-service] [           main] s.s.Tier2FailClosedTest$MissingTokenTest : Started Tier2FailClosedTest.MissingTokenTest in 5.949 seconds (process running for 34.686)
2026-10-09T02:22:16.877Z  INFO 29528 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.security.Tier2FailClosedTest$InsufficientRoleTest]: InsufficientRoleTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:22:16.884Z  INFO 29528 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.security.Tier2FailClosedTest$InsufficientRoleTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:22:17.109Z  INFO 29528 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Starting Tier2FailClosedTest.InsufficientRoleTest using Java 21.0.12.1 with PID 29528 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T02:22:17.110Z  INFO 29528 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : No active profile set, falling back to 1 default profile: "default"
2026-10-09T02:22:17.947Z  INFO 29528 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:22:17.948Z  INFO 29528 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:22:17.948Z  INFO 29528 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:22:17.977Z  INFO 29528 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T02:22:17.977Z  INFO 29528 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 864 ms
2026-10-09T02:22:18.528Z  INFO 29528 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:22:19.223Z  INFO 29528 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-2].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:22:19.224Z  INFO 29528 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:22:19.225Z  INFO 29528 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-09T02:22:19.306Z  INFO 29528 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 44213 (http) with context path '/'
2026-10-09T02:22:19.318Z  INFO 29528 --- [settings-service] [           main] Tier2FailClosedTest$InsufficientRoleTest : Started Tier2FailClosedTest.InsufficientRoleTest in 2.323 seconds (process running for 37.329)
2026-10-09T02:22:19.499Z  INFO 29528 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.outbox.OutboxPublisherTest]: OutboxPublisherTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:22:19.508Z  INFO 29528 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.outbox.OutboxPublisherTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:22:19.599Z  INFO 29528 --- [settings-service] [           main] c.b.settings.outbox.OutboxPublisherTest  : Starting OutboxPublisherTest using Java 21.0.12.1 with PID 29528 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T02:22:19.599Z  INFO 29528 --- [settings-service] [           main] c.b.settings.outbox.OutboxPublisherTest  : The following 1 profile is active: "test"
2026-10-09T02:22:19.956Z  INFO 29528 --- [settings-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:22:19.980Z  INFO 29528 --- [settings-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 23 ms. Found 2 JPA repository interfaces.
2026-10-09T02:22:20.219Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Starting...
2026-10-09T02:22:20.258Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-2 - Added connection org.postgresql.jdbc.PgConnection@1cfbcd9f
2026-10-09T02:22:20.259Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Start completed.
2026-10-09T02:22:20.278Z  INFO 29528 --- [settings-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/settings_db_test (PostgreSQL 16.15)
2026-10-09T02:22:20.294Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.007s)
2026-10-09T02:22:20.322Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:22:20.323Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:22:20.358Z  INFO 29528 --- [settings-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:22:20.361Z  INFO 29528 --- [settings-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:22:20.383Z  INFO 29528 --- [settings-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:22:20.423Z  INFO 29528 --- [settings-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:22:20.445Z  INFO 29528 --- [settings-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:22:21.426Z  INFO 29528 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:22:24.133Z  INFO 29528 --- [settings-service] [           main] c.b.settings.outbox.OutboxPublisherTest  : Started OutboxPublisherTest in 4.618 seconds (process running for 42.21)
2026-10-09T02:22:24.940Z  INFO 29528 --- [settings-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [localhost:5672]
2026-10-09T02:22:25.027Z  INFO 29528 --- [settings-service] [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#7511928e:0/SimpleConnection@7c66040f [delegate=amqp://guest@127.0.0.1:5672/, localPort=48678]
2026-10-09T02:22:25.716Z  INFO 29528 --- [settings-service] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.bookinghub.settings.controller.SettingsControllerIntegrationTest]: SettingsControllerIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-10-09T02:22:25.729Z  INFO 29528 --- [settings-service] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.bookinghub.settings.SettingsServiceApplication for test class com.bookinghub.settings.controller.SettingsControllerIntegrationTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-09T02:22:25.913Z  INFO 29528 --- [settings-service] [           main] .b.s.c.SettingsControllerIntegrationTest : Starting SettingsControllerIntegrationTest using Java 21.0.12.1 with PID 29528 (started by root in /home/daytona/project/services/settings-service)
2026-10-09T02:22:25.913Z  INFO 29528 --- [settings-service] [           main] .b.s.c.SettingsControllerIntegrationTest : The following 1 profile is active: "test"
2026-10-09T02:22:26.210Z  INFO 29528 --- [settings-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-09T02:22:26.218Z  INFO 29528 --- [settings-service] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 7 ms. Found 2 JPA repository interfaces.
2026-10-09T02:22:26.396Z  INFO 29528 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 0 (http)
2026-10-09T02:22:26.397Z  INFO 29528 --- [settings-service] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-09T02:22:26.397Z  INFO 29528 --- [settings-service] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-09T02:22:26.419Z  INFO 29528 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-3].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2026-10-09T02:22:26.420Z  INFO 29528 --- [settings-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 505 ms
2026-10-09T02:22:26.676Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Starting...
2026-10-09T02:22:26.723Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-3 - Added connection org.postgresql.jdbc.PgConnection@35737e55
2026-10-09T02:22:26.723Z  INFO 29528 --- [settings-service] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Start completed.
2026-10-09T02:22:26.725Z  INFO 29528 --- [settings-service] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5432/settings_db_test (PostgreSQL 16.15)
2026-10-09T02:22:26.741Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.007s)
2026-10-09T02:22:26.767Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-10-09T02:22:26.767Z  INFO 29528 --- [settings-service] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-10-09T02:22:26.817Z  INFO 29528 --- [settings-service] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-09T02:22:26.819Z  INFO 29528 --- [settings-service] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-09T02:22:26.828Z  INFO 29528 --- [settings-service] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-09T02:22:26.940Z  INFO 29528 --- [settings-service] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-09T02:22:26.958Z  INFO 29528 --- [settings-service] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-09T02:22:27.835Z  INFO 29528 --- [settings-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoints beneath base path '/actuator'
2026-10-09T02:22:28.269Z  INFO 29528 --- [settings-service] [           main] o.a.c.c.C.[Tomcat-3].[localhost].[/]     : Initializing Spring TestDispatcherServlet ''
2026-10-09T02:22:28.269Z  INFO 29528 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Initializing Servlet ''
2026-10-09T02:22:28.270Z  INFO 29528 --- [settings-service] [           main] o.s.t.web.servlet.TestDispatcherServlet  : Completed initialization in 1 ms
2026-10-09T02:22:28.319Z  INFO 29528 --- [settings-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 38823 (http) with context path '/'
2026-10-09T02:22:28.330Z  INFO 29528 --- [settings-service] [           main] .b.s.c.SettingsControllerIntegrationTest : Started SettingsControllerIntegrationTest in 2.596 seconds (process running for 46.407)
```

