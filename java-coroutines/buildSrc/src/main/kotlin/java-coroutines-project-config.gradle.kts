plugins {
    `maven-publish`
    java
    signing
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

publishing {
    repositories {
        maven {
            url = uri("file:///${System.getProperty("user.home")}/local-repository")
            name = "dev"
        }
    }
    publications {
        withType<MavenPublication> {
            artifactId = name
            groupId = rootProject.group.toString()
            pom {
                licenses {
                    license {
                        name = "MIT License"
                        url = "http://www.opensource.org/licenses/mit-license.php"
                    }
                }
                developers {
                    developer {
                        name = "Mikhail Dyomin"
                        email = "m.v.dyomin@mail.ru"
                        organizationUrl = "https://github.com/DyominMV"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/DyominMV/java-coroutines.git"
                    developerConnection = "scm:git:ssh://github.com:DyominMV/java-coroutines.git"
                    url = "https://github.com/DyominMV/java-coroutines/tree/master"
                }
                url = "https://github.com/DyominMV/java-coroutines/tree/master/"
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}