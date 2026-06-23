package io.github.dyominmv.javacoroutines;

public sealed interface Coroutine<Result> extends CoroutineLike<Result> permits CoroutineImpl {

    class CoroutineException extends RuntimeException {
        public CoroutineException(String message) { super(message); }
    }

    static void suspend() throws CoroutineException {
        throw new CoroutineException(
                "Coroutine.suspend should only be called from method returning " + Coroutine.class.getCanonicalName()
        );
    }

    static <Result> Coroutine<Result> result(Result result) {
        throw new CoroutineException(
                "Coroutine.result should only be called from method returning " + Coroutine.class.getCanonicalName()
        );
    }

}
