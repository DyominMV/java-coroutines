plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.dyominmv"
version = "1.0.0"

repositories {
    mavenLocal()
}

dependencies {
    implementation("$group:java-coroutines-plugin-base:$version")
}

gradlePlugin {
    plugins {
        register("io.github.dyominmv.java-coroutines-gradle-plugin") {
            id = "io.github.dyominmv.java-coroutines-gradle-plugin"
            implementationClass = "io.github.dyominmv.javacoroutines.JavaCoroutinesGradlePlugin"
        }
    }
}