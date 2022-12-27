package sipka.jvm.constexpr.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

/**
 * {@link ConstantReconstructor} that calls a static or instance method to reconstruct a value.
 */
final class MethodBasedConstantReconstructor implements ConstantReconstructor {
	public static final MethodBasedConstantReconstructor TOSTRING_INSTANCE;
	static {
		try {
			TOSTRING_INSTANCE = new MethodBasedConstantReconstructor(Object.class.getMethod("toString"));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError("Object.toString method not found.", e);
		}
	}

	private final Method method;

	private final transient Class<?>[] parameterTypes;
	private final transient boolean staticFunc;

	public MethodBasedConstantReconstructor(Method m) {
		this.parameterTypes = m.getParameterTypes();
		this.staticFunc = (m.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
		this.method = m;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins) {
		//ins is the INVOKESTATIC/INVOKEVIRTUAL instruction
		int paramcount = parameterTypes.length;
		Object[] args = new Object[paramcount];
		AsmStackReconstructedValue[] derivedargs = new AsmStackReconstructedValue[paramcount];
		if (!context.getInliner().reconstructArguments(context.forArgumentReconstruction(), parameterTypes, ins, args,
				derivedargs)) {
			return null;
		}
		AbstractInsnNode firstins = paramcount == 0 ? ins : derivedargs[0].getFirstIns();
		Object subject;
		if (staticFunc) {
			subject = null;
		} else {
			AsmStackReconstructedValue subjectval = context.getInliner().reconstructStackValue(
					context.withReceiverType(method.getDeclaringClass()), firstins.getPrevious());
			if (subjectval == null) {
				return null;
			}
			firstins = subjectval.getFirstIns();
			subject = subjectval.getValue();
		}
		Object resultobj;
		try {
			resultobj = method.invoke(subject, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new AsmStackReconstructedValue(firstins, ins.getNext(), resultobj);
	}
}