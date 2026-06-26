package io.github.dyominmv.javacoroutines;

import java.util.function.Function;

public final class CoroutineImpl<Result> implements Coroutine<Result> {
    private final Function<Continuation, Result> coroutineBody;
    private final Continuation continuation;

    private volatile Result result = null;

    public CoroutineImpl(Continuation continuation, Function<Continuation, Result> coroutineBody) {
        this.continuation = continuation;
        this.coroutineBody = coroutineBody;
    }

    @Override
    public void proceed() { result = coroutineBody.apply(continuation); }

    @Override
    public boolean finished() { return continuation.finished; }

    @Override
    public Result getResult() { return result; }
}
