package sipka.jvm.constexpr.tool;

import java.util.HashSet;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import sipka.jvm.constexpr.tool.options.ToolInput;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassReader;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.JumpInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LabelNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LookupSwitchInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.TableSwitchInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * A class that is being operated on.
 */
class TransformedClass {
	public static class TransformedField {
		protected final FieldNode fieldNode;

		protected Optional<?> calculatedConstantValue;

		public TransformedField(FieldNode fieldNode) {
			this.fieldNode = fieldNode;
		}

		public boolean setCalculatedConstantValue(Object val) {
			if (this.calculatedConstantValue != null) {
				throw new IllegalStateException(
						"constant value already set: " + this.calculatedConstantValue + " for " + fieldNode.name);
			}

			if (((fieldNode.access & (Opcodes.ACC_FINAL | Opcodes.ACC_STATIC)) != (Opcodes.ACC_FINAL
					| Opcodes.ACC_STATIC))) {
				throw new IllegalStateException(
						"can't set constant value to non static final field: " + fieldNode.name);
			}
			this.calculatedConstantValue = Optional.ofNullable(val);
			if (val == null) {
				return true;
			}
			if (Utils.isConstantValue(val) && Utils.isInlineableConstantType(Type.getType(fieldNode.desc))) {
				fieldNode.value = val;
				return true;
			}
			return false;
		}

	}

	protected final ToolInput<?> input;
	protected final ClassReader classReader;
	protected final ClassNode classNode;

	protected transient NavigableMap<String, TransformedField> transformedFields = new TreeMap<>();

	protected transient MethodNode clinitMethod;
	protected transient NavigableSet<String> staticFieldReferences = new TreeSet<>();
	protected transient Set<LabelNode> nonJumpTargetLabelNodes = new HashSet<>();

	protected transient Set<AbstractInsnNode> inlinedInstructions = new HashSet<>();

	public TransformedClass(ToolInput<?> input, ClassReader classReader, ClassNode classNode) {
		this.input = input;
		this.classReader = classReader;
		this.classNode = classNode;

		for (MethodNode mn : classNode.methods) {
			for (AbstractInsnNode ins : mn.instructions) {
				if (ins.getOpcode() == Opcodes.GETSTATIC) {
					FieldInsnNode fins = (FieldInsnNode) ins;
					staticFieldReferences.add(fins.desc + " " + fins.owner + "." + fins.name);
				} else {
					if (ins.getType() == AbstractInsnNode.LABEL) {
						LabelNode lins = (LabelNode) ins;
						if (!isLabelNodeReferencedByJump(mn, lins)) {
							nonJumpTargetLabelNodes.add(lins);
						}
					}
				}
			}
			if ("<clinit>".equals(mn.name)) {
				if (clinitMethod != null) {
					throw new IllegalArgumentException("Multiple <clinit> methods in class: " + classNode.name);
				}
				clinitMethod = mn;
			}
		}
		for (FieldNode f : classNode.fields) {
			TransformedField transfield = new TransformedField(f);
			if (transformedFields.put(f.name, transfield) != null) {
				throw new IllegalArgumentException("Duplicate fields in class: " + f.name);
			}
			if (f.value != null) {
				transfield.calculatedConstantValue = Optional.of(f.value);
			}
		}
	}

	public boolean isReferencesStaticField(ClassNode cn, FieldNode fn) {
		return staticFieldReferences.contains(fn.desc + " " + cn.name + "." + fn.name);
	}

	private static boolean isLabelNodeReferencedByJump(MethodNode mn, LabelNode ln) {
		for (AbstractInsnNode ins : mn.instructions) {
			switch (ins.getType()) {
				case AbstractInsnNode.JUMP_INSN: {
					JumpInsnNode jins = (JumpInsnNode) ins;
					if (ln.equals(jins.label)) {
						return true;
					}
					break;
				}
				case AbstractInsnNode.LOOKUPSWITCH_INSN: {
					LookupSwitchInsnNode lsins = (LookupSwitchInsnNode) ins;
					if (lsins.labels.contains(ln)) {
						return true;
					}
					break;
				}
				case AbstractInsnNode.TABLESWITCH_INSN: {
					TableSwitchInsnNode tsins = (TableSwitchInsnNode) ins;
					if (tsins.labels.contains(ln)) {
						return true;
					}
					break;
				}
				default: {
					break;
				}
			}
		}
		for (TryCatchBlockNode tcbnode : mn.tryCatchBlocks) {
			//only the handler needs to be checked, as that is where the code jumps in case of exception
			if (ln.equals(tcbnode.handler)) {
				return true;
			}
		}
		return false;
	}
}