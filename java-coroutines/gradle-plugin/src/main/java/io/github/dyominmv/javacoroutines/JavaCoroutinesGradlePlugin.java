package io.github.dyominmv.javacoroutines;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public class JavaCoroutinesGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(@NonNull Project project) {
        project.getDependencies().add("implementation", "io.github.dyominmv:java-coroutines:1.0.0");
    }
}