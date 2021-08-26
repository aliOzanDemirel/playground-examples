package test

import config.Configuration
import io.gatling.core.Predef.{constantUsersPerSec, nothingFor, _}
import io.gatling.http.Predef._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class IoBoundSimulation extends Simulation {

  val httpProtocol = http.baseUrl(Configuration.TARGET_URL)
    .acceptHeader("application/json")
    .userAgentHeader(Configuration.USER_AGENT)

  val scn = scenario("IO Bound Scenario")
    .exec(
      http("io_bound_wait_1")
        .get("/io")
        .headers(Configuration.JSON_HEADER)
        .check(jsonPath("$.success").exists)
        .check(status.is(200))
    )
    .pause(FiniteDuration.apply(1, TimeUnit.SECONDS))
    .exec(
      http("io_bound_wait_2")
        .get("/io?duration=2000")
        .headers(Configuration.JSON_HEADER)
        .check(jsonPath("$.success").exists)
        .check(status.is(200))
    )

  val scnInjection = scn.inject(
    atOnceUsers(10),
    nothingFor(2),
    rampUsers(200) during 5,
    nothingFor(2),
    constantUsersPerSec(100) during 10,
    nothingFor(1),
    constantUsersPerSec(160) during 10
  )
  setUp(scnInjection).protocols(httpProtocol)
}