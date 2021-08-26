package config

import io.gatling.http.Predef.HttpHeaderNames

object Configuration {

  val TARGET_URL = "http://localhost:8080"
  val JSON_HEADER = Map(HttpHeaderNames.ContentType -> "application/json", HttpHeaderNames.Accept -> "application/json")
  val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36"
}
