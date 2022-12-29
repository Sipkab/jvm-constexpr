package sipka.jvm.constexpr.tool;

import java.lang.reflect.Method;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

/**
 * {@link ConstantReconstructor} that checks if the value is an {@link Enum}, and calls the given method on it if so.
 */
class EnumOnlyMethodConstantReconstructor implements ConstantReconstructor {
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	public static final EnumOnlyMethodConstantReconstructor NAME_INSTANCE;
	public static final EnumOnlyMethodConstantReconstructor ORDINAL_INSTANCE;
	public static final EnumOnlyMethodConstantReconstructor GETDECLARINGCLASS_INSTANCE;
	static {
		try {
			Method name = Enum.class.getMethod("name");
			Method ordinal = Enum.class.getMethod("ordinal");
			Method getdeclaringclass = Enum.class.getMethod("getDeclaringClass");
			NAME_INSTANCE = new EnumOnlyMethodConstantReconstructor(name);
			ORDINAL_INSTANCE = new EnumOnlyMethodConstantReconstructor(ordinal);
			GETDECLARINGCLASS_INSTANCE = new EnumOnlyMethodConstantReconstructor(getdeclaringclass);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	private final Method method;

	public EnumOnlyMethodConstantReconstructor(Method method) {
		this.method = method;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		// ins is INVOKEVIRTUAL
		AsmStackReconstructedValue subjectval;
		try {
			subjectval = context.getInliner().reconstructStackValue(context, ins.getPrevious());
		} catch (ReconstructionException e) {
			throw context.newInstanceAccessFailureReconstructionException(e, ins,
					Type.getInternalName(method.getDeclaringClass()), method.getName(),
					Type.getMethodDescriptor(method));
		}
		if (subjectval == null) {
			return null;
		}
		Object subject = subjectval.getValue();
		if (!(subject instanceof Enum<?>)) {
			//not Enum, ignored by us
			return null;
		}
		Object resultval;
		try {
			resultval = method.invoke(subject);
		} catch (Exception e) {
			throw context.newMethodInvocationFailureReconstructionException(e, ins,
					Type.getInternalName(method.getDeclaringClass()), method.getName(),
					Type.getMethodDescriptor(method), subject, EMPTY_OBJECT_ARRAY);
		}
		return new AsmStackReconstructedValue(subjectval.getFirstIns(), ins.getNext(),
				AsmStackInfo.createMethod(Type.getType(method.getDeclaringClass()), method.getName(),
						Type.getType(method), subjectval.getStackInfo(), AsmStackInfo.EMPTY_ASMSTACKINFO_ARRAY),
				resultval);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[method=");
		builder.append(method);
		builder.append("]");
		return builder.toString();
	}

}
