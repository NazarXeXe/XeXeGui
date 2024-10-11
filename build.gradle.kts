plugins {
    kotlin("jvm") version "2.0.20"
}

group = "me.nazarxexe"
version = "1.0-SNAPSHOT"

subprojects {
    apply {
        plugin("maven-publish")
    }
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = this@subprojects.group as String
                artifactId = this@subprojects.name
                version = this@subprojects.version as String
            }
        }
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