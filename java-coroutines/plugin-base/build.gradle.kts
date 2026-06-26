plugins {
    `java-library`
    id("java-coroutines-project-config")
}

dependencies {
    implementation(project(":runtime"))
}

publishing {
    publications {
        create<MavenPublication>("java-coroutines-plugin-base") {
            from(components["java"])
            pom {
                description = "base for creating plugins to generate suspendable java methods (aka coroutines)"
            }
        }
    }
}