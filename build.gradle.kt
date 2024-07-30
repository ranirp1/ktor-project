plugins {
    kotlin("jvm") version "1.8.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-auth:2.3.0")
    implementation("io.ktor:ktor-auth-jwt:2.3.0")
    implementation("io.ktor:ktor-websockets:2.3.0")
    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.40.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
    implementation("com.h2database:h2:2.1.212")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.0")
}

application {
    mainClass.set("MainKt")
}
