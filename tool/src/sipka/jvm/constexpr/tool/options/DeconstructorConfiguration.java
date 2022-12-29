package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sipka.jvm.constexpr.tool.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public class DeconstructorConfiguration {
	protected final Type memberOwner;
	//<init> if constructor
	protected final String memberName;

	DeconstructorConfiguration(Type memberOwner, String memberName) {
		this.memberOwner = memberOwner;
		this.memberName = memberName;
	}

	public static DeconstructorConfiguration createConstructor(Constructor<?> constructor,
			DeconstructionDataAccessor... parameterdataaccessors) throws NullPointerException {
		return createConstructor(Type.getType(constructor.getDeclaringClass()), parameterdataaccessors);
	}

	public static DeconstructorConfiguration createConstructor(Type methodowner,
			DeconstructionDataAccessor... parameterdataaccessors) throws NullPointerException {
		return new ConstructorDeconstructorConfiguration(methodowner, parameterdataaccessors);
	}

	public static DeconstructorConfiguration createStaticMethod(Method method,
			DeconstructionDataAccessor... parameterdataaccessors) throws NullPointerException {
		if (((method.getModifiers() & Modifier.STATIC) != Modifier.STATIC)) {
			throw new IllegalArgumentException("Method is not static: " + method);
		}
		return createStaticMethod(Type.getType(method.getDeclaringClass()), method.getName(),
				Type.getType(method.getReturnType()), parameterdataaccessors);
	}

	public static DeconstructorConfiguration createStaticMethod(Type methodowner, String methodname,
			Type methoddescriptor, DeconstructionDataAccessor... parameterdataaccessors) throws NullPointerException {
		return new StaticMethodDeconstructorConfiguration(methodowner, methodname, methoddescriptor.getReturnType(),
				parameterdataaccessors);
	}

	public static DeconstructorConfiguration createStaticField(Field field) throws NullPointerException {
		if (((field.getModifiers() & Modifier.STATIC) != Modifier.STATIC)) {
			throw new IllegalArgumentException("Field is not static: " + field);
		}
		return createStaticField(Type.getType(field.getDeclaringClass()), field.getName(),
				Type.getType(field.getType()));
	}

	public static DeconstructorConfiguration createStaticField(Type fieldowner, String fieldname, Type fieldtype) {
		return new FieldDeconstructorConfiguration(fieldowner, fieldname, fieldtype);
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
