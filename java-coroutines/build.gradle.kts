plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.dyominmv"
version = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("java-coroutines") {
            from(components["java"])
        }
    }
}