package io.github.dyominmv.javacoroutines;

import java.util.ArrayDeque;

/// Stores the state of suspended coroutine (values from local variables and operand stack, suspension point index,
/// and flag, showing whether coroutine is finished or not)
///
/// Storing order:
/// 1. store all values of operand stack (top to bottom) using `store()`
/// 2. store all local variables using `storeLocal`
///
/// Loading order:
/// 1. load all values of operand stack using `load<type>()`
/// 2. load all local variables using `load<type>()`
///
/// note that there is no difference between type-1 integral primitives for jvm verification mechanism, thus `loadInt`
/// can also be used to load value of types `short`, 'char', 'byte', and 'boolean'.
@SuppressWarnings("DataFlowIssue")
public final class Continuation {

    // stack1, stack2, ..., stack_last, local1, local2, ..., local_last,
    private final ArrayDeque<Object> stackFrame = new ArrayDeque<>();

    /// sequential number of label, from which coroutine body should continue execution
    public int suspensionPoint = 0;

    /// the flag is set right before "native" return instruction of coroutine body
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
