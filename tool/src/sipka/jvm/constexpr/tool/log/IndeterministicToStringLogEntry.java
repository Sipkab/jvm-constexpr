package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class IndeterministicToStringLogEntry implements LogEntry {

	private final String classInternalName;

	public IndeterministicToStringLogEntry(String classInternalName) {
		this.classInternalName = classInternalName;
	}

	public String getClassInternalName() {
		return classInternalName;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Object string representation seems to be indeterministic (contains identity hashcode): ");
		sb.append(Type.getObjectType(classInternalName).getClassName());
		return sb.toString();
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
		IndeterministicToStringLogEntry other = (IndeterministicToStringLogEntry) obj;
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
