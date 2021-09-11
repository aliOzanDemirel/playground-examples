plugins {
    scala
    id("org.gradle.playframework") version "0.12"
}

val versions = mapOf("play" to "2.8.8", "scala" to "2.13.6")

tasks.getByName<org.gradle.playframework.tasks.PlayRun>("runPlay") {
    play.platform {
        javaVersion.set(JavaVersion.VERSION_16)
        scalaVersion.set(versions["scala"])
        playVersion.set(versions["play"])
    }
    httpPort.set(8080)
    forkOptions.memoryMaximumSize = "2G"
}

tasks.compilePlayRoutes {
    injectedRoutesGenerator.set(true)
}

dependencies {

    implementation(project(":business-logic"))

    implementation("com.typesafe.play:play_2.13:${versions["play"]}")
    implementation("com.typesafe.play:play-guice_2.13:${versions["play"]}")
    implementation("com.typesafe.play:play-logback_2.13:${versions["play"]}")

    implementation("org.scala-lang:scala-library:${versions["scala"]}")
}