package test

import config.Configuration
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class CpuBoundSimulation extends Simulation {

  val httpProtocol = http.baseUrl(Configuration.TARGET_URL)
    .acceptHeader("application/json")
    .userAgentHeader(Configuration.USER_AGENT)

  val scn = scenario("CPU Bound Scenario")
    .exec(
      http("cpu_bound_small_payload")
        .post("/cpu")
        .headers(Configuration.JSON_HEADER)
        .body(RawFileBody("request/small_payload.json"))
        .check(jsonPath("$.nanoseconds").exists)
        .check(status.is(202))
    )
    .pause(1)
    .exec(
      http("cpu_bound_big_payload")
        .post("/cpu")
        .headers(Configuration.JSON_HEADER)
        .body(RawFileBody("request/big_payload.json"))
        .check(jsonPath("$.nanoseconds").exists)
        .check(status.is(202))
    )

  val scnInjection = scn.inject(
    atOnceUsers(10),
    nothingFor(2),
    rampUsers(400) during 10,
    nothingFor(2),
    constantUsersPerSec(60) during 10,
    nothingFor(2),
    incrementUsersPerSec(10)
      times 2
      eachLevelLasting 10
      separatedByRampsLasting 5
  )
  val throttledInjection = scnInjection.throttle(
    reachRps(800) in FiniteDuration.apply(20, TimeUnit.SECONDS),
    holdFor(10),
    jumpToRps(200),
    holdFor(10)
  )
  setUp(throttledInjection).protocols(httpProtocol)
}