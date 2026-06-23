package io.github.dyominmv.javacoroutines;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JavaCoroutines {

    private final ClassModifier modifier = new ClassModifier();
    private final List<Path> modifiableClassRoots;

    public JavaCoroutines(List<Path> modifiableClassRoots) {
        this.modifiableClassRoots = modifiableClassRoots;
    }

    public void execute() throws IOException {
        for (Path root : modifiableClassRoots)
            try (var paths = Files.walk(root)) {
                var iterator = paths.iterator();
                while (iterator.hasNext()) modifyFileIfNeeded(iterator.next());
            }
    }

    private void modifyFileIfNeeded(Path file) throws IOException {
        if (!Files.exists(file) || !Files.isRegularFile(file) || !".class".equals(getExtension(file))) return;

        var classModel = ClassFile.of().parse(file);
        if (!modifier.shouldModify(classModel)) return;

        Files.write(file, modifier.modifyClass(classModel));
    }

    private String getExtension(Path file) {
        String name = file.toFile().getName();
        return name.substring(name.lastIndexOf('.'));
    }

}
