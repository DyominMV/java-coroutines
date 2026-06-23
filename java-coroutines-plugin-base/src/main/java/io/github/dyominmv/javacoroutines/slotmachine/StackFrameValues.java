package io.github.dyominmv.javacoroutines.slotmachine;

import io.github.dyominmv.javacoroutines.CoroutinesTransformationException;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static io.github.dyominmv.javacoroutines.slotmachine.SlotType.SimpleSlot.*;

class StackFrameValuesIterator implements Iterator<StackFrameValue> {
    private SlotType first;
    private SlotType second;
    private final Iterator<SlotType> tail;

    public StackFrameValuesIterator(Iterable<SlotType> slots) {
        tail = slots.iterator();
        first = nextSlotOrNull();
        second = nextSlotOrNull();
    }

    private SlotType nextSlotOrNull() {
        return tail.hasNext() ? tail.next() : null;
    }

    private void stepForward() {
        first = second;
        second = nextSlotOrNull();
    }

    @Override
    public boolean hasNext() {
        return first != null;
    }

    @Override
    public StackFrameValue next() {
        var result = switch (first) {
            case null -> throw new NoSuchElementException();
            case UninitializedSlot _ -> throw new IllegalStateException("How did uninitialized value gor here?");
            case TOP -> StackFrameValue.NonStorable.Split.TOP;
            case NULL -> StackFrameValue.NonStorable.Null.NULL;
            case INTEGER -> StackFrameValue.Storable.ShortPrimitive.INTEGER;
            case FLOAT -> StackFrameValue.Storable.ShortPrimitive.FLOAT;
            case SlotType.Reference(ClassDesc classDesc) -> new StackFrameValue.Storable.Reference(classDesc);
            case DOUBLE -> second == TOP
                    ? StackFrameValue.Storable.LongPrimitive.DOUBLE
                    : StackFrameValue.NonStorable.Split.DOUBLE_FIRST;
            case LONG -> second == TOP
                    ? StackFrameValue.Storable.LongPrimitive.LONG
                    : StackFrameValue.NonStorable.Split.LONG_FIRST;
        };

        for (int i = 0; i < result.slots(); i++) stepForward();
        return result;
    }
}

public class StackFrameValues {
    private final List<StackFrameValue.Storable> stackValues = new ArrayList<>();
    private final List<StackFrameValue> localValues = new ArrayList<>();
    private final List<Integer> topSlotIndices = new ArrayList<>();

    StackFrameValues(Iterable<SlotType> stackSlots, Iterable<SlotType> localSlots) {
        var stackIterator = new StackFrameValuesIterator(stackSlots);
        while (stackIterator.hasNext())
            switch (stackIterator.next()) {
                case StackFrameValue.Storable storable -> stackValues.add(storable);
                case StackFrameValue.NonStorable nonStorable ->
                        throw new CoroutinesTransformationException("Stack has non-storable value" + nonStorable);
            }

        var localsIterator = new StackFrameValuesIterator(localSlots);
        var localSlotIndex = 0;
        while (localsIterator.hasNext()) {
            var localValue = localsIterator.next();

            if (localValue == StackFrameValue.NonStorable.Split.TOP) topSlotIndices.add(localSlotIndex);
            localValues.add(localValue);

            localSlotIndex += localValue.slots();
        }
    }

    public Iterable<StackFrameValue> locals() { return localValues; }
    public Iterable<Integer> topLocalSlotIndices() { return topSlotIndices; }

    public Iterable<StackFrameValue.Storable> stack() { return stackValues; }
    public Iterable<StackFrameValue.Storable> stackReversed() { return stackValues.reversed(); }
}
