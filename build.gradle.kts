plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.2.3"
    kotlin("plugin.serialization") version "1.8.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.2.3")
    implementation("io.ktor:ktor-server-netty:2.2.3")
    implementation("io.ktor:ktor-websockets:2.2.3")
    implementation("org.jetbrains.exposed:exposed-core:0.39.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.39.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.39.2")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.example.MainKt") // Make sure this matches the package and file name
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17" // Make sure this matches your Java version
    }
}
