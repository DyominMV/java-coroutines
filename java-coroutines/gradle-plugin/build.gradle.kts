plugins {
    `java-gradle-plugin`
    id("java-coroutines-project-config")
}

dependencies {
    implementation(project(":plugin-base"))
}

val defaultDescription = "Gradle plugin to create suspendable java methods (aka coroutines)"

gradlePlugin {
    plugins {
        register("java-coroutines-gradle-plugin") {
            id = "io.github.dyominmv.java-coroutines-gradle-plugin"
            displayName = "java-coroutines-gradle-plugin"
            implementationClass = "io.github.dyominmv.javacoroutines.JavaCoroutinesGradlePlugin"
            description = defaultDescription
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "java-coroutines-gradle-plugin"
            pom {
                name = this@withType.artifactId
                description = defaultDescription
            }
        }
    }
}
