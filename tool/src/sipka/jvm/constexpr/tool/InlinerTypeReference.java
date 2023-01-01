package sipka.jvm.constexpr.tool;

class InlinerTypeReference {
	private Class<?> type;

	public InlinerTypeReference(Class<?> type) {
		this.type = type;
	}

	/**
	 * @param context
	 * @return <code>null</code> if the type is not available.
	 */
	public Class<?> getType(ConstantExpressionInliner context) {
		return type;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[");
		if (type != null) {
			builder.append("type=");
			builder.append(type);
		}
		builder.append("]");
		return builder.toString();
	}

}
