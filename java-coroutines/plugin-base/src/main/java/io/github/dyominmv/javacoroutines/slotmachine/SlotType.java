package io.github.dyominmv.javacoroutines.slotmachine;

import io.github.dyominmv.javacoroutines.Utils;

import java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo;
import java.lang.constant.ClassDesc;

import static io.github.dyominmv.javacoroutines.slotmachine.SlotType.SimpleSlot.*;
import static io.github.dyominmv.javacoroutines.slotmachine.SlotType.UninitializedSlot.UNINITIALIZED;

sealed interface SlotType {
    enum SimpleSlot implements SlotType { TOP, INTEGER, FLOAT, DOUBLE, LONG, NULL, }
    enum UninitializedSlot implements SlotType { UNINITIALIZED, }
    record Reference(ClassDesc classDesc) implements SlotType {
        public Reference(Class<?> aClass) { this(Utils.desc(aClass)); }
    }

    static SlotType of(VerificationTypeInfo verificationTypeInfo) {
        return switch (verificationTypeInfo) {
            case SimpleVerificationTypeInfo.TOP -> TOP;
            case SimpleVerificationTypeInfo.INTEGER -> INTEGER;
            case SimpleVerificationTypeInfo.FLOAT -> FLOAT;
            case SimpleVerificationTypeInfo.DOUBLE -> DOUBLE;
            case SimpleVerificationTypeInfo.LONG -> LONG;
            case SimpleVerificationTypeInfo.NULL -> NULL;
            case SimpleVerificationTypeInfo.UNINITIALIZED_THIS -> UNINITIALIZED;
            case UninitializedVerificationTypeInfo _ -> UNINITIALIZED;
            case ObjectVerificationTypeInfo objectTypeInfo -> new Reference(objectTypeInfo.classSymbol());
        };
    }
}
