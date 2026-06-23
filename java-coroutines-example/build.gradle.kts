import io.github.dyominmv.javacoroutines.TransformCoroutines

plugins {
    java
    id("io.github.dyominmv.java-coroutines-gradle-plugin").version("1.0.0")
}

version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.0")
}


tasks.compileTestJava {
    outputs.upToDateWhen { false }
}

val transformTestCoroutines by tasks.register<TransformCoroutines>("transformTestCoroutines") {
    dependsOn(tasks.compileTestJava)
    classDirectories = tasks.compileTestJava.flatMap { it.destinationDirectory }.map { listOf(it) }
    outputs.upToDateWhen { false }
}

tasks.test {
    dependsOn(transformTestCoroutines)
    useJUnitPlatform()
}