package io.github.dyominmv.javacoroutines;

import org.junit.jupiter.api.Test;

import static io.github.dyominmv.javacoroutines.Coroutine.result;
import static io.github.dyominmv.javacoroutines.Coroutine.suspend;

@SuppressWarnings("ConstantValue")
public class SimpleCoroutineTest {

    private int number = 0;

    private Coroutine<String> simpleCoroutine() {
        for (int i = 1; i <= 5; i += 1) {
            number = i;
            suspend();
        }
        return result("Success");
    }

    @Test
    void simpleCoroutine_shouldSetNewValueAndSuspend() {
        var coroutine = simpleCoroutine();
        assert number == 0;

        coroutine.proceed();
        assert number == 1;
        assert !coroutine.finished();

        coroutine.proceed();
        assert number == 2;
        assert !coroutine.finished();

        coroutine.proceed();
        assert number == 3;
        assert !coroutine.finished();

        coroutine.proceed();
        assert number == 4;
        assert !coroutine.finished();

        coroutine.proceed();
        assert number == 5;
        assert !coroutine.finished();

        coroutine.proceed();
        assert coroutine.finished();
        assert "Success".equals(coroutine.getResult());
    }
}
