Usage:

in `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal() 
        mavenCentral()
    }
}
```

in `build.gradle.kts`
```kotlin
plugins {
    java
    id("io.github.dyominmv.java-coroutines-gradle-plugin").version("1.0.0")
}

val transformCoroutines by tasks.register<TransformCoroutines>("transformCoroutines") {
   dependsOn(tasks.compileJava)
   classDirectories = tasks.compileJava.flatMap { it.destinationDirectory }.map { listOf(it) }
}

tasks.jar { dependsOn(transformCoroutines) }

val transformTestCoroutines by tasks.register<TransformCoroutines>("transformTestCoroutines") {
   dependsOn(tasks.compileTestJava)
   classDirectories = tasks.compileTestJava.flatMap { it.destinationDirectory }.map { listOf(it) }
}

tasks.test { dependsOn(transformTestCoroutines) }
```

in your java class
```java
class Example {
    public Coroutine<String> tripleHello(String name) {
        for (int i = 0; i < 3; i += 1) {
            System.out.println("Hello " + name + "!");
            suspend();
        }
        
        return result("Hello logged successfully"); 
    }

   static void main() {
      var coroutine = tripleHello("user"); // nothing is printed
      coroutine.proceed(); // Hello user ! (once)
      assert false == coroutine.finished();
      var result = coroutine.proceedUntilFinished(); // Hello user ! (two more times)
      assert "Hello logged successfully".equals(result);
    }
}
```