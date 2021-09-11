plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("org.jetbrains.kotlin.kapt") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.micronaut.application") version "2.0.4"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.21"
}

dependencies {

    implementation(project(":business-logic"))

    kapt("io.micronaut:micronaut-http-validation")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("javax.annotation:javax.annotation-api")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "16"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "16"
        }
    }
}

application {
    mainClass.set("app.MainKt")
}

micronaut {
    version("3.0.1")
    runtime("netty")
    processing {
        incremental(false)
        annotations("app.*")
    }
}