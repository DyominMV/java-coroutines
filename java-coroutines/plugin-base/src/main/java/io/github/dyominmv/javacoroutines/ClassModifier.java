package io.github.dyominmv.javacoroutines;

import io.github.dyominmv.javacoroutines.transformations.TransformCoroutines;

import java.lang.classfile.*;

public class ClassModifier {

    public boolean shouldModify(ClassModel classModel) {
        if (isAlreadyModified(classModel)) return false;

        for (var method: classModel.methods())
            if (CoroutineChecker.isCoroutine(method)) return true;

        return false;
    }

    private boolean isAlreadyModified(ClassModel classModel) {
        var annotationsAttribute = classModel.findAttribute(Attributes.runtimeVisibleAnnotations()).orElse(null);
        if (null == annotationsAttribute) return false;

        for (var annotation : annotationsAttribute.annotations())
            if (Utils.desc(AlreadyModified.class).equals(annotation.classSymbol())) return true;

        return false;
    }

    public byte[] modifyClass(ClassModel classModel) {
        return ClassFile.of().transformClass(classModel, new TransformCoroutines());
    }

}
