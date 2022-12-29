package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public class DeconstructorNotConfiguredLogEntry implements LogEntry {

	private final String classInternalName;

	public DeconstructorNotConfiguredLogEntry(String classInternalName) {
		this.classInternalName = classInternalName;
	}

	public String getClassInternalName() {
		return classInternalName;
	}

	@Override
	public String getMessage() {
		return "Deconstructor not configured for type: " + Type.getObjectType(classInternalName).getClassName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classInternalName == null) ? 0 : classInternalName.hashCode());
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
		DeconstructorNotConfiguredLogEntry other = (DeconstructorNotConfiguredLogEntry) obj;
		if (classInternalName == null) {
			if (other.classInternalName != null)
				return false;
		} else if (!classInternalName.equals(other.classInternalName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[classInternalName=");
		builder.append(classInternalName);
		builder.append("]");
		return builder.toString();
	}

}
