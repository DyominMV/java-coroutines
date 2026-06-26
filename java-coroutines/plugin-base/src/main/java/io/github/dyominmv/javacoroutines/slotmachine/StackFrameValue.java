package io.github.dyominmv.javacoroutines.slotmachine;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

public sealed interface StackFrameValue {
    sealed interface NonStorable extends StackFrameValue {
        enum Null implements NonStorable { NULL, }
        enum Split implements NonStorable { DOUBLE_FIRST, TOP, LONG_FIRST, }
    }

    sealed interface Storable extends StackFrameValue {
        record Reference(ClassDesc classDesc) implements Storable {}
        enum ShortPrimitive implements Storable { INTEGER, FLOAT, }
        enum LongPrimitive implements Storable { DOUBLE, LONG, }

        default Class<?> upperBound() {
            return switch (this) {
                case LongPrimitive.DOUBLE   -> double.class;
                case LongPrimitive.LONG     -> long.class;
                case ShortPrimitive.FLOAT   -> float.class;
                case ShortPrimitive.INTEGER -> int.class;
                case Reference _            -> Object.class;
            };
        }

        default TypeKind typeKind() {
            return switch (this) {
                case LongPrimitive.DOUBLE   -> TypeKind.DOUBLE;
                case LongPrimitive.LONG     -> TypeKind.LONG;
                case ShortPrimitive.FLOAT   -> TypeKind.FLOAT;
                case ShortPrimitive.INTEGER -> TypeKind.INT;
                case Reference _            -> TypeKind.REFERENCE;
            };
        }
    }

    default int slots() { return (this instanceof Storable.LongPrimitive) ? 2 : 1; }
}
