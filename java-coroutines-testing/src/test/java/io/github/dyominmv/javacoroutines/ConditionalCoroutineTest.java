package io.github.dyominmv.javacoroutines;

import org.junit.jupiter.api.Test;

import static io.github.dyominmv.javacoroutines.Coroutine.result;
import static io.github.dyominmv.javacoroutines.Coroutine.suspend;

public class ConditionalCoroutineTest {

    private boolean coroutineEntered = false;
    private boolean isEven = false;
    private boolean isOdd = false;

    private Coroutine<String> mod2Coroutine(int number) {
        coroutineEntered = true;
        suspend();
        if (number % 2 == 1) {
            isOdd = true;
            suspend();
            return result("Odd");
        } else {
            isEven = true;
            suspend();
            return result("Even");
        }
    }

    @Test
    void mod2Coroutine_shouldSetFlagAndReturnOdd_whenOddParameter() {
        var coroutine = mod2Coroutine(101);
        assert !coroutineEntered;
        assert !coroutine.finished();

        coroutine.proceed();
        assert coroutineEntered && !isOdd && !isEven; // condition is not checked yet
        assert !coroutine.finished();

        coroutine.proceed();
        assert isOdd && !isEven;
        assert !coroutine.finished();

        coroutine.proceed();
        assert coroutine.finished();
        assert "Odd".equals(coroutine.getResult());
    }

    @Test
    void mod2Coroutine_shouldSetFlagAndReturnOdd_whenEvenParameter() {
        var coroutine = mod2Coroutine(100);
        assert !coroutineEntered;
        assert !coroutine.finished();

        coroutine.proceed();
        assert coroutineEntered && !isOdd && !isEven; // condition is not checked yet
        assert !coroutine.finished();

        coroutine.proceed();
        assert !isOdd && isEven;
        assert !coroutine.finished();

        coroutine.proceed();
        assert coroutine.finished();
        assert "Even".equals(coroutine.getResult());
    }
}
