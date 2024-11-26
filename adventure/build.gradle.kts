
plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "me.nazarxexe.ui"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

}

dependencies {
    api(project(":core"))
    api("net.kyori:adventure-api:4.17.0")
    api("net.kyori:adventure-text-minimessage:4.17.0")


    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            version = project.version as String
            artifactId = project.name
            from(components["kotlin"])
        }
    }
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}