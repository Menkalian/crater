plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("org.jetbrains.dokka")

    `maven-publish`
}

kotlin {
    val ktorVersion = "1.6.7"
    fun ktor(module: String) = "io.ktor:ktor-$module:$ktorVersion"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(ktor("client-core"))
                implementation(ktor("client-serialization"))
                implementation(ktor("client-logging"))
                implementation(ktor("client-websockets"))

                implementation(project(":shared-data"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(ktor("client-js"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(ktor("client-cio"))
                implementation(ktor("client-cio"))
                api("org.slf4j:slf4j-api:1.7.36")
                implementation("org.slf4j:slf4j-api:1.7.36")

                api(ktor("client-core"))
                api(ktor("client-serialization"))
                api(ktor("client-logging"))
                api(ktor("client-websockets"))

                api(project(":shared-data"))
            }
        }
        val nativeMain by getting {
            dependencies {
                implementation(ktor("client-curl"))
            }
        }
    }
}
