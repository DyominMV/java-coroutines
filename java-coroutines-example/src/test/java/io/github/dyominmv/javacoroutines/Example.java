package io.github.dyominmv.javacoroutines;

import org.junit.jupiter.api.Test;

import static io.github.dyominmv.javacoroutines.Coroutine.result;
import static io.github.dyominmv.javacoroutines.Coroutine.suspend;

public class Example {
    private Coroutine<String> getOtherStringCoroutine() {
        System.out.println("\t > I am other coroutine! I suspend only twice");
        suspend();
        System.out.println("\t > I am other coroutine! One suspend is left");
        suspend();
        return result("Other coroutine finished");
    }

    public Coroutine<String> example() {
        System.out.println("first");
        suspend();
        for (int i = 0; i < 10; i += 1) {
            System.out.println("cycle " + i);
            suspend();
        }

        Coroutine<String> otherCoroutine = getOtherStringCoroutine();

        while (!otherCoroutine.finished()) {
            System.out.println("waiting for other string...");
            suspend();
            otherCoroutine.proceed();
        }
        var otherString = otherCoroutine.getResult();

        System.out.println("Other string is: " + otherString);
        return result("Success!");
    }


    @Test
    void shouldExecuteAndPrint() {
        var result = example().proceedUntilFinished();
        assert "Success!".equals(result);
    }
}
