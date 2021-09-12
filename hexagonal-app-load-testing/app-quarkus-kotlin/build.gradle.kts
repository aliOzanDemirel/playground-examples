plugins {

    kotlin("jvm") version "1.5.21"
    kotlin("plugin.allopen") version "1.5.21"
    id("io.quarkus") version "2.2.3.Final"
}

dependencies {

    implementation(project(":business-logic"))
    implementation("org.jboss.slf4j:slf4j-jboss-logmanager:1.1.0.Final")

    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:2.2.2.Final"))
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-resteasy-mutiny")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-hibernate-validator")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

allOpen {

    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
}