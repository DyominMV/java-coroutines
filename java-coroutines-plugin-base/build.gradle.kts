plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.dyominmv"
version = "1.0.0"

repositories {
    mavenLocal()
}

dependencies {
    implementation("$group:java-coroutines:$version")
}

publishing {
    publications {
        create<MavenPublication>("java-coroutines-plugin-base") {
            from(components["java"])
        }
    }
}