plugins {
    `java-library`
    id("java-coroutines-project-config")
}

publishing {
    publications {
        create<MavenPublication>("java-coroutines") {
            from(components["java"])
            pom {
                description = "runtime for java coroutines created by java-coroutines-gradle-plugin"
            }
        }
    }
}