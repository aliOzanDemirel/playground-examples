plugins {
    kotlin("jvm") version "1.6.0"
}

dependencies {

    implementation(project(":business-logic"))

    // slf4j-api already exists
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

    implementation("io.jooby:jooby-jetty:2.10.0")
    implementation("io.insert-koin:koin-core:3.1.2")

    implementation("io.jooby:jooby-jackson:2.10.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
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