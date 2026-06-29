package io.github.dyominmv.javacoroutines;

import org.junit.jupiter.api.Test;

import static io.github.dyominmv.javacoroutines.Coroutine.suspend;

@SuppressWarnings("InfiniteLoopStatement")
public class SequenceTest {

    private final Sequence<Long> fibonacci = new Sequence<>(consumer -> {
        long a = 1;
        long b = 0;
        while(true) {
            long c = a + b;
            a = b;
            b = c;
            consumer.accept(b);
            suspend();
        }
    });

    @Test
    @SuppressWarnings("AssertWithSideEffects")
    void fibonacci_shouldProduceFirst10FibonacciNumbers() {
        assert fibonacci.next() == 1L;
        assert fibonacci.next() == 1L;
        assert fibonacci.next() == 2L;
        assert fibonacci.next() == 3L;
        assert fibonacci.next() == 5L;
        assert fibonacci.next() == 8L;
        assert fibonacci.next() == 13L;
        assert fibonacci.next() == 21L;
        assert fibonacci.next() == 34L;
        assert fibonacci.next() == 55L;

        assert fibonacci.hasNext();
    }

}
