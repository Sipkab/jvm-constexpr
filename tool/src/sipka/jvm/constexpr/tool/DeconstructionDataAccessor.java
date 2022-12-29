package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
		return new MethodDeconstructionDataAccessor(declaringtype.getMethod(methodname), receivertype);
	}

	public static DeconstructionDataAccessor createForField(Field f) {
		return new FieldDeconstructionDataAccessor(f);
	}
}
