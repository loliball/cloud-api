import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maven-publish")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.4.30"
    kotlin("jvm") version "1.6.10"
    application
}

group = "loli.ball"
version = "1.0-SNAPSHOT"

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.9.3")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    api(project(":core"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}