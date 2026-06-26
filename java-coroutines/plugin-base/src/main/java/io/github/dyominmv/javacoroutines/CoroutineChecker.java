package io.github.dyominmv.javacoroutines;

import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;

public class CoroutineChecker {

    public static boolean isCoroutine(MethodModel method) {
        if (!method.methodTypeSymbol().returnType().equals(Utils.desc(Coroutine.class))) return false;
        if (method.code().isEmpty()) return false;
        return invokesSuspendOrResult(method.code().orElseThrow());
    }

    private static boolean invokesSuspendOrResult(CodeModel codeModel) {
        for (var codeElement: codeModel)
            if (isSuspendInvocation(codeElement) || isResultInvocation(codeElement)) return true;

        return false;
    }

    public static boolean isResultInvocation(CodeElement codeElement) {
        return isInvokeStaticCoroutine(codeElement, "result");
    }

    public static boolean isSuspendInvocation(CodeElement codeElement) {
        return isInvokeStaticCoroutine(codeElement, "suspend");
    }

    private static boolean isInvokeStaticCoroutine(CodeElement codeElement, String methodName) {
        return codeElement instanceof InvokeInstruction invoke
                && invoke.opcode() == Opcode.INVOKESTATIC
                && invoke.owner().matches(Utils.desc(Coroutine.class))
                && invoke.name().equalsString(methodName);
    }
}
