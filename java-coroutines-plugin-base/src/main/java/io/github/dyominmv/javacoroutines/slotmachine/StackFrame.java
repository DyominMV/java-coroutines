package io.github.dyominmv.javacoroutines.slotmachine;

import io.github.dyominmv.javacoroutines.slotmachine.SlotSpan.DoubleSlotSpan;
import io.github.dyominmv.javacoroutines.slotmachine.SlotSpan.SingleSlotSpan;

import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class StackFrame {
    private final List<SlotType> stack = new ArrayList<>();
    private final List<SlotType> locals = new ArrayList<>();

    public StackFrame(StackMapFrameInfo stackMapFrameInfo) {
        for (var stackElement : stackMapFrameInfo.stack()) stack.add(SlotType.of(stackElement));
        for (var local : stackMapFrameInfo.locals()) locals.add(SlotType.of(local));
    }

    public StackFrame(ClassDesc instanceDesc, List<ClassDesc> methodParameters) {
        if (null != instanceDesc) addLocal(SlotSpan.forDescriptor(instanceDesc));
        for (var argument : methodParameters) addLocal(SlotSpan.forDescriptor(argument));
    }

    public void pushStack(SlotType slot) { stack.addLast(slot); }

    public void pushStackLong() {
        pushStack(SlotType.SimpleSlot.LONG);
        pushStack(SlotType.SimpleSlot.TOP);
    }

    public void pushStackDouble() {
        pushStack(SlotType.SimpleSlot.DOUBLE);
        pushStack(SlotType.SimpleSlot.TOP);
    }

    public void pushStack(SlotSpan slotSpan) { switch (slotSpan) {
        case SingleSlotSpan(SlotType slotType) -> pushStack(slotType);
        case DoubleSlotSpan(SlotType first, SlotType second) -> {
            pushStack(first);
            pushStack(second);
        }
    }}

    public void pushStack(int slot) {
        var local = locals.get(slot);
        switch (local) {
            case SlotType.SimpleSlot.LONG -> pushStackLong();
            case SlotType.SimpleSlot.DOUBLE -> pushStackDouble();
            case SlotType.SimpleSlot _, SlotType.Reference _, SlotType.UninitializedSlot _ -> pushStack(local);
        }
    }

    public SlotType popStack() { return stack.removeLast(); }

    public void popStack(int count) { for (int i = 0; i < count; i++) popStack(); }

    private void addLocal(SlotSpan slotSpan) { switch (slotSpan) {
        case SingleSlotSpan(SlotType slotType) -> locals.addLast(slotType);
        case DoubleSlotSpan(SlotType firstSlot, SlotType secondSlot) -> {
            locals.addLast(firstSlot);
            locals.addLast(secondSlot);
        }
    }}

    public void putLocal(int index, SlotType slot) {
        var slotsToAdd = (index + 1) - locals.size();
        if (slotsToAdd > 0) locals.addAll(Arrays.asList(new SlotType[slotsToAdd]));

        locals.set(index, slot);
    }

    public void putLocalLong(int index) {
        putLocal(index, SlotType.SimpleSlot.LONG);
        putLocal(index + 1, SlotType.SimpleSlot.TOP);
    }

    public void putLocalDouble(int index) {
        putLocal(index, SlotType.SimpleSlot.DOUBLE);
        putLocal(index + 1, SlotType.SimpleSlot.TOP);
    }

    public Iterable<SlotType> stackSlots() { return stack; }

    public Iterable<SlotType> localSlots() { return locals; }

    public void clearStack() { stack.clear(); }

    public void clear() {
        stack.clear();
        locals.clear();
    }
}
