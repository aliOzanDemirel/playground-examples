## Load Testing Hexagonal Applications

This is a demonstration project for trying out different web frameworks while keeping the [business-logic](#business-logic) same. There is not really
any domain logic in the module, it is an example of how this concern can be isolated from application/framework binding code.

Modules prefixed with `app-` are applications that use common `business-logic`, all these _apps_ have same API (HTTP carrying JSON bodies) contract
and validation rules, they are only built by using different web frameworks around same common logic module. There are probably countless other
frameworks to try (like http4k, lagom, spark, ninja, blade etc.) than the ones in this project, these are merely selected out of curiosity.

- GET /io endpoint, to simulate IO bound task
    - non-mandatory query parameter `duration`, defaults to `1000` if not provided
    - responds with JSON body that has boolean property `success`
- POST /cpu endpoint, to simulate CPU bound task
    - JSON request body with mandatory property `inputs` as list of strings, cannot be empty
    - responds with JSON body that has number properties `nanoseconds`, `milliseconds` and `seconds`

#### business-logic

This is the reusable business logic module that is framework/library independent. It is not exactly the core domain logic in DDD as it encapsulates
both the application layer logic and the domain layer logic, so all use cases and orchestration logic lies in this module without further separation
into core domain. In pure DDD you would have rich domain logic that is business agnostic so that any application with different business logic can use
it as common domain module.

This module does not have dependencies other than API to log through and unit tests. This structure is especially beneficial for unit testing since it
is very easy to test core logic by avoiding lots of unnecessary unit test noise when logic is mingled with application code that includes framework
configuration, logging, external dependencies etc.

#### gatling-runner

This is the module to be able to run basic [gatling](https://gatling.io/docs/gatling/reference/current/general/simulation_setup/) load test scenarios
through gradle wrapper, to have some primitive comparison between application modules (running load testing client within same host as target
application is wrong anyway).

- Run some server on `localhost:8080` as target application, and then execute gatling scenarios with `gatlingRun`:
    - Only CPU scenario: `gradlew --rerun-tasks gatlingRun-test.CpuBoundSimulation`
    - Only IO scenario: `gradlew --rerun-tasks gatlingRun-test.IoBoundSimulation`

### Applications

##### app-ratpack-groovy

This example is built by Ratpack/Guice/Groovy. [Ratpack](https://ratpack.io/manual/current/async.html) is non-blocking framework with its own
constructs (guaranteed ordering in execution model) for async handling and request processing, framework runs on netty server.

- Compiling and running this application requires java runtime less than 16 because groovy compilation fails with Java 16+ and upgrading groovy
  version (3+) is not supported within used ratpack version.
    - Use explicit java runtime when running with gradle: `gradlew -Dorg.gradle.java.home="%JAVA_11%" app-ratpack-groovy:run`
- Run the application directly through gradle, enable debug too: `gradlew app-ratpack-groovy:run -Dapp.log.debugEnabled=true`
- Run with main class by creating fat jar first:
    - `gradlew app-ratpack-groovy:clean app-ratpack-groovy:jar`
    - `java -Dapp.log.debugEnabled=true -Dapp.log.httpRequest=true -jar app.jar`

##### app-spring-kotlin

This example is built by spring boot and kotlin, there are API handlers for both reactive stack (webflux and coroutine) and servlet stack (webmvc).

- Run with gradle wrapper: `gradlew app-spring-kotlin:bootRun --args='--spring.profiles.active=coroutine,--kotlinx.coroutines.debug'`
    - [CoroutineApi](app-spring-kotlin/src/main/kotlin/app/api/CoroutineApi.kt) is enabled by spring profile `coroutine`
    - [WebfluxApi](app-spring-kotlin/src/main/kotlin/app/api/WebfluxApi.kt) is enabled by spring profile `webflux`
    - [WebmvcApi](app-spring-kotlin/src/main/kotlin/app/api/WebmvcApi.kt) is enabled by spring profile `webmvc`
- Build executable jar: `gradlew app-spring-kotlin:clean app-spring-kotlin:bootJar`

##### app-jooby-kotlin

Built by kotlin, [jooby](https://jooby.io/#introduction) and [koin](https://insert-koin.io/), embedded jetty is used in classpath, 'worker' execution
mode is configured while running the application.

- Generate and run fat jar: `gradlew app-jooby-kotlin:clean app-jooby-kotlin:jar`
    - Run with non-blocking io processing with coroutines: `java -DthreadPerRequest=false -jar app.jar`
    - Run with thread-per-request jetty workers: `java -jar app.jar`

##### app-ktor-kotlin

Built with kotlin and [ktor](https://ktor.io/docs/a-ktor-application.html), by using coroutine io (CIO) engine within ktor. Ktor is native kotlin
framework with superb coroutine support, to the level of reading&writing http bodies.

- Generate and run fat jar:
    - `gradlew app-ktor-kotlin:clean app-ktor-kotlin:jar`
    - `java -jar app.jar`

##### app-micronaut-kotlin

Built with kotlin and [micronaut](https://docs.micronaut.io/latest/guide/#reactiveServer), uses netty server by default, with classic golden rule _do
not block event loop_. Kotlin coroutine integration is used to simulate non-blocking execution in synchronous fashion. DI and AOP concerns (
transaction, validation etc.) are resolved at compile time with AoT, so the startup is faster and less memory intensive thanks to not having
reflection scanning, plus this can be further improved by native compilation.

- Run with gradle wrapper: `gradlew app-micronaut-kotlin:run`
- Build executable fat jar: `gradlew app-micronaut-kotlin:clean app-micronaut-kotlin:shadowJar`

##### app-quarkus-kotlin

Built with kotlin and [quarkus](https://quarkus.io/get-started/), uses vertx for underlying reactive implementation and has its own reactive streams
implementation: [mutiny](https://smallrye.io/smallrye-mutiny/). Default configuration has worker thread pool for request replying executor. There is
also newer [resteasy-reactive](https://quarkus.io/guides/resteasy-reactive) implementation that replaces the classic one, changing the default
executor to event loop pool. Quarkus -just like Micronaut- uses AoT for fast startup and supports native compilation through its plugin.

- Property `enableCoroutine` configures IO endpoint to execute non-blocking sequential code, default behaviour is async reactive stream that delays.
- Run with gradle wrapper: `gradlew app-quarkus-kotlin:quarkusDev -DenableCoroutine=true`
- Generate jar as production build and run it:
    - `gradlew app-quarkus-kotlin:clean app-quarkus-kotlin:quarkusBuild`
    - `java -jar build/quarkus-app/quarkus-run.jar`

##### app-javalin-kotlin

Built by kotlin, [javalin](https://javalin.io/documentation) and [koin](https://insert-koin.io/), it is running on embedded jetty server, default
configuration is thread-per-request model.

- Run by creating fat jar:
    - `gradlew app-javalin-kotlin:clean app-javalin-kotlin:jar`
    - `java -jar app.jar`

##### app-vertx-java

Built by java and [vertx-web](https://vertx.io/docs/vertx-web/java/#_basic_vert_x_web_concepts), running on netty server with event loop responding
requests by default. It is possible to use [vertx-sync](https://vertx.io/docs/vertx-sync/java/) to write non-blocking code synchronously, it uses
[quasar fibers](https://docs.paralleluniverse.co/quasar) (coroutine) but quasar is abandoned so this integration library is deprecated and ultimately
unnecessary considering the upcoming [project loom](https://wiki.openjdk.java.net/display/loom).

- `enableBlockingIo` parameter configures worker thread per request instead of event loop, run with gradle wrapper:
  `gradlew app-vertx-java:clean app-vertx-java:vertxRun -DenableBlockingIo=true`

##### app-dropwizard-java

Built by java and [dropwizard](https://www.dropwizard.io/en/latest/manual/core.html), jetty server is used within dropwizard-core.

- Run with gradle wrapper: `gradlew app-dropwizard-java:clean app-dropwizard-java:run`

##### app-helidon-java

Built by java and [helidon mp](https://helidon.io/docs/v2/#/mp/introduction/02_microprofile), built on top of netty web server as an implementation of
microprofile specification.

- Run with gradle wrapper: `gradlew app-helidon-java:clean app-helidon-java:run`

##### app-play-scala

Built by scala and [play framework](https://www.playframework.com/documentation/2.8.x/Home), this version of play uses akka-http as web server, serves
incoming requests in non-blocking fashion.

- Run with gradle wrapper: `gradlew app-play-scala:clean app-play-scala:runPlay`
