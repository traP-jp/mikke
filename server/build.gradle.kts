plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.ktlint)
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
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auto.head.response)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.hsts)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.metrics)
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

    // Utils
    implementation(libs.uuid.creator)
}

val specFile = rootProject.file("../openapi/openapi.yaml")
val generatedOpenApiDir = layout.buildDirectory.dir("generated/openapi")

openApiGenerate {
    generatorName.set("kotlin-server")
    library.set("ktor")
    configOptions.set(
        mapOf(
            "serializationLibrary" to "kotlinx-serialization",
            "serializableModel" to "true",
            "enumPropertyNaming" to "UPPERCASE",
        ),
    )

    inputSpec.set(specFile.toURI().toString())

    outputDir.set(generatedOpenApiDir.map { it.asFile.absolutePath })

    typeMappings.set(
        mapOf(
            "UUID" to "kotlin.uuid.Uuid",
            "uuid" to "kotlin.uuid.Uuid",
            "string+date-time" to "kotlin.time.Instant",
        ),
    )

    packageName.set("jp.trap.mikke.openapi")
}

sourceSets.main {
    kotlin.srcDir(generatedOpenApiDir.map { it.dir("src/main/kotlin") })
    resources {
        srcDir(specFile.parentFile)
    }
}

tasks.build {
    dependsOn(tasks.openApiGenerate)
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

tasks.matching { it.name == "kspKotlin" }.configureEach {
    dependsOn(tasks.openApiGenerate)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
    jvmToolchain(24)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    filter {
        exclude { it.file.path.contains("generated") }
    }
}
