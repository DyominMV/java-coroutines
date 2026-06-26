package io.github.dyominmv.javacoroutines;

public sealed interface Coroutine<Result> extends CoroutineLike<Result> permits CoroutineImpl {

    class CoroutineException extends RuntimeException {
        public CoroutineException(String message) { super(message); }
    }

    /// every call to this method made from methods returning `Coroutine<...>` is replaced by following set of actions:
    /// - store operand stack and local variables into continuation
    /// - assign number of current suspension point to `Continuation.suspensionPoint`
    /// - return null
    /// - place label that corresponds to current suspension point number
    /// - load operand stack and local variables from continuation
    ///
    /// use it when you want to suspend your method
    ///
    /// note that normally you may leave method body by `return` or `throw`. in case of coroutine this method is the
    /// other option. It implies that some `finally`-blocks (that are usually inlined before every `return` or `throw`)
    /// will not be executed if suspended coroutine execution was never continued.
    static void suspend() throws CoroutineException {
        throw new CoroutineException(
                "Coroutine.suspend should only be called from method returning " + Coroutine.class.getCanonicalName()
        );
    }

    /// call to this method is just removed from the byte-code of methods that use it. This method is used only to
    /// enforce type-safety when returning values from coroutine methods.
    static <Result> Coroutine<Result> result(Result result) {
        throw new CoroutineException(
                "Coroutine.result should only be called from method returning " + Coroutine.class.getCanonicalName()
        );
    }

}
