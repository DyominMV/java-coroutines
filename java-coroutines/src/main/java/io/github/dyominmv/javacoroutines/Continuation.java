package io.github.dyominmv.javacoroutines;

import java.util.ArrayDeque;

@SuppressWarnings("DataFlowIssue")
public final class Continuation {

    // stack1, stack2, ..., stack_last, local1, local2, ..., local_last,
    private final ArrayDeque<Object> stackFrame = new ArrayDeque<>();

    /*
        storing order: store stack (via addFirst), store locals (via addLast)
        loading order: load stack (via pollFirst), load locals (via pollFirst)
     */

    public int suspensionPoint = 0;
    public boolean finished = false;

    public void store(int value) { stackFrame.addFirst(value); }
    public void storeLocal(int value) { stackFrame.addLast(value); }
    public int loadInt() { return (int) stackFrame.pollFirst(); }

    public void store(long value) { stackFrame.addFirst(value); }
    public void storeLocal(long value) { stackFrame.addLast(value); }
    public long loadLong() { return (long) stackFrame.pollFirst(); }

    public void store(float value) { stackFrame.addFirst(value); }
    public void storeLocal(float value) { stackFrame.addLast(value); }
    public float loadFloat() { return (float) stackFrame.pollFirst(); }

    public void store(double value) { stackFrame.addFirst(value); }
    public void storeLocal(double value) { stackFrame.addLast(value); }
    public double loadDouble() { return (double) stackFrame.pollFirst(); }

    public void store(Object value) { stackFrame.addFirst(value); }
    public void storeLocal(Object value) { stackFrame.addLast(value); }
    public Object loadObject() { return stackFrame.pollFirst(); }
}
