import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val ktor_version: String by project

plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "ru.leadpogrommer.vk22"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

application {
    mainClass.set("ru.leadpogrommer.vk22.tester.MainKt")
}

