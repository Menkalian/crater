import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    id("org.jetbrains.dokka")

    id("de.menkalian.vela.keygen")

    `maven-publish`
}

springBoot {
    buildInfo()
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation(project(":shared-data"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    val ktorVersion = "1.6.7"
    fun ktor(module: String) = "io.ktor:ktor-$module:$ktorVersion"
    implementation(ktor("server-cio"))
    implementation(ktor("websockets"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")

    // Database Drivers
    runtimeOnly("org.xerial:sqlite-jdbc:3.36.0.3")
    runtimeOnly("org.postgresql:postgresql:42.3.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

keygen {
    create("default") {
        targetPackage.set("de.menkalian.crater.variables")
        finalLayerAsString.set(true)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.jar)
            artifact(tasks.kotlinSourcesJar)
            artifact(tasks.bootJar)
        }
    }
}

tasks.bootJar.configure {
    archiveClassifier.set("boot")
}
