package sipka.jvm.constexpr.tool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;

/**
 * Value class that is a unique identifier of a method of a class.
 */
class MethodKey extends MemberKey {
	protected final String methodDescriptor;

	public MethodKey(String owner, String methodName, String methodDescriptor) {
		super(owner, methodName);
		this.methodDescriptor = methodDescriptor;
	}

	public MethodKey(MethodInsnNode methodins) {
		this(methodins.owner, methodins.name, methodins.desc);
	}

	public MethodKey(Method m) {
		this(Type.getInternalName(m.getDeclaringClass()), m.getName(), Type.getMethodDescriptor(m));
	}

	public MethodKey(Constructor<?> m) {
		this(Type.getInternalName(m.getDeclaringClass()), Utils.CONSTRUCTOR_METHOD_NAME,
				Type.getConstructorDescriptor(m));
	}

	public static MethodKey create(Method method) {
		return new MethodKey(method);
	}

	public static MethodKey create(Constructor<?> constructor) {
		return new MethodKey(constructor);
	}

	public static MethodKey create(Executable executable) {
		if (executable instanceof Method) {
			return new MethodKey((Method) executable);
		}
		return new MethodKey((Constructor<?>) executable);
	}

	public String getMethodName() {
		return memberName;
	}

	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((methodDescriptor == null) ? 0 : methodDescriptor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodKey other = (MethodKey) obj;
		if (methodDescriptor == null) {
			if (other.methodDescriptor != null)
				return false;
		} else if (!methodDescriptor.equals(other.methodDescriptor))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[owner=");
		builder.append(owner);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append(", methodDescriptor=");
		builder.append(methodDescriptor);
		builder.append("]");
		return builder.toString();
	}
}