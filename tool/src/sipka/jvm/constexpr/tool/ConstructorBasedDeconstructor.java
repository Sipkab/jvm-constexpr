package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.TypeInsnNode;

/**
 * {@link ConstantDeconstructor} that uses a constructor to deconstruct the value.
 * <p>
 * A no-arg method is called for each argument of the constructor, and an {@link Opcodes#NEW NEW}, {@link Opcodes#DUP
 * DUP} and an {@link Opcodes#INVOKESPECIAL INVOKESPECIAL} instructions are placed.
 */
final class ConstructorBasedDeconstructor implements ConstantDeconstructor {
	private final String typeInternalName;

	private final DeconstructionDataAccessor[] dataAccessors;

	public ConstructorBasedDeconstructor(String typeInternalName, DeconstructionDataAccessor[] dataAccessors) {
		this.dataAccessors = dataAccessors;

		this.typeInternalName = typeInternalName;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			Object value) {
		InsnList insnlist = new InsnList();
		AsmStackInfo[] argumentstackinfos = new AsmStackInfo[dataAccessors.length];

		TypeInsnNode newins = new TypeInsnNode(Opcodes.NEW, typeInternalName);
		InsnNode dup = new InsnNode(Opcodes.DUP);

		insnlist.add(newins);
		insnlist.add(dup);

		Type[] asmargtypes = new Type[dataAccessors.length];
		for (int i = 0; i < dataAccessors.length; i++) {
			DeconstructedData deconstructeddata;
			DeconstructionDataAccessor dataaccessor = dataAccessors[i];
			try {
				deconstructeddata = dataaccessor.getData(value);
			} catch (Exception e) {
				context.logDeconstructionFailure(value, dataaccessor, e);
				return null;
			}
			Object arg = deconstructeddata.getData();
			Type argasmtype = Type.getType(deconstructeddata.getReceiverType());
			asmargtypes[i] = argasmtype;
			DeconstructionResult argdeconresult = context.deconstructValue(transclass, arg, argasmtype);
			if (argdeconresult == null) {
				return null;
			}
			argumentstackinfos[i] = argdeconresult.getStackInfo();
			insnlist.add(argdeconresult.getInstructions());
		}
		//every argument has been deconstructed, add the invoke instruction

		String methoddescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, asmargtypes);
		MethodInsnNode initins = new MethodInsnNode(Opcodes.INVOKESPECIAL, typeInternalName,
				Utils.CONSTRUCTOR_METHOD_NAME, methoddescriptor);
		insnlist.add(initins);

		return DeconstructionResult.createConstructor(insnlist, Type.getObjectType(typeInternalName),
				Type.getType(methoddescriptor), argumentstackinfos);
	}

	public static ConstantDeconstructor create(Class<?> type, DeconstructionDataAccessor... argumentdataaccessors) {
		return create(Type.getType(type), argumentdataaccessors);
	}

	public static ConstantDeconstructor create(Type type, DeconstructionDataAccessor... argumentdataaccessors) {
		return new ConstructorBasedDeconstructor(type.getInternalName(), argumentdataaccessors);
	}
}