package io.github.dyominmv.javacoroutines.transformations;

import io.github.dyominmv.javacoroutines.Continuation;
import io.github.dyominmv.javacoroutines.CoroutineImpl;
import io.github.dyominmv.javacoroutines.slotmachine.SlotMachine;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.invoke.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.dyominmv.javacoroutines.Utils.desc;
import static io.github.dyominmv.javacoroutines.Utils.methodDesc;
import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

public class BuildCoroutineHandle implements Consumer<CodeBuilder> {

    private final SlotMachine slotMachine;
    private final MethodHandleDesc coroutineBodyDesc;


    public BuildCoroutineHandle(SlotMachine slotMachine, MethodHandleDesc coroutineBodyDesc) {
        this.slotMachine = slotMachine;
        this.coroutineBodyDesc = coroutineBodyDesc;
    }

    @Override
    public void accept(CodeBuilder cb) {
        var cops = new ContinuationOperations(cb);

        // create continuation
        cb.new_(desc(Continuation.class));                                            // continuation
        cb.dup();                                                                     // continuation, continuation
        cb.invokespecial(desc(Continuation.class), "<init>", methodDesc(void.class)); // continuation
        // store method parameters into it
        cops.storeStateToContinuation(slotMachine.getCurrentStackFrame()); // continuation
        // create and return coroutine handle
        cb.new_(desc(CoroutineImpl.class));      // continuation, coroutine
        cb.dup_x1();                             // coroutine, continuation, coroutine
        cb.swap();                               // coroutine, coroutine, continuation
        cb.invokedynamic(coroutineBodyLambda()); // coroutine, coroutine, continuation, lambda
        cb.invokespecial(
                desc(CoroutineImpl.class),
                "<init>",
                methodDesc(void.class, Continuation.class, Function.class)
        );                                       // coroutine
        cb.areturn();
    }

    private DynamicCallSiteDesc coroutineBodyLambda() {
        return DynamicCallSiteDesc.of(LAMBDA_METAFACTORY, "apply", methodDesc(Function.class)).withArgs(
                methodDesc(Object.class, Object.class),      // interface method type
                coroutineBodyDesc,                           // implementation method
                methodDesc(Object.class, Continuation.class) // enforced method type
        );
    }

    private static final DirectMethodHandleDesc LAMBDA_METAFACTORY = MethodHandleDesc.ofMethod(
            STATIC,
            desc(LambdaMetafactory.class),
            "metafactory",
            methodDesc(
                    CallSite.class,
                    MethodHandles.Lookup.class,
                    String.class,
                    MethodType.class,
                    MethodType.class,
                    MethodHandle.class,
                    MethodType.class
            )
    );
}
