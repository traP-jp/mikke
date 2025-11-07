import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
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
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.sessions)
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
    implementation(libs.hikaricp)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)
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
val generatedTraqClientDir = layout.buildDirectory.dir("generated/traq-client")

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

val downloadApiSpecTask =
    tasks.register("downloadApiSpec") {
        val url = "https://raw.githubusercontent.com/traPtitech/traQ/master/docs/v3-api.yaml"
        val outputFile = layout.buildDirectory.file("downloaded-specs/v3-api.yaml")

        outputs.file(outputFile)

        doLast {
            val targetFile = outputFile.get().asFile
            targetFile.parentFile.mkdirs()

            URI.create(url).toURL().openStream().use { input ->
                Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

tasks.register<GenerateTask>("generateTraqClient") {
    dependsOn(downloadApiSpecTask)

    generatorName.set("kotlin")
    library.set("multiplatform")
    globalProperties.set(
        mapOf(
            "apis" to "User,Me,Message,Channel,File,Stamp,Webhook,Oauth2",
            "models" to "",
            "supportingFiles" to "",
        ),
    )
    additionalProperties.set(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "enumPropertyNaming" to "UPPERCASE",
        ),
    )

    inputSpec.set("${layout.buildDirectory.dir("downloaded-specs").get().asFile.absolutePath}/v3-api.yaml")

    outputDir.set(generatedTraqClientDir.map { it.asFile.absolutePath })

    typeMappings.set(
        mapOf(
            "UUID" to "kotlin.uuid.Uuid",
            "uuid" to "kotlin.uuid.Uuid",
            "string+date-time" to "Instant",
        ),
    )

    packageName.set("jp.trap.mikke.traq.client")
}

sourceSets.main {
    kotlin.srcDir(generatedOpenApiDir.map { it.dir("src/main/kotlin") })
    kotlin.srcDir(generatedTraqClientDir.map { it.dir("src/commonMain/kotlin") })
    resources {
        srcDir(specFile.parentFile)
    }
}

tasks.build {
    dependsOn(tasks.openApiGenerate)
    dependsOn(tasks.named("generateTraqClient"))
    dependsOn(tasks.shadowJar)
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
    dependsOn(tasks.named("generateTraqClient"))
}

tasks.matching { it.name == "kspKotlin" }.configureEach {
    dependsOn(tasks.openApiGenerate)
    dependsOn(tasks.named("generateTraqClient"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-opt-in=io.ktor.utils.io.InternalAPI")
    }
    jvmToolchain(24)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    filter {
        exclude { it.file.path.contains("generated") }
    }
}
