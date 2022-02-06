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
    implementation(project(":core"))
    api("com.squareup.okhttp3:okhttp:4.9.3")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation ("com.google.api-client:google-api-client:1.33.1")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.33.0")
    implementation ("com.google.apis:google-api-services-drive:v3-rev20220110-1.32.1")
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