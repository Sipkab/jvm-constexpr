package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;

class DeconstructionResult {
	enum Kind {
		STATIC_METHOD,
		CONSTRUCTOR,
		STATIC_FIELD,
		ARRAY,
		CONSTANT,
		NULL,

		;
	}

	private InsnList instructions;
	private AsmStackInfo stackInfo;

	private DeconstructionResult(InsnList instructions, AsmStackInfo stackinfo) {
		this.instructions = instructions;
		this.stackInfo = stackinfo;
	}

	public static DeconstructionResult createConstant(InsnList instructions, Object value) {
		return new DeconstructionResult(instructions, AsmStackInfo.createConstant(value));
	}

	public static DeconstructionResult createNull(InsnList instructions) {
		return new DeconstructionResult(instructions, AsmStackInfo.createNull());
	}

	public static DeconstructionResult createArray(InsnList instructions, Type componenttype, AsmStackInfo length,
			AsmStackInfo[] elements) {
		return new DeconstructionResult(instructions, AsmStackInfo.createArray(componenttype, length, elements));
	}

	public static DeconstructionResult createConstructor(InsnList instructions, Type instancetype,
			Type methoddescriptor, AsmStackInfo[] arguments) {
		return new DeconstructionResult(instructions,
				AsmStackInfo.createConstructor(instancetype, methoddescriptor, arguments));
	}

	public static DeconstructionResult createStaticMethod(InsnList instructions, Type methodowner, String methodname,
			Type methoddescriptor, AsmStackInfo[] arguments) {
		return new DeconstructionResult(instructions,
				AsmStackInfo.createStaticMethod(methodowner, methodname, methoddescriptor, arguments));
	}

	public static DeconstructionResult createField(InsnList instructions, Type fieldowner, String fieldname,
			Type fielddescriptor) {
		return new DeconstructionResult(instructions,
				AsmStackInfo.createStaticField(fieldowner, fieldname, fielddescriptor));
	}

	public InsnList getInstructions() {
		return instructions;
	}

	public AsmStackInfo getStackInfo() {
		return stackInfo;
	}
}
