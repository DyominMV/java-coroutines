package io.github.dyominmv.javacoroutines;

import io.github.dyominmv.javacoroutines.transformations.TransformCoroutines;

import java.lang.classfile.*;

import static io.github.dyominmv.javacoroutines.Utils.desc;
import static io.github.dyominmv.javacoroutines.Utils.isCoroutine;

public class ClassModifier {

    public boolean shouldModify(ClassModel classModel) {
        if (isAlreadyModified(classModel)) return false;

        for (var method: classModel.methods())
            if (isCoroutine(method)) return true;

        return false;
    }

    private boolean isAlreadyModified(ClassModel classModel) {
        var annotationsAttribute = classModel.findAttribute(Attributes.runtimeVisibleAnnotations()).orElse(null);
        if (null == annotationsAttribute) return false;

        for (var annotation : annotationsAttribute.annotations())
            if (desc(AlreadyModified.class).equals(annotation.classSymbol())) return true;

        return false;
    }

    public byte[] modifyClass(ClassModel classModel) {
        return ClassFile.of().transformClass(classModel, new TransformCoroutines());
    }

}
