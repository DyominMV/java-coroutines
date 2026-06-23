package io.github.dyominmv.javacoroutines;

public interface CoroutineLike<Result> {
    void proceed();
    boolean finished();
    Result getResult();

    default Result proceedUntilFinished() {
        while (!finished()) proceed();
        return getResult();
    }
}
