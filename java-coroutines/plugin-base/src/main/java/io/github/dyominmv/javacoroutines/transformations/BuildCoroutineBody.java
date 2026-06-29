package io.github.dyominmv.javacoroutines.transformations;

import io.github.dyominmv.javacoroutines.Continuation;
import io.github.dyominmv.javacoroutines.Coroutine;
import io.github.dyominmv.javacoroutines.Utils;
import io.github.dyominmv.javacoroutines.slotmachine.SlotMachine;

import java.lang.classfile.*;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.github.dyominmv.javacoroutines.Utils.*;
import static java.lang.classfile.TypeKind.INT;

public class BuildCoroutineBody implements Consumer<CodeBuilder> {

    private final SlotMachine slotMachine;
    private final CodeModel originalCode;
    private final int continuationSlot;

    public BuildCoroutineBody(SlotMachine slotMachine, CodeModel originalCode) {
        this.slotMachine = slotMachine;
        this.originalCode = originalCode;

        var initialLocalSlots = 0;
        for (var local : slotMachine.getCurrentStackFrame().locals()) initialLocalSlots += local.slots();
        continuationSlot = Math.max(maxAccessedLocalSlot(originalCode), initialLocalSlots);
    }

    @Override
    public void accept(CodeBuilder cb) {
        var cops = new ContinuationOperations(cb);

        // count suspension points
        List<Label> suspensionPointLabels = new ArrayList<>();
        suspensionPointLabels.add(cb.newLabel()); // start label
        for (var codeElement : originalCode)
            if (isSuspendInvocation(codeElement)) suspensionPointLabels.add(cb.newLabel());

        // variable 0 - continuation
        // variable 1 - current label
        cb.aload(0);                                                               // coroutine
        cb.getfield(desc(Continuation.class), "suspensionPoint", desc(int.class)); // int
        cb.storeLocal(INT, 1);                                                     // <empty stack>

        // === build goto-block ===
        for (var suspensionPointLabel : suspensionPointLabels) {
            cb.loadLocal(INT, 1);
            cb.ifeq(suspensionPointLabel);
            cb.iinc(1, -1);
        }
        // throw IllegalStateException if unknown suspension point
        cb.new_(desc(IllegalStateException.class));
        cb.dup();
        cb.loadConstant("Wrong suspension point index specified");
        cb.invokespecial(desc(IllegalStateException.class), "<init>", methodDesc(void.class, String.class));
        cb.athrow();

        var suspensionPointIndex = 0;

        // === bind start label ===
        cb.labelBinding(suspensionPointLabels.get(suspensionPointIndex));
        // load initial state from continuation
        cb.aload(0);
        cops.loadStateFromContinuation(slotMachine.getCurrentStackFrame());
        cb.astore(continuationSlot);

        // === accept original instructions ===
        for (var codeElement : originalCode) {
            if (isSuspendInvocation(codeElement)) {
                var stackFrame = slotMachine.getCurrentStackFrame();
                suspensionPointIndex += 1;

                // store state
                cb.aload(continuationSlot);                                                // continuation
                cops.storeStateToContinuation(stackFrame);                                 // continuation
                // store suspensionPointIndex
                cb.loadConstant(suspensionPointIndex);                                     // continuation,
                cb.putfield(desc(Continuation.class), "suspensionPoint", desc(int.class)); // <empty stack>
                // return null
                cb.aconst_null();                                                          // null
                cb.areturn();

                cb.labelBinding(suspensionPointLabels.get(suspensionPointIndex));

                // load state
                cb.aload(0);                                                               // continuation
                cops.loadStateFromContinuation(stackFrame);                                // ..., continuation
                cb.astore(continuationSlot);                                               // ...

            } else if (isResultInvocation(codeElement)) {
                // ignore invocation because we return result itself, not a wrapper
            } else {
                if (codeElement instanceof ReturnInstruction) {
                    cb.aload(continuationSlot);                                             // ..., continuation
                    cb.loadConstant(1);                                                     // ..., continuation, true
                    cb.putfield(desc(Continuation.class), "finished", desc(boolean.class)); // ...
                }

                slotMachine.accept(codeElement);
                cb.accept(codeElement);
            }
        }
    }

    private boolean isResultInvocation(CodeElement codeElement) {
        return isInvokeStaticCoroutine(codeElement, "result");
    }

    private boolean isSuspendInvocation(CodeElement codeElement) {
        return isInvokeStaticCoroutine(codeElement, "suspend");
    }

    private boolean isInvokeStaticCoroutine(CodeElement codeElement, String methodName) {
        return codeElement instanceof InvokeInstruction invoke
                && invoke.opcode() == Opcode.INVOKESTATIC
                && invoke.owner().matches(Utils.desc(Coroutine.class))
                && invoke.name().equalsString(methodName);
    }

}
