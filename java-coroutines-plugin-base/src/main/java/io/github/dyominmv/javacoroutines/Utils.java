package io.github.dyominmv.javacoroutines;

import java.lang.classfile.CodeModel;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;

public class Utils {
    private Utils() { throw new RuntimeException(Utils.class + " is a utility class"); }

    public static ClassDesc desc(Class<?> aClass) {
        return aClass.describeConstable().orElseThrow();
    }

    public static MethodTypeDesc methodDesc(Class<?> returnType, Class<?>... parameters) {
        var paramDescriptors = new ArrayList<ClassDesc>(parameters.length);
        for (Class<?> parameter : parameters) paramDescriptors.add(desc(parameter));

        return MethodTypeDesc.of(desc(returnType), paramDescriptors);
    }

    public static int maxAccessedLocalSlot(CodeModel codeModel) {
        var result = 0;

        for (var codeElement : codeModel)
            if (codeElement instanceof LoadInstruction loadInstruction)
                result = Math.max(loadInstruction.slot() + loadInstruction.typeKind().slotSize(), result);
            else if (codeElement instanceof StoreInstruction storeInstruction)
                result = Math.max(storeInstruction.slot() + storeInstruction.typeKind().slotSize(), result);
            else if (codeElement instanceof IncrementInstruction incrementInstruction)
                result = Math.max(incrementInstruction.slot() + 1, result);

        return result;
    }
}
