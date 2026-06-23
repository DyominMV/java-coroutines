package io.github.dyominmv.javacoroutines;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.OutputDirectories;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public abstract class TransformCoroutines extends DefaultTask {
    @TaskAction
    public void execute() throws IOException {
        var modifiableClassRoots = new ArrayList<Path>();
        for (var directory: getClassDirectories().get())
            modifiableClassRoots.add(directory.getAsFile().getAbsoluteFile().toPath());
        new JavaCoroutines(modifiableClassRoots).execute();
    }

    @OutputDirectories
    public abstract ListProperty<Directory> getClassDirectories();
}
