package io.github.dyominmv.javacoroutines.slotmachine;

import io.github.dyominmv.javacoroutines.CoroutinesTransformationException;
import io.github.dyominmv.javacoroutines.Utils;

import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.classfile.instruction.ConstantInstruction.LoadConstantInstruction;
import java.lang.classfile.instruction.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlotMachine {
    private StackFrame stackFrame;
    private final Map<Label, StackMapFrameInfo> predefinedFrames;

    public SlotMachine(List<StackMapFrameInfo> stackMaps, ClassDesc instanceDesc, List<ClassDesc> methodParameters) {
        stackFrame = new StackFrame(instanceDesc, methodParameters);
        predefinedFrames = new HashMap<>(stackMaps.size());
        for (var frame : stackMaps) predefinedFrames.put(frame.target(), frame);
    }

    public StackFrameValues getCurrentStackFrame() {
        return new StackFrameValues(stackFrame.stackSlots(), stackFrame.localSlots());
    }

    public void accept(CodeElement codeElement) {
        switch (codeElement) {
            case LabelTarget labelTarget -> acceptLabelTarget(labelTarget);
            case Instruction instruction -> acceptInstruction(instruction);
            case null, default -> { /*do nothing*/ }
        }
    }

    private void acceptLabelTarget(LabelTarget labelTarget) {
        var predefinedFrame = predefinedFrames.get(labelTarget.label());
        if (null != predefinedFrame) stackFrame = new StackFrame(predefinedFrame);
    }

    private void acceptInstruction(Instruction instruction) {
        switch (instruction.opcode()) {
            case NOP -> {}

            case ACONST_NULL -> stackFrame.pushStack(SlotType.SimpleSlot.NULL);

            case ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, BIPUSH, SIPUSH, ILOAD,
                 ILOAD_0, ILOAD_1, ILOAD_2, ILOAD_3, ILOAD_W ->
                    stackFrame.pushStack(SlotType.SimpleSlot.INTEGER);

            case LCONST_0, LCONST_1, LLOAD, LLOAD_0, LLOAD_1, LLOAD_2, LLOAD_3, LLOAD_W ->
                    stackFrame.pushStackLong();

            case FCONST_0, FCONST_1, FCONST_2, FLOAD, FLOAD_0, FLOAD_1, FLOAD_2, FLOAD_3, FLOAD_W ->
                    stackFrame.pushStack(SlotType.SimpleSlot.FLOAT);

            case DCONST_0, DCONST_1, DLOAD, DLOAD_0, DLOAD_1, DLOAD_2, DLOAD_3, DLOAD_W ->
                    stackFrame.pushStackDouble();

            case LDC, LDC_W, LDC2_W -> acceptLoadConstant((LoadConstantInstruction) instruction);

            case ALOAD, ALOAD_0, ALOAD_1, ALOAD_2, ALOAD_3, ALOAD_W ->
                    stackFrame.pushStack(((LoadInstruction) instruction).slot());

            case IALOAD, BALOAD, CALOAD, SALOAD -> {
                stackFrame.popStack(2);
                stackFrame.pushStack(SlotType.SimpleSlot.INTEGER);
            }
            case FALOAD -> {
                stackFrame.popStack(2);
                stackFrame.pushStack(SlotType.SimpleSlot.FLOAT);
            }
            case LALOAD -> {
                stackFrame.popStack(2);
                stackFrame.pushStackLong();
            }
            case DALOAD -> {
                stackFrame.popStack(2);
                stackFrame.pushStackDouble();
            }
            case AALOAD -> acceptReferenceArrayLoad();

            case ISTORE, ISTORE_0, ISTORE_1, ISTORE_2, ISTORE_3, ISTORE_W,
                 FSTORE, FSTORE_0, FSTORE_1, FSTORE_2, FSTORE_3, FSTORE_W,
                 ASTORE, ASTORE_0, ASTORE_1, ASTORE_2, ASTORE_3, ASTORE_W -> {
                var storeInstruction = (StoreInstruction) instruction;
                var storedElement = stackFrame.popStack();
                stackFrame.putLocal(storeInstruction.slot(), storedElement);
            }
            case LSTORE, LSTORE_0, LSTORE_1, LSTORE_2, LSTORE_3, LSTORE_W -> {
                var storeInstruction = (StoreInstruction) instruction;
                stackFrame.popStack(2);
                stackFrame.putLocalLong(storeInstruction.slot());
            }
            case DSTORE, DSTORE_0, DSTORE_1, DSTORE_2, DSTORE_3, DSTORE_W -> {
                var storeInstruction = (StoreInstruction) instruction;
                stackFrame.popStack(2);
                stackFrame.putLocalDouble(storeInstruction.slot());
            }
            case IASTORE, FASTORE, AASTORE, BASTORE, CASTORE, SASTORE -> stackFrame.popStack(3); // array, index, value

            case LASTORE, DASTORE -> stackFrame.popStack(4); // array, index, value_start, top

            case POP -> stackFrame.popStack();

            case POP2 -> stackFrame.popStack(2);

            case DUP -> {
                var topValue = stackFrame.popStack();
                stackFrame.pushStack(topValue);
                stackFrame.pushStack(topValue);
            }
            case DUP_X1 -> {
                var value1 = stackFrame.popStack();
                var value2 = stackFrame.popStack();
                stackFrame.pushStack(value1);
                stackFrame.pushStack(value2);
                stackFrame.pushStack(value1);
            }
            case DUP_X2 -> {
                var value1 = stackFrame.popStack();
                var value2 = stackFrame.popStack();
                var value3 = stackFrame.popStack();
                stackFrame.pushStack(value1);
                stackFrame.pushStack(value3);
                stackFrame.pushStack(value2);
                stackFrame.pushStack(value1);
            }
            case DUP2 -> {
                var value1 = stackFrame.popStack();
                var value2 = stackFrame.popStack();
                stackFrame.pushStack(value2);
                stackFrame.pushStack(value1);
                stackFrame.pushStack(value2);
                stackFrame.pushStack(value1);
            }
            case DUP2_X1 -> {
                var value1 = stackFrame.popStack();
                var value2 = stackFrame.popStack();
                var value3 = stackFrame.popStack();
                stackFrame.pushStack(value2);
                stackFrame.pushStack(value1);
                stackFrame.pushStack(value3);
                stackFrame.pushStack(value2);
                stackFrame.pushStack(value1);
            }
            case DUP2_X2 -> {
                var value1 = stackFrame.popStack();
                var value2 = stackFrame.popStack();
                var value3 = stackFrame.popStack();
                var value4 = stackFrame.popStack();
                stackFrame.pushStack(value2);
                stackFrame.pushStack(value1);
                stackFrame.pushStack(value4);
                stackFrame.pushStack(value3);
                stackFrame.pushStack(value2);
                stackFrame.pushStack(value1);
            }
            case SWAP -> {
                var value1 = stackFrame.popStack();
                var value2 = stackFrame.popStack();
                stackFrame.pushStack(value1);
                stackFrame.pushStack(value2);
            }
            case IADD, ISUB, IMUL, IDIV, IREM, IAND, IOR, IXOR, ISHL, ISHR, IUSHR,
                 FADD, FSUB, FMUL, FDIV, FREM,                  LSHL, LSHR, LUSHR ->
                    stackFrame.popStack(); // binary operation on short type as second operand : _, a -> _

            case LADD, LSUB, LMUL, LDIV, LREM, LAND, LOR, LXOR,
                 DADD, DSUB, DMUL, DDIV, DREM ->
                    stackFrame.popStack(2); // binary operation on long type as second operand: _, _, a, b -> _, _

            case INEG, LNEG, FNEG, DNEG, IINC, IINC_W, I2B, I2C, I2S -> { /* stack types unchanged */ }

            case I2L, F2L -> { // Short value to L
                stackFrame.popStack();
                stackFrame.pushStackLong();
            }
            case I2F -> { // Short value to F
                stackFrame.popStack();
                stackFrame.pushStack(SlotType.SimpleSlot.FLOAT);
            }
            case I2D, F2D -> { // Short value to D
                stackFrame.popStack();
                stackFrame.pushStackDouble();
            }
            case L2I, D2I -> { // Long value to I
                stackFrame.popStack(2);
                stackFrame.pushStack(SlotType.SimpleSlot.INTEGER);
            }
            case L2F, D2F -> { // Long value to F
                stackFrame.popStack(2);
                stackFrame.pushStack(SlotType.SimpleSlot.FLOAT);
            }
            case L2D -> { // Long value to D
                stackFrame.popStack(2);
                stackFrame.pushStackDouble();
            }
            case F2I -> { // Short value to I
                stackFrame.popStack();
                stackFrame.pushStack(SlotType.SimpleSlot.INTEGER);
            }
            case D2L -> { // Long value to L
                stackFrame.popStack(2);
                stackFrame.pushStackLong();
            }
            case LCMP, DCMPL, DCMPG -> {
                stackFrame.popStack(4); // both long values popped
                stackFrame.pushStack(SlotType.SimpleSlot.INTEGER); // int pushed
            }
            case FCMPL, FCMPG -> {
                stackFrame.popStack(2); // both float values popped
                stackFrame.pushStack(SlotType.SimpleSlot.INTEGER); // int pushed
            }
            case IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IFNULL, IFNONNULL ->
                    stackFrame.popStack(); // pop compared short value

            case IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE ->
                    stackFrame.popStack(2); // pop 2 compared short values

            case GOTO, GOTO_W -> { }

            case JSR, JSR_W, RET, RET_W ->
                    throw new CoroutinesTransformationException("opcode " + instruction.opcode() + " unsupported");

            case TABLESWITCH, LOOKUPSWITCH -> stackFrame.popStack(); // pop int index or key

            case IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN -> stackFrame.clear();

            case GETSTATIC -> {
                var getStatic = (FieldInstruction) instruction;
                stackFrame.pushStack(SlotSpan.forDescriptor(getStatic.typeSymbol()));
            }
            case PUTSTATIC -> {
                var putStatic = (FieldInstruction) instruction;
                stackFrame.popStack(SlotSpan.forDescriptor(putStatic.typeSymbol()).slots());
            }
            case GETFIELD -> {
                stackFrame.popStack();
                var getField = (FieldInstruction) instruction;
                stackFrame.pushStack(SlotSpan.forDescriptor(getField.typeSymbol()));
            }
            case PUTFIELD -> {
                stackFrame.popStack();
                var putField = (FieldInstruction) instruction;
                stackFrame.popStack(SlotSpan.forDescriptor(putField.typeSymbol()).slots());
            }
            case INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE ->
                    acceptInvoke(((InvokeInstruction) instruction).typeSymbol(), true);

            case INVOKESTATIC -> acceptInvoke(((InvokeInstruction) instruction).typeSymbol(), false);

            case INVOKEDYNAMIC  -> acceptInvoke(((InvokeDynamicInstruction) instruction).typeSymbol(), false);

            case NEW -> {
                var objectType = ((NewObjectInstruction) instruction).className().asSymbol();
                stackFrame.pushStack(new SlotType.Reference(objectType));
            }
            case NEWARRAY -> {
                var newArray = (NewPrimitiveArrayInstruction) instruction;
                var arrayType = newArray.typeKind().upperBound().arrayType();
                stackFrame.popStack(); // remove array size
                stackFrame.pushStack(new SlotType.Reference(arrayType)); // push array itself
            }
            case ANEWARRAY -> {
                var aNewArray = (NewReferenceArrayInstruction) instruction;
                var arrayType = aNewArray.componentType().asSymbol().arrayType();
                stackFrame.popStack(); // remove array size
                stackFrame.pushStack(new SlotType.Reference(arrayType)); // push array itself
            }
            case MULTIANEWARRAY -> {
                var multiANewArray = (NewMultiArrayInstruction) instruction;
                var arrayType = multiANewArray.arrayType().asSymbol();
                stackFrame.popStack(multiANewArray.dimensions()); // remove array dimensions sizes
                stackFrame.pushStack(new SlotType.Reference(arrayType));
            }
            case ARRAYLENGTH -> {
                stackFrame.popStack();
                stackFrame.pushStack(SlotType.SimpleSlot.INTEGER);
            }
            case ATHROW -> {
                var thrown = stackFrame.popStack();
                stackFrame.clearStack();
                stackFrame.pushStack(thrown);
            }
            case CHECKCAST -> {
                var targetType = ((TypeCheckInstruction) instruction).type().asSymbol();
                var topElement = stackFrame.popStack();
                if (!topElement.equals(SlotType.SimpleSlot.NULL))
                    stackFrame.pushStack(new SlotType.Reference(targetType));
                else
                    stackFrame.pushStack(topElement);
            }
            case INSTANCEOF -> {
                stackFrame.popStack();
                stackFrame.pushStack(SlotType.SimpleSlot.INTEGER);
            }
            case MONITORENTER, MONITOREXIT -> stackFrame.popStack(); // remove monitor reference

            case null, default ->
                    throw new CoroutinesTransformationException("Unexpected opcode: " + instruction.opcode());
        }
    }

    private void acceptLoadConstant(LoadConstantInstruction loadConstant) {
        var constantValue = loadConstant.constantEntry().constantValue();
        switch (constantValue) {
            case null -> stackFrame.pushStack(SlotType.SimpleSlot.NULL);
            case ClassDesc _ -> stackFrame.pushStack(new SlotType.Reference(Class.class));
            case MethodHandleDesc _ -> stackFrame.pushStack(new SlotType.Reference(MethodHandle.class));
            case MethodTypeDesc _ -> stackFrame.pushStack(new SlotType.Reference(MethodType.class));
            case String _ -> stackFrame.pushStack(new SlotType.Reference(String.class));
            case Double _ -> stackFrame.pushStackDouble();
            case Float _ -> stackFrame.pushStack(SlotType.SimpleSlot.FLOAT);
            case Integer _ -> stackFrame.pushStack(SlotType.SimpleSlot.INTEGER);
            case Long _ -> stackFrame.pushStackLong();
            case DynamicConstantDesc<?> dynConstant ->
                    stackFrame.pushStack(SlotSpan.forDescriptor(dynConstant.constantType()));

            default -> throw new CoroutinesTransformationException("Unexpected constant value: " + constantValue);
        }
    }

    private void acceptReferenceArrayLoad() {
        stackFrame.popStack(); // pop index
        var arrayType = stackFrame.popStack();
        if (!(arrayType instanceof SlotType.Reference(ClassDesc arrayTypeDesc)))
            throw new CoroutinesTransformationException(arrayType + "should be array type");

        stackFrame.pushStack(new SlotType.Reference(arrayTypeDesc.componentType()));
    }

    private void acceptInvoke(MethodTypeDesc methodType, boolean hasReceiver) {
        var parameters = methodType.parameterList();
        var parameterSlots = 0;
        for (ClassDesc parameter : parameters) parameterSlots += SlotSpan.forDescriptor(parameter).slots();
        stackFrame.popStack(parameterSlots);

        if (hasReceiver) stackFrame.popStack(); // remove instance if static call has receiver

        if (!methodType.returnType().equals(Utils.desc(void.class)))
            stackFrame.pushStack(SlotSpan.forDescriptor(methodType.returnType()));
    }

}
