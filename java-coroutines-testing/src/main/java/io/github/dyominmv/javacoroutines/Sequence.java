package io.github.dyominmv.javacoroutines;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.function.Consumer;

public final class Sequence<T> implements Iterator<T> {

    public interface SequenceGenerator<T> {
        Coroutine<Void> generateSequence(Consumer<T> sequence);
    }

    private final Queue<T> elements = new ArrayDeque<>(2);
    private final Coroutine<Void> coroutine;

    public Sequence(SequenceGenerator<T> generator) {
        coroutine = generator.generateSequence(elements::add);
    }

    private void proceedUntilFinishedOrNextElementAvailable() {
        while (!coroutine.finished() && elements.isEmpty())
            coroutine.proceed();
    }

    @Override
    public boolean hasNext() {
        proceedUntilFinishedOrNextElementAvailable();
        return !elements.isEmpty();
    }

    @Override
    public T next() {
        proceedUntilFinishedOrNextElementAvailable();
        if (elements.isEmpty()) throw new NoSuchElementException();
        return elements.poll();
    }
}