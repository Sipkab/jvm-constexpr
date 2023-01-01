package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sipka.jvm.constexpr.tool.DeconstructedData;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public interface DeconstructionDataAccessor {
	public DeconstructedData getData(Object value) throws Exception;

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	public static DeconstructionDataAccessor createForMethod(Method m) {
		return new MethodDeconstructionDataAccessor(m);
	}

	public static DeconstructionDataAccessor createForMethod(Class<?> declaringtype, String methodname)
			throws NoSuchMethodException {
		return createForMethod(declaringtype.getMethod(methodname));
	}

	public static DeconstructionDataAccessor createForMethodWithReceiver(Class<?> declaringtype, String methodname,
			Class<?> receivertype) throws NoSuchMethodException {
		return createForMethodWithReceiver(declaringtype.getMethod(methodname), receivertype);
	}

	public static DeconstructionDataAccessor createForMethodWithReceiver(Class<?> declaringtype, String methodname,
			Type receivertype) throws NoSuchMethodException {
		return createForMethodWithReceiver(declaringtype.getMethod(methodname), receivertype);
	}

	public static DeconstructionDataAccessor createForMethodWithReceiver(Method method, Class<?> receivertype) {
		return createForMethodWithReceiver(method, Type.getType(receivertype));
	}

	public static DeconstructionDataAccessor createForMethodWithReceiver(Method method, Type type) {
		return new MethodDeconstructionDataAccessor(method, type);
	}

	public static DeconstructionDataAccessor createForField(Field f) {
		return new FieldDeconstructionDataAccessor(f);
	}
}
