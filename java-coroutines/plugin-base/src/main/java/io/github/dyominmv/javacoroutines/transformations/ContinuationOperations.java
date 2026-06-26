package io.github.dyominmv.javacoroutines.transformations;

import io.github.dyominmv.javacoroutines.Continuation;
import io.github.dyominmv.javacoroutines.slotmachine.StackFrameValue;
import io.github.dyominmv.javacoroutines.slotmachine.StackFrameValues;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;

import static io.github.dyominmv.javacoroutines.Utils.desc;
import static io.github.dyominmv.javacoroutines.Utils.methodDesc;

public class ContinuationOperations {
    private final CodeBuilder cb;

    public ContinuationOperations(CodeBuilder codeBuilder) { this.cb = codeBuilder; }

    // requires continuation object on top of the stack
    // at the end leaves the continuation object on top of the stack
    public void loadStateFromContinuation(StackFrameValues desiredStackFrame) {
        // load stack
        for (var value : desiredStackFrame.stack()) loadStackElement(value);

        // load local variable slots of type `top`
        for (var topLocalSlotIndex : desiredStackFrame.topLocalSlotIndices()){
            cb.loadConstant(0L);
            cb.lstore(topLocalSlotIndex - 1);
        }
        // load all other locals
        int slot = 0;
        for (var local: desiredStackFrame.locals()) {
            loadLocalVariable(local, slot);
            slot += local.slots();
        }
    }

    // requires continuation object on top of the stack
    // at the end leaves the continuation object on top of the stack
    public void storeStateToContinuation(StackFrameValues actualStackFrame) {
        // store stack
        for (var value: actualStackFrame.stackReversed()) storeStackElement(value);

        // store locals
        var slot = 0;
        for (var local: actualStackFrame.locals()) {
            if (local instanceof StackFrameValue.Storable storableLocal) storeLocalVariable(storableLocal, slot);
            slot += local.slots();
        }
    }

    // ..., continuation -> ..., continuation (unchanged)
    private void loadLocalVariable(StackFrameValue local, int slot) {
        switch (local) {
            case StackFrameValue.NonStorable.Null.NULL -> cb.aconst_null().astore(slot);
            case StackFrameValue.NonStorable.Split.DOUBLE_FIRST -> cb.dconst_0().dstore(slot);
            case StackFrameValue.NonStorable.Split.LONG_FIRST -> cb.lconst_0().lstore(slot);
            case StackFrameValue.NonStorable.Split.TOP -> {} // skip, already initialized

            case StackFrameValue.Storable value -> {
                cb.dup();                              // ..., continuation, continuation
                loadFromContinuation(value);           // ..., continuation, value
                cb.storeLocal(value.typeKind(), slot); // ..., continuation
            }
        }
    }

    // ..., continuation -> ..., continuation (unchanged)
    private void storeLocalVariable(StackFrameValue.Storable local, int slot) {
        cb.dup();                             // ..., continuation, continuation
        cb.loadLocal(local.typeKind(), slot); // ..., continuation, continuation, value
        storeLocalIntoContinuation(local);    // ..., continuation
    }

    // ..., continuation -> ..., value, continuation
    private void loadStackElement(StackFrameValue.Storable value) {
        cb.dup();                    // ..., continuation, continuation
        loadFromContinuation(value); // ..., continuation, value

        if (value instanceof StackFrameValue.Storable.LongPrimitive){
            cb.dup2_x1();            // ..., value2, continuation, value2
            cb.pop2();               // ..., value2, continuation
        }
        else cb.swap();              // ..., continuation, value1
    }

    // ..., value, continuation -> ..., continuation
    private void storeStackElement(StackFrameValue.Storable value) {
        if (value instanceof StackFrameValue.Storable.LongPrimitive) {
            cb.dup_x2();                   // ..., continuation, value, continuation
            cb.dup_x2();                   // ..., continuation, continuation, value, continuation
            cb.pop();                      // ..., continuation, continuation, value
        } else {
            cb.dup_x1();                   // ..., continuation, value, continuation
            cb.swap();                     // ..., continuation, continuation, value
        }
        storeValueIntoContinuation(value); // ..., continuation
    }

    private void storeValueIntoContinuation(StackFrameValue.Storable value) {
        cb.invokevirtual(desc(Continuation.class), "store", methodDesc(void.class, value.upperBound()));
    }

    private void storeLocalIntoContinuation(StackFrameValue.Storable value) {
        cb.invokevirtual(desc(Continuation.class), "storeLocal", methodDesc(void.class, value.upperBound()));
    }

    private void loadFromContinuation(StackFrameValue.Storable value) {
        var methodName = switch(value) {
            case StackFrameValue.Storable.Reference _ -> "loadObject";
            case StackFrameValue.Storable.LongPrimitive.LONG -> "loadLong";
            case StackFrameValue.Storable.LongPrimitive.DOUBLE -> "loadDouble";
            case StackFrameValue.Storable.ShortPrimitive.INTEGER -> "loadInt";
            case StackFrameValue.Storable.ShortPrimitive.FLOAT -> "loadFloat";
        };

        cb.invokevirtual(desc(Continuation.class), methodName, methodDesc(value.upperBound()));

        if (value instanceof StackFrameValue.Storable.Reference(ClassDesc classDesc)) cb.checkcast(classDesc);
    }

}
