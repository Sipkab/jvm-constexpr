package sipka.jvm.constexpr.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;

/**
 * {@link ConstantDeconstructor} that uses a static method to deconstruct the value.
 * <p>
 * A no-arg method is called for each argument of the static method, and an {@link Opcodes#INVOKESTATIC INVOKESTATIC}
 * instruction is placed.
 */
final class StaticMethodBasedDeconstructor implements ConstantDeconstructor {
	private final String methodOwnerTypeInternalName;
	private final String methodName;

	private final Type valueType;
	private final String[] methodsNames;
	private final Type[] asmArgTypes;

	public StaticMethodBasedDeconstructor(Type valuetype, Type methodownertype, String methodname, String[] methods,
			Type[] asmargtypes) {
		this.valueType = valuetype;
		this.methodOwnerTypeInternalName = methodownertype.getInternalName();
		this.methodName = methodname;
		this.methodsNames = methods;
		this.asmArgTypes = asmargtypes;
	}

	@Override
	public InsnList deconstructValue(ConstantExpressionInliner context, TransformedClass transclass, Object value) {
		try {
			InsnList insnlist = new InsnList();
			for (int i = 0; i < methodsNames.length; i++) {
				Method method = value.getClass().getMethod(methodsNames[i]);
				Object arg = method.invoke(value);
				Type argasmtype = asmArgTypes[i];
				if (argasmtype == null) {
					argasmtype = Type.getType(method.getReturnType());
					asmArgTypes[i] = argasmtype;
				}
				InsnList deconstructedinstructions = context.deconstructValue(transclass, arg, argasmtype);
				if (deconstructedinstructions == null) {
					return null;
				}
				insnlist.add(deconstructedinstructions);
			}
			//arguments everything has been deconstructed, add the invokespecial

			MethodInsnNode initins = new MethodInsnNode(Opcodes.INVOKESTATIC, methodOwnerTypeInternalName, methodName,
					Type.getMethodDescriptor(valueType, asmArgTypes));
			insnlist.add(initins);

			return insnlist;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param type
	 *            The owner of the method, and the return type of the method.
	 * @param methodname
	 * @param argumentsgettermethodnames
	 * @return
	 */
	public static ConstantDeconstructor createStaticFactoryDeconstructor(Class<?> type, String methodname,
			String... argumentsgettermethodnames) {
		return createStaticFactoryDeconstructor(type, methodname, new Type[argumentsgettermethodnames.length],
				argumentsgettermethodnames);
	}

	/**
	 * @param type
	 *            The owner of the method, and the return type of the method.
	 * @param methodname
	 * @param asmargtypes
	 * @param argumentsgettermethodnames
	 * @return
	 */
	public static ConstantDeconstructor createStaticFactoryDeconstructor(Class<?> type, String methodname,
			Type[] asmargtypes, String... argumentsgettermethodnames) {
		Type asmtype = Type.getType(type);
		return createStaticMethodDeconstructor(type, asmtype, methodname, asmargtypes, argumentsgettermethodnames);
	}

	/**
	 * @param valuetype
	 *            The return type of the method.
	 * @param methodowner
	 *            The owner type of the method.
	 * @param methodname
	 * @param argumentsgettermethodnames
	 * @return
	 */
	public static ConstantDeconstructor createStaticMethodDeconstructor(Class<?> valuetype, Type methodowner,
			String methodname, String... argumentsgettermethodnames) {
		return createStaticMethodDeconstructor(valuetype, methodowner, methodname,
				new Type[argumentsgettermethodnames.length], argumentsgettermethodnames);
	}

	/**
	 * @param valuetype
	 *            The return type of the method.
	 * @param methodowner
	 *            The owner type of the method.
	 * @param methodname
	 * @param asmargtypes
	 * @param argumentsgettermethodnames
	 * @return
	 */
	public static ConstantDeconstructor createStaticMethodDeconstructor(Class<?> valuetype, Type methodowner,
			String methodname, Type[] asmargtypes, String... argumentsgettermethodnames) {
		Type valueasmtype = Type.getType(valuetype);
		return createStaticMethodDeconstructor(valueasmtype, methodowner, methodname, asmargtypes,
				argumentsgettermethodnames);
	}

	/**
	 * @param valuetype
	 *            The return type of the method.
	 * @param methodowner
	 *            The owner type of the method.
	 * @param methodname
	 * @param asmargtypes
	 * @param argumentsgettermethodnames
	 * @return
	 */
	public static ConstantDeconstructor createStaticMethodDeconstructor(Type valuetype, Type methodowner,
			String methodname, Type[] asmargtypes, String... argumentsgettermethodnames) {
		return new StaticMethodBasedDeconstructor(valuetype, methodowner, methodname, argumentsgettermethodnames,
				asmargtypes);
	}
}