plugins {
    kotlin("jvm") version "1.5.21"
}

val ktorVersion = "1.6.3"

dependencies {

    implementation(project(":business-logic"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.6")

    implementation(kotlin("stdlib"))
}

tasks.getByName<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "app.MainKt"
    }
    archiveFileName.set("app.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}