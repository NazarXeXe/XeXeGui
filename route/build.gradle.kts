plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "me.nazarxexe"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    
}

dependencies {
    api(project(":core"))
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")

    testImplementation(kotlin("test"))
}

afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class.java) {
                from(components["kotlin"])
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}