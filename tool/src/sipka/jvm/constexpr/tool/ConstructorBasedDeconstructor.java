package sipka.jvm.constexpr.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

	private final String[] methodsNames;

	private final Type[] asmArgTypes;

	public ConstructorBasedDeconstructor(String[] methodnames, String typeInternalName, Type[] asmargtypes) {
		this.methodsNames = methodnames;
		this.asmArgTypes = asmargtypes;

		this.typeInternalName = typeInternalName;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			Object value) {
		try {
			InsnList insnlist = new InsnList();
			AsmStackInfo[] argumentstackinfos = new AsmStackInfo[methodsNames.length];

			TypeInsnNode newins = new TypeInsnNode(Opcodes.NEW, typeInternalName);
			InsnNode dup = new InsnNode(Opcodes.DUP);

			insnlist.add(newins);
			insnlist.add(dup);

			for (int i = 0; i < methodsNames.length; i++) {
				Method method = value.getClass().getMethod(methodsNames[i]);
				Object arg = method.invoke(value);
				Type argasmtype = asmArgTypes[i];
				if (argasmtype == null) {
					argasmtype = Type.getType(method.getReturnType());
					asmArgTypes[i] = argasmtype;
				}
				DeconstructionResult argdeconresult = context.deconstructValue(transclass, arg, argasmtype);
				if (argdeconresult == null) {
					return null;
				}
				argumentstackinfos[i] = argdeconresult.getStackInfo();
				insnlist.add(argdeconresult.getInstructions());
			}
			//every argument has been deconstructed, add the invokespecial

			String methoddescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, asmArgTypes);
			MethodInsnNode initins = new MethodInsnNode(Opcodes.INVOKESPECIAL, typeInternalName,
					Utils.CONSTRUCTOR_METHOD_NAME, methoddescriptor);
			insnlist.add(initins);

			return DeconstructionResult.createConstructor(insnlist, Type.getObjectType(typeInternalName),
					Type.getType(methoddescriptor), argumentstackinfos);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static ConstantDeconstructor create(Class<?> type, String... argumentsgettermethodnames) {
		return create(type, new Type[argumentsgettermethodnames.length], argumentsgettermethodnames);
	}

	public static ConstantDeconstructor create(Class<?> type, Type[] asmargtypes,
			String... argumentsgettermethodnames) {
		return create(Type.getType(type), asmargtypes, argumentsgettermethodnames);
	}

	public static ConstantDeconstructor create(Type type, Type[] asmargtypes, String... argumentsgettermethodnames) {
		return new ConstructorBasedDeconstructor(argumentsgettermethodnames, type.getInternalName(), asmargtypes);
	}
}