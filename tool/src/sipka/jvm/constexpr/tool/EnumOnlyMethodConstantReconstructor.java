package sipka.jvm.constexpr.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

/**
 * {@link ConstantReconstructor} that checks if the value is an {@link Enum}, and calls the given method on it if so.
 */
public class EnumOnlyMethodConstantReconstructor implements ConstantReconstructor {
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
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins) {
		// ins is INVOKEVIRTUAL
		AsmStackReconstructedValue subjectval = context.getInliner().reconstructStackValue(context, ins.getPrevious());
		if (subjectval == null) {
			return null;
		}
		Object subject = subjectval.getValue();
		if (!(subject instanceof Enum<?>)) {
			return null;
		}
		Object resultval;
		try {
			resultval = method.invoke(subject);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new AsmStackReconstructedValue(subjectval.getFirstIns(), ins.getNext(), resultval);
	}

}
