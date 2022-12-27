package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public class DeconstructorConfiguration {
	protected final Type memberOwner;
	//<init> if constructor
	protected final String memberName;

	DeconstructorConfiguration(Type memberOwner, String memberName) {
		this.memberOwner = memberOwner;
		this.memberName = memberName;
	}

	public static DeconstructorConfiguration createConstructor(Constructor<?> constructor, String... gettermethods) {
		Type[] asmargtypes = Utils.toAsmTypes(constructor.getParameterTypes());

		return new ConstructorDeconstructorConfiguration(Type.getType(constructor.getDeclaringClass()), asmargtypes,
				gettermethods);
	}

	public static DeconstructorConfiguration createConstructor(Type methodowner, Type[] parametertypes,
			String... gettermethods) {
		return new ConstructorDeconstructorConfiguration(methodowner, parametertypes, gettermethods);
	}

	public static DeconstructorConfiguration createStaticMethod(Method method, String... gettermethods) {
		if (((method.getModifiers() & Modifier.STATIC) != Modifier.STATIC)) {
			throw new IllegalArgumentException("Method is not static: " + method);
		}
		Type[] asmargtypes = Utils.toAsmTypes(method.getParameterTypes());

		return new StaticMethodDeconstructorConfiguration(Type.getType(method.getDeclaringClass()), method.getName(),
				Type.getType(method.getReturnType()), asmargtypes, gettermethods);
	}

	public static DeconstructorConfiguration createStaticMethod(Type methodowner, String methodname,
			Type methoddescriptor, String... gettermethods) {
		return new StaticMethodDeconstructorConfiguration(methodowner, methodname, methoddescriptor.getReturnType(),
				methoddescriptor.getArgumentTypes(), gettermethods);
	}

	public static DeconstructorConfiguration createStaticField(Field field) {
		if (((field.getModifiers() & Modifier.STATIC) != Modifier.STATIC)) {
			throw new IllegalArgumentException("Field is not static: " + field);
		}
		return new FieldDeconstructorConfiguration(Type.getType(field.getDeclaringClass()), field.getName(),
				Type.getType(field.getType()));
	}

	public Type getMemberOwner() {
		return memberOwner;
	}

	public String getMemberName() {
		return memberName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((memberName == null) ? 0 : memberName.hashCode());
		result = prime * result + ((memberOwner == null) ? 0 : memberOwner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeconstructorConfiguration other = (DeconstructorConfiguration) obj;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		if (memberOwner == null) {
			if (other.memberOwner != null)
				return false;
		} else if (!memberOwner.equals(other.memberOwner))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[memberOwner=");
		builder.append(memberOwner);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append("]");
		return builder.toString();
	}

}
