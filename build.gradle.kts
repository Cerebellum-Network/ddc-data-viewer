plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.allopen") version "1.4.20"

    id("io.quarkus") version "1.11.3.Final"

    id("com.avast.gradle.docker-compose") version "0.14.0"

    idea
}

group = "network.cere"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Quarkus
    implementation(enforcedPlatform("io.quarkus:quarkus-universe-bom:1.11.3.Final"))
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-smallrye-health")

    // Web
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-resteasy-reactive-qute")
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client")

    // Crypto
    implementation("com.rfksystems:blake2b:1.0.0")
    implementation("com.google.crypto.tink:tink:1.5.0")

    // Logging
    implementation("io.quarkus:quarkus-logging-json")

    // Kotlin
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Build
    implementation("io.quarkus:quarkus-container-image-jib")
    implementation("io.quarkus:quarkus-arc")

    // Tests
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:kotlin-extensions")
    testImplementation("org.awaitility:awaitility-kotlin:4.0.3")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = JavaVersion.VERSION_11.toString()
            javaParameters = true
            useIR = true
        }
    }

    withType<Test> {
        dependsOn(composeUp)
        finalizedBy(composeDown)
        useJUnitPlatform()
    }
}

dockerCompose {
    useComposeFiles = listOf("docker-compose/docker-compose.yaml")
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("javax.ws.rs.Path")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

idea.module {
    isDownloadJavadoc = true
    isDownloadSources = true
}
