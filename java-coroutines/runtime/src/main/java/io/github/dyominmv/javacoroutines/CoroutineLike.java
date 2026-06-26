package io.github.dyominmv.javacoroutines;

/// object to handle coroutines.
///
/// typical usage:
/// ```java
/// CoroutineLike<Result> coroutine = ...;
/// while (!coroutine.finished()) {
///     // wait or suspend
///     // proceed when waited enough time
///     coroutine.proceed();
/// }
/// var result = coroutine.getResult()
/// ```
public interface CoroutineLike<Result> {
    /// continue coroutine execution
    void proceed();

    /// check whether coroutine is finished
    boolean finished();

    /// get result computed by coroutine. Call after execution is finished.
    Result getResult();

    default Result proceedUntilFinished() {
        while (!finished()) proceed();
        return getResult();
    }
}
