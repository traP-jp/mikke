plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    application
}

group = "jp.trap"
version = "1.0.0"

application {
    mainClass.set("jp.trap.mikke.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    // Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)

    // Kotlinx
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Logging
    implementation(libs.logback.classic)
    implementation(libs.koin.logger.slf4j)

    // Database
    runtimeOnly(libs.mariadb)

    // Test
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit5)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}
