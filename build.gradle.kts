plugins {
    kotlin("jvm") version "2.0.20"
}

group = "me.nazarxexe"
version = "1.0-SNAPSHOT"

subprojects {
    apply {
        plugin("maven-publish")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}