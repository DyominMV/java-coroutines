package io.github.dyominmv.javacoroutines.slotmachine;

import io.github.dyominmv.javacoroutines.CoroutinesTransformationException;
import io.github.dyominmv.javacoroutines.Utils;

import java.lang.constant.ClassDesc;

import static io.github.dyominmv.javacoroutines.slotmachine.SlotType.SimpleSlot.*;

sealed interface SlotSpan {
    int slots();

    record SingleSlotSpan(SlotType slotType) implements SlotSpan {
        @Override public int slots() { return 1; }
    }
    record DoubleSlotSpan(SlotType firstSlot, SlotType secondSlot) implements SlotSpan {
        @Override public int slots() { return 2; }
    }

    static SlotSpan forDescriptor(ClassDesc classDesc) {
        if (Utils.desc(int.class).equals(classDesc)) return new SingleSlotSpan(INTEGER);
        if (Utils.desc(char.class).equals(classDesc)) return new SingleSlotSpan(INTEGER);
        if (Utils.desc(byte.class).equals(classDesc)) return new SingleSlotSpan(INTEGER);
        if (Utils.desc(short.class).equals(classDesc)) return new SingleSlotSpan(INTEGER);
        if (Utils.desc(boolean.class).equals(classDesc)) return new SingleSlotSpan(INTEGER);
        if (Utils.desc(float.class).equals(classDesc)) return new SingleSlotSpan(FLOAT);
        if (Utils.desc(double.class).equals(classDesc)) return new DoubleSlotSpan(DOUBLE, TOP);
        if (Utils.desc(long.class).equals(classDesc)) return new DoubleSlotSpan(LONG, TOP);

        if (classDesc.isPrimitive())
            throw new CoroutinesTransformationException("Descriptor " + classDesc + " expected to be non-primitive");

        return new SingleSlotSpan(new Reference(classDesc));
    }

}
