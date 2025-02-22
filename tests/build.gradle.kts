plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.3"

}

group = "me.nazarxexe.ui"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    api(project(":core"))
    api(project(":adventure"))
    api(project(":route"))
    api(project(":pagination"))
    api(project(":coroutine"))
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
    implementation("io.ktor:ktor-client-cio-jvm:3.0.0")

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.0")
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-kotlin-coroutines-annotations:2.0.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}