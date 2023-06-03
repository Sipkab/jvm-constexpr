package sipka.jvm.constexpr.tool;

import java.util.Arrays;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} that uses a static method to deconstruct the value.
 * <p>
 * A no-arg method is called for each argument of the static method, and an {@link Opcodes#INVOKESTATIC INVOKESTATIC}
 * instruction is placed.
 */
final class StaticMethodBasedDeconstructor implements ConstantDeconstructor {
	private final String methodOwnerTypeInternalName;
	private final String methodName;

	private final Type returnType;
	private final DeconstructionDataAccessor[] dataAccessors;

	public StaticMethodBasedDeconstructor(Type valuetype, Type methodownertype, String methodname,
			DeconstructionDataAccessor[] dataAccessors) {
		this.returnType = valuetype;
		this.methodOwnerTypeInternalName = methodownertype.getInternalName();
		this.methodName = methodname;
		this.dataAccessors = dataAccessors;
	}

	public String getMethodOwnerTypeInternalName() {
		return methodOwnerTypeInternalName;
	}

	public String getMethodName() {
		return methodName;
	}

	public Type getReturnType() {
		return returnType;
	}

	public DeconstructionDataAccessor[] getDataAccessors() {
		return dataAccessors;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		InsnList insnlist = new InsnList();
		AsmStackInfo[] arginfos = new AsmStackInfo[dataAccessors.length];

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
			Type argasmtype = deconstructeddata.getReceiverType();
			asmargtypes[i] = argasmtype;
			DeconstructionResult argdecon = context.deconstructValue(transclass, methodnode, arg, argasmtype);
			if (argdecon == null) {
				return null;
			}
			arginfos[i] = argdecon.getStackInfo();
			insnlist.add(argdecon.getInstructions());
		}
		//arguments everything has been deconstructed, add the invoke instruction

		MethodInsnNode initins = new MethodInsnNode(Opcodes.INVOKESTATIC, methodOwnerTypeInternalName, methodName,
				Type.getMethodDescriptor(returnType, asmargtypes));
		insnlist.add(initins);

		return DeconstructionResult.createStaticMethod(insnlist, Type.getObjectType(methodOwnerTypeInternalName),
				methodName, Type.getType(initins.desc), arginfos);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[methodOwnerTypeInternalName=");
		builder.append(methodOwnerTypeInternalName);
		builder.append(", methodName=");
		builder.append(methodName);
		builder.append(", returnType=");
		builder.append(returnType);
		builder.append(", dataAccessors=");
		builder.append(Arrays.toString(dataAccessors));
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @param type
	 *            The owner of the method, and the return type of the method.
	 * @param methodname
	 * @param asmargtypes
	 * @param argumentdataaccessors
	 * @return
	 */
	public static ConstantDeconstructor createStaticFactoryDeconstructor(Class<?> type, String methodname,
			DeconstructionDataAccessor... argumentdataaccessors) {
		Type asmtype = Type.getType(type);
		return createStaticMethodDeconstructor(type, asmtype, methodname, argumentdataaccessors);
	}

	/**
	 * @param valuetype
	 *            The return type of the method.
	 * @param methodowner
	 *            The owner type of the method.
	 * @param methodname
	 * @param argumentdataaccessors
	 * @return
	 */
	public static ConstantDeconstructor createStaticMethodDeconstructor(Class<?> valuetype, Type methodowner,
			String methodname, DeconstructionDataAccessor... argumentdataaccessors) {
		Type valueasmtype = Type.getType(valuetype);
		return createStaticMethodDeconstructor(valueasmtype, methodowner, methodname, argumentdataaccessors);
	}

	/**
	 * @param valuetype
	 *            The return type of the method.
	 * @param methodowner
	 *            The owner type of the method.
	 * @param methodname
	 * @param argumentdataaccessors
	 * @return
	 */
	public static ConstantDeconstructor createStaticMethodDeconstructor(Type valuetype, Type methodowner,
			String methodname, DeconstructionDataAccessor... argumentdataaccessors) {
		return new StaticMethodBasedDeconstructor(valuetype, methodowner, methodname, argumentdataaccessors);
	}
}