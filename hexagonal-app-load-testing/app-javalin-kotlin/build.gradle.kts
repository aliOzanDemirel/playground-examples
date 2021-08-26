plugins {
    kotlin("jvm") version "1.5.21"
}

dependencies {

    implementation(project(":business-logic"))

    // slf4j-api comes from business-logic module
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

    implementation("io.javalin:javalin:3.13.11")
    implementation("io.insert-koin:koin-core:3.1.2")

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")
}

tasks.getByName<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "app.MainKt"
    }
    archiveFileName.set("app.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}