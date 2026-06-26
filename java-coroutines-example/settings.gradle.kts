pluginManagement {
    repositories {
        mavenLocal()
        maven(uri("file:///${System.getProperty("user.home")}/local-repository"))
        gradlePluginPortal()
        mavenCentral()
    }
}