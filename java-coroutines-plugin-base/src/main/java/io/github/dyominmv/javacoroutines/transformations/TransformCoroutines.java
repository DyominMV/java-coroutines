package io.github.dyominmv.javacoroutines.transformations;

import io.github.dyominmv.javacoroutines.AlreadyModified;
import io.github.dyominmv.javacoroutines.Continuation;
import io.github.dyominmv.javacoroutines.slotmachine.SlotMachine;

import java.lang.classfile.*;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DirectMethodHandleDesc.Kind;
import java.lang.constant.MethodHandleDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.Collections;

import static io.github.dyominmv.javacoroutines.CoroutineChecker.isCoroutine;
import static io.github.dyominmv.javacoroutines.Utils.desc;
import static io.github.dyominmv.javacoroutines.Utils.methodDesc;

public class TransformCoroutines implements ClassTransform {
    @Override
    public void accept(ClassBuilder builder, ClassElement element) {
        if (element instanceof MethodModel method && isCoroutine(method))
            transformCoroutine(builder, method);
        else if (element instanceof RuntimeVisibleAnnotationsAttribute annotationsAttribute)
            addModifiedAnnotation(builder, annotationsAttribute);
        else
            builder.accept(element);
    }

    private void addModifiedAnnotation(ClassBuilder builder, RuntimeVisibleAnnotationsAttribute annotations) {
        var modifiedAnnotations = new ArrayList<>(annotations.annotations());
        modifiedAnnotations.add(Annotation.of(desc(AlreadyModified.class)));
        builder.accept(RuntimeVisibleAnnotationsAttribute.of(modifiedAnnotations));
    }

    private void transformCoroutine(ClassBuilder builder, MethodModel coroutineMethod) {
        addCoroutineBody(builder, coroutineMethod);
        builder.transformMethod(coroutineMethod, new AddCoroutineHandle(coroutineMethod));
    }

    private void addCoroutineBody(ClassBuilder classBuilder, MethodModel coroutineMethod) {
        var code = new BuildCoroutineBody(createSlotMachine(coroutineMethod), coroutineMethod.code().orElseThrow());
        classBuilder.withMethodBody(
                getBodyName(coroutineMethod),
                methodDesc(Object.class, Continuation.class),
                AccessFlag.PRIVATE.mask() | AccessFlag.STATIC.mask(),
                code
        );
    }

    private class AddCoroutineHandle implements MethodTransform {
        private final MethodModel coroutineMethod;

        private AddCoroutineHandle(MethodModel coroutineMethod) { this.coroutineMethod = coroutineMethod; }

        @Override
        public void accept(MethodBuilder builder, MethodElement element) {
            if (!(element instanceof CodeModel)) builder.accept(element);
        }

        @Override
        public void atEnd(MethodBuilder builder) {
            var code = new BuildCoroutineHandle(createSlotMachine(coroutineMethod), getBodyDesc(coroutineMethod));
            builder.withCode(code);
        }
    }

    private SlotMachine createSlotMachine(MethodModel coroutineMethod) {
        var instanceDesc = coroutineMethod.flags().has(AccessFlag.STATIC) ? null : getOwner(coroutineMethod);

        var stackMapAttribute = coroutineMethod.code().orElseThrow()
                .findAttribute(Attributes.stackMapTable()).orElse(null);
        var stackMaps = stackMapAttribute != null
                ? stackMapAttribute.entries()
                : Collections.<StackMapFrameInfo>emptyList();

        return new SlotMachine(stackMaps, instanceDesc, coroutineMethod.methodTypeSymbol().parameterList());
    }

    private DirectMethodHandleDesc getBodyDesc(MethodModel coroutineMethod) {
        return MethodHandleDesc.ofMethod(
                Kind.STATIC,
                getOwner(coroutineMethod),
                getBodyName(coroutineMethod),
                methodDesc(Object.class, Continuation.class)
        );
    }

    private ClassDesc getOwner(MethodModel coroutineMethod) {
        return coroutineMethod.parent().orElseThrow().thisClass().asSymbol();
    }

    private String getBodyName(MethodModel coroutineMethod) {
        return coroutineMethod.methodName().stringValue() + COROUTINE_BODY_SUFFIX;
    }

    private static final String COROUTINE_BODY_SUFFIX = "$coroutineBody";

}

