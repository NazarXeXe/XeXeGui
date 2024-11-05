plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "XeXeGUI"
include("core")
include("tests")
include("adventure")
include("route")
include("pagination")
include("coroutine")
include("placeholderapi")
include("data")
