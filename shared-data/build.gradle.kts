plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("org.jetbrains.dokka")

    `maven-publish`

    id("de.menkalian.vela.keygen")
}

keygen {
    create("default") {
        generator.set(de.menkalian.vela.gradle.KeygenExtension.Generator.KOTLIN)
        sourceDir.set(File(projectDir.absolutePath + "src/commonMain/keygen"))
    }
}

kotlin {
}

afterEvaluate {
    // Build fat jar, since transitive dependencies do not work properly at the moment
    tasks.getByName("jvmJar", Jar::class) {
        from(
            configurations
                .getByName("jvmRuntimeClasspath")
                .map { if (it.isDirectory) it else zipTree(it) }
        )
    }

    publishing {
    }
}
