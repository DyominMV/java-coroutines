package io.github.dyominmv.javacoroutines;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/// Transform classes that contain coroutine-methods
///
/// applies following transformations:
/// 1. class is annotated with `@AlreadyModified`
/// 2. every method that returns `Coroutine<...>` AND contains call to `Coroutine.suspend` or `Coroutine.result` is
/// replaced by two methods:
///    1. one has similar signature and is called "coroutine handle". Calling this method created coroutine.
///    2. other is static and its type is `Continuation -> Object`. It is called "coroutine body". It contains modified
/// body of initial method.
///
/// "Coroutine handle" contains all annotations of initial method. Code of "coroutine handle" includes following:
/// 1. store all arguments of a method (including `this`, if any) into a continuation
/// 2. create and return `new CoroutineImpl(continuation, <this class>::<coroutine body>)`
///
/// "Coroutine body" consists from:
/// 1. block of jump instructions that specifies where to continue execution from depending on
/// `Continuation.suspensionPoint`
/// 2. code from initial method, where `Coroutine.suspend()` are replaced by following set of actions:
///    1. store operand stack and local variables into continuation
///    2. assign number of current suspension point to `Continuation.suspensionPoint`
///    3. return null
///    4. place label that corresponds to current suspension point number
///    5. load operand stack and local variables from continuation
public class JavaCoroutines {

    private final ClassModifier modifier = new ClassModifier();
    private final List<Path> modifiableClassRoots;

    /// creates an object to execute transformations of classes.
    ///
    /// @param modifiableClassRoots specifies directories that contain class-files to be transformed.
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
