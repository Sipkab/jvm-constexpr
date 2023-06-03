package sipka.jvm.constexpr.tool;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import sipka.jvm.constexpr.tool.TransformedClass.TransformedField;
import sipka.jvm.constexpr.tool.log.BytecodeLocation;
import sipka.jvm.constexpr.tool.log.ConfigClassMemberInaccessibleLogEntry;
import sipka.jvm.constexpr.tool.log.DeconstructionFailedLogEntry;
import sipka.jvm.constexpr.tool.log.DeconstructorNotConfiguredLogEntry;
import sipka.jvm.constexpr.tool.log.IndeterministicToStringLogEntry;
import sipka.jvm.constexpr.tool.log.InstructionReplacementLogEntry;
import sipka.jvm.constexpr.tool.log.LogContextInfo;
import sipka.jvm.constexpr.tool.log.MultipleInitializationPathLogEntry;
import sipka.jvm.constexpr.tool.log.ReconstructionFailureLogEntry;
import sipka.jvm.constexpr.tool.log.ToolLogger;
import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.options.ToolInput;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassReader;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassWriter;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Handle;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.IntInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LabelNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LdcInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.TypeInsnNode;

public class ConstantExpressionInliner {
	public static final int ASM_API = Opcodes.ASM9;

	private static final Map<String, ConstantDeconstructor> baseConstantDeconstructors = new TreeMap<>();

	/**
	 * Internal names mapped to the type that are considered constants. That is, they don't have a mutable state, and
	 * all of their non-static functions are pure functions and they don't rely or read the executing environment.
	 * <p>
	 * All of their constructors are also free to be called for constant optimization.
	 * <p>
	 * If the type is an enum, then the enum fields are retrievable via GETSTATIC, and is considered a constant.
	 * <p>
	 * Map of internal names to classes.
	 */
	private static final Map<String, InlinerTypeReference> baseConstantTypes = new TreeMap<>();
	private static final NavigableMap<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors = new TreeMap<>(
			MemberKey::compare);
	static {
		BaseConfig.configure(baseConstantTypes, baseConstantReconstructors, baseConstantDeconstructors);
	}

	private final NavigableMap<FieldKey, Field> optionsConstantFields = new TreeMap<>(MemberKey::compare);
	/**
	 * {@link ClassNode#name} to {@link TransformedClass} instances.
	 */
	private final NavigableMap<String, TransformedClass> inputClasses = new TreeMap<>();

	/**
	 * Class internal name to deconstructors.
	 */
	private final Map<String, ConstantDeconstructor> constantDeconstructors = new TreeMap<>();
	/**
	 * The reconstructors that can be used by the inliner.
	 * <p>
	 * The map contains full {@link MemberKey} keys, as well as member keys with empty owner, that contains only the
	 * member name and descriptor for instance methods.
	 */
	private final NavigableMap<MemberKey, TypeReferencedConstantReconstructor> constantReconstructors = new TreeMap<>(
			MemberKey::compare);
	/**
	 * Internal names to classes.
	 */
	private final Map<String, InlinerTypeReference> constantTypes = new TreeMap<>();

	private final Map<List<LogContextInfo>, ReconstructionFailureLogEntry> reconstructionFailureLogEntries = new HashMap<>();

	private final Map<String, DeconstructorNotConfiguredLogEntry> deconstructorNotConfiguredLogEntries = new TreeMap<>();
	private final Set<FieldKey> multipleInitializationLoggedFields = new TreeSet<>(MemberKey::compare);
	private final Set<String> configMemberNotAvailableLoggedEntries = new TreeSet<>();

	private final Map<String, IndeterministicToStringLogEntry> toStringLogEntries = new TreeMap<>();

	private ClassLoader classLoader;

	private ToolLogger logger;

	private ConstantExpressionInliner() {
	}

	private void runInlining(InlinerOptions options) throws IOException {
		OutputConsumer oc = options.getOutputConsumer();
		if (oc == null) {
			throw new IllegalArgumentException("Output consumer is not set.");
		}

		logger = options.getLogger();
		classLoader = options.getClassLoader();

		Collection<? extends ToolInput<?>> inputs = options.getInputs();

		if (inputs.isEmpty()) {
			return;
		}
		constantDeconstructors.putAll(baseConstantDeconstructors);
		constantReconstructors.putAll(baseConstantReconstructors);
		constantTypes.putAll(baseConstantTypes);

		//accumulate the general instance method constant reconstructors
		for (Entry<MemberKey, TypeReferencedConstantReconstructor> entry : baseConstantReconstructors.entrySet()) {
			ConstantReconstructor delegate = entry.getValue().delegate;
			if (delegate instanceof MethodBasedConstantReconstructor) {
				addGeneralInstanceMethodConstantReconstructor((MethodBasedConstantReconstructor) delegate,
						(MethodKey) entry.getKey());
			}
		}

		for (Field f : options.getConstantFields()) {
			Field prev = optionsConstantFields.putIfAbsent(new FieldKey(f), f);
			if (prev != null && !prev.equals(f)) {
				throw new IllegalArgumentException("Multiple equal constant fields specified: " + f + " and " + prev);
			}
		}
		for (Entry<Class<?>, ? extends DeconstructionSelector> configentry : options.getDeconstructorConfigurations()
				.entrySet()) {
			DeconstructionSelector selector = configentry.getValue();
			String typeinternalname = Type.getInternalName(configentry.getKey());
			if (selector == null) {
				//override to remove
				constantDeconstructors.remove(typeinternalname);
			} else {
				constantDeconstructors.put(typeinternalname, new ConfigSelectorConstantDeconstructor(selector));
			}
		}
		for (Entry<Member, ReconstructorPredicate> entry : options.getConstantReconstructors().entrySet()) {
			Member e = entry.getKey();
			ReconstructorPredicate predicate = entry.getValue();
			//setAccessible so we can surely call it later
			((AccessibleObject) e).setAccessible(true);

			ConstantReconstructor reconstructor;
			MemberKey memberkey = MemberKey.create(e);
			if (e instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) e;
				reconstructor = new ConstructorBasedConstantReconstructor(c, predicate);
			} else if (e instanceof Method) {
				Method m = (Method) e;
				reconstructor = new MethodBasedConstantReconstructor(m, predicate);
			} else if (e instanceof Field) {
				Field f = (Field) e;
				reconstructor = new FieldBasedConstantReconstructor(f, predicate);
			} else {
				throw new IllegalArgumentException("Unrecognized Executable type: " + e);
			}
			constantReconstructors.put(memberkey,
					new TypeReferencedConstantReconstructor(reconstructor, e.getDeclaringClass()));
			if (reconstructor instanceof MethodBasedConstantReconstructor) {
				addGeneralInstanceMethodConstantReconstructor((MethodBasedConstantReconstructor) reconstructor,
						(MethodKey) memberkey);
			}

		}
		for (Class<?> ctype : options.getConstantTypes()) {
			constantTypes.put(Type.getInternalName(ctype), new InlinerTypeReference(ctype));
		}

		for (ToolInput<?> input : inputs) {
			byte[] classbytes = input.getBytes();
			ClassReader cr = new ClassReader(classbytes);

			ClassNode cn = new ClassNode(ASM_API);
			cr.accept(cn, ClassReader.EXPAND_FRAMES);

			TransformedClass prev = inputClasses.putIfAbsent(cn.name, new TransformedClass(input, cr, cn));
			if (prev != null) {
				throw new IllegalArgumentException(
						"Duplicate input class with name: " + cn.name + " " + input + " and " + prev.input);
			}
		}
		List<TransformedClass> round = new ArrayList<>(inputClasses.values());
		// some optimizations only need to be performed once,
		for (TransformedClass transclass : round) {
			ClassNode cn = transclass.classNode;

			for (MethodNode mn : cn.methods) {
				replacePrimitiveTypeGetStaticInstructions(mn);
			}
		}

		List<TransformedClass> nextround = new ArrayList<>();
		for (; !round.isEmpty(); round = nextround, nextround = new ArrayList<>()) {
			for (TransformedClass transclass : round) {
				ClassNode cn = transclass.classNode;

				for (MethodNode mn : cn.methods) {
					performInstructionInlining(transclass, mn);
				}
				if (transclass.clinitMethod != null) {
					for (TransformedField transfield : transclass.transformedFields.values()) {
						boolean inlined = inlineFieldInitializerValueFromStaticInitializers(transclass, transfield,
								transclass.clinitMethod);
						if (inlined) {
							//inline the field value to other codes
							for (TransformedClass tc : inputClasses.values()) {
								if (tc.isReferencesStaticField(cn, transfield.fieldNode)) {
									inlineFieldValue(transclass, transfield, tc);
									//always reprocess them if they reference this field
									nextround.add(tc);
								}
							}
						}
					}
					if (Utils.isMethodEmpty(transclass.clinitMethod)) {
						transclass.classNode.methods.remove(transclass.clinitMethod);
						transclass.clinitMethod = null;
					}
				}
			}
		}

		//report the logs before the outputs, so the caller can have a chance to abort in case of error
		if (logger != null) {
			//order the reconstruction log entries by the size of their stack, so we always log the longest
			//ones with a given root cause
			List<ReconstructionFailureLogEntry> reconlogentries = new ArrayList<>(
					reconstructionFailureLogEntries.values());
			reconlogentries.sort((l, r) -> {
				return -Integer.compare(l.getContextStack().size(), r.getContextStack().size());
			});
			//sort by the root cause locations, so the logs are always reported in order
			reconlogentries.sort((l, r) -> {
				List<LogContextInfo> lcstack = l.getContextStack();
				LogContextInfo llast = lcstack.get(lcstack.size() - 1);
				List<LogContextInfo> rcstack = r.getContextStack();
				LogContextInfo rlast = rcstack.get(rcstack.size() - 1);

				BytecodeLocation lbcloc = llast.getBytecodeLocation();
				BytecodeLocation rbcloc = rlast.getBytecodeLocation();
				return lbcloc.compareLocation(rbcloc);
			});

			Collection<LogContextInfo> lastreconstructioncontextinfos = new HashSet<>();
			for (ReconstructionFailureLogEntry entry : reconlogentries) {
				List<LogContextInfo> cstack = entry.getContextStack();
				LogContextInfo last = cstack.get(cstack.size() - 1);
				if (!lastreconstructioncontextinfos.add(last)) {
					//already logged an entry with this root cause 
					continue;
				}
				logger.log(entry);
			}

			for (DeconstructorNotConfiguredLogEntry logentry : deconstructorNotConfiguredLogEntries.values()) {
				logger.log(logentry);
			}
			for (IndeterministicToStringLogEntry logentry : toStringLogEntries.values()) {
				logger.log(logentry);
			}
		}

		for (Entry<String, TransformedClass> entry : inputClasses.entrySet()) {
			TransformedClass transclass = entry.getValue();

			ClassNode cn = transclass.classNode;
			ClassWriter cw = new ClassWriter(transclass.classReader, 0);

			cn.accept(cw);
			oc.put(transclass.input, cw.toByteArray());
		}

	}

	private void addGeneralInstanceMethodConstantReconstructor(MethodBasedConstantReconstructor reconstructor,
			MethodKey memberkey) {
		if (Modifier.isStatic(reconstructor.getMethod().getModifiers())) {
			//don't add, only for instance methods
			return;
		}
		MethodKey reducedkey = new MethodKey("", memberkey.getMemberName(), memberkey.getMethodDescriptor());
		constantReconstructors.compute(reducedkey, (k, present) -> {
			if (present == null) {
				return new TypeReferencedConstantReconstructor(reconstructor);
			}
			if (present.delegate instanceof DynamicInstanceMethodBasedConstantReconstructor) {
				((DynamicInstanceMethodBasedConstantReconstructor) present.delegate).addReconstructor(reconstructor);
				return present;
			} else {
			}
			return new TypeReferencedConstantReconstructor(new DynamicInstanceMethodBasedConstantReconstructor(
					reconstructor, (MethodBasedConstantReconstructor) present.delegate));
		});
	}

	/**
	 * Runs the constant inliner tool with the given options.
	 * 
	 * @param options
	 *            The options.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws IOException
	 *             If an IO exception is thrown during the reading of the input, or writing of the output.
	 */
	public static void run(InlinerOptions options) throws NullPointerException, IOException {
		Objects.requireNonNull(options, "options");
		new ConstantExpressionInliner().runInlining(options);
	}

	/**
	 * Reconstructs method arguments from the stack.
	 * 
	 * @param transclass
	 *            The class being transformed.
	 * @param parameterAsmTypes
	 *            The types from the associated method descriptor. May be <code>null</code> in which case the
	 *            instruction descriptor is used.
	 * @param parameterTypes
	 *            The classes of the parameters, <code>null</code> if no information is available about them.
	 * @param ins
	 *            The instruction at which the arguments should be reconstructed. This is the method call instruction.
	 * @param outargs
	 *            The output array of arguments.
	 * @param outderivedargs
	 *            The output array of the reconstructed value holders.
	 * @return <code>true</code> if the arguments were successfully reconstructed.
	 * @throws ReconstructionException
	 */
	boolean reconstructArguments(ReconstructionContext context, Type[] parameterAsmTypes, Class<?>[] parameterTypes,
			AbstractInsnNode ins, Object[] outargs, AsmStackReconstructedValue[] outderivedargs)
			throws ReconstructionException {
		if (parameterAsmTypes == null) {
			if (ins instanceof MethodInsnNode) {
				String methoddesc = ((MethodInsnNode) ins).desc;
				parameterAsmTypes = Type.getArgumentTypes(methoddesc);
				if (parameterAsmTypes.length != outargs.length) {
					throw new IllegalArgumentException("Parameter count mismatch for descriptor: " + methoddesc
							+ " and expected: " + outargs.length);
				}
			}
		}
		AbstractInsnNode argit = ins.getPrevious();
		for (int i = 0; i < outargs.length; i++) {
			int paramindex = outargs.length - i - 1;
			AsmStackReconstructedValue argval;
			Class<?> parameterclass = parameterTypes == null ? null : parameterTypes[paramindex];
			try {
				argval = reconstructStackValue(context.withReceiverType(parameterclass), argit);
			} catch (ReconstructionException e) {
				throw context.newArgumentIndexReconstructionException(e, ins, paramindex);
			}
			if (argval == null) {
				return false;
			}
			Type type = parameterAsmTypes == null ? null : parameterAsmTypes[paramindex];
			Object val = castValueFromAsm(type, argval.getValue());
			outderivedargs[paramindex] = argval;
			outargs[paramindex] = val;
			argit = argval.getFirstIns().getPrevious();
		}
		return true;
	}

	boolean reconstructArguments(ReconstructionContext context, Class<?>[] parameterTypes, AbstractInsnNode ins,
			Object[] outargs, AsmStackReconstructedValue[] outderivedargs) throws ReconstructionException {
		return reconstructArguments(context, null, parameterTypes, ins, outargs, outderivedargs);
	}

	private Collection<AsmStackReconstructedValue> getAssignedFieldValuesFromMethod(TransformedClass transclass,
			FieldNode fieldnode, MethodNode clinitmethodnode, Field constantfield) {
		final int putopcode;
		if (((fieldnode.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC)) {
			putopcode = Opcodes.PUTSTATIC;
		} else {
			putopcode = Opcodes.PUTFIELD;
		}
		List<AbstractInsnNode> putinsns = new ArrayList<>();
		InsnList instructions = clinitmethodnode.instructions;
		for (AbstractInsnNode ins = instructions.getFirst(); ins != null;) {
			if (ins.getOpcode() != putopcode) {
				ins = ins.getNext();
				continue;
			}
			FieldInsnNode fieldins = (FieldInsnNode) ins;
			if (!fieldins.name.equals(fieldnode.name) || !fieldins.owner.equals(transclass.classNode.name)) {
				//different field
				ins = ins.getNext();
				continue;
			}
			putinsns.add(ins);
			ins = ins.getNext();
		}
		if (putinsns.isEmpty()) {
			return null;
		}

		ReconstructionContext reconstructioncontext;
		if (constantfield != null) {
			if (putinsns.size() != 1) {
				//force inline, but multiple initialization paths
				if (logger != null) {
					if (multipleInitializationLoggedFields
							.add(new FieldKey(transclass.classNode.name, fieldnode.name, fieldnode.desc))) {
						logger.log(new MultipleInitializationPathLogEntry(transclass.classNode.name, fieldnode.name,
								fieldnode.desc));
					}
				}
				return null;
			}
			reconstructioncontext = ReconstructionContext.createConstantField(this, transclass, constantfield,
					clinitmethodnode);
		} else {
			//try to find the receiver type, not really important, but just to be nice
			//array receiver types would only matter if the field is configured as constant, but that's the other branch of this condition
			Type fieldtype = Type.getType(fieldnode.desc);
			Class<?> receivertype = Utils.getReceiverType(fieldtype);
			if (receivertype == null) {
				InlinerTypeReference typeref = constantTypes.get(fieldtype.getInternalName());
				if (typeref != null) {
					receivertype = typeref.getType(this);
				}
			}
			reconstructioncontext = ReconstructionContext.createForReceiverType(this, transclass, receivertype,
					clinitmethodnode);
		}

		List<AsmStackReconstructedValue> results = new ArrayList<>();

		for (AbstractInsnNode ins : putinsns) {
			AbstractInsnNode prev = ins.getPrevious(); // the instruction before PUTSTATIC/PUTFIELD

			AsmStackReconstructedValue nvalue;
			try {
				nvalue = reconstructStackValue(reconstructioncontext, prev);
			} catch (ReconstructionException e) {
				ReconstructionException exc = reconstructioncontext.newMemberInliningReconstructionException(e, ins,
						transclass.classNode.name, fieldnode.name, fieldnode.desc);
				handleReconstructionException(exc);
				continue;
			}
			if (nvalue == null) {
				//failed to reconstruct this assignment
				//so the field value cannot be determined
				return null;
			}
			AsmStackReconstructedValue result = results.isEmpty() ? null : results.get(0);
			if (result != null && !Objects.deepEquals(result.getValue(), nvalue.getValue())) {
				//multiple different values are possibly assigned to the field
				return null;
			}
			results.add(nvalue);
		}

		if (results.isEmpty()) {
			return null;
		}

		return results;
	}

	/**
	 * Reconstruct an array from an array store instruction.
	 * 
	 * @throws ReconstructionException
	 */
	private AsmStackReconstructedValue reconstructArrayStore(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		//it has opcodes something like this:
//51  iconst_4
//52  anewarray java.lang.Object [3]
//55  dup
//56  iconst_0
//57  bipush 123
//59  invokestatic java.lang.Short.valueOf(short) : java.lang.Short [10]
//62  aastore
//63  dup
//64  iconst_1
//65  ldc2_w <Long 456> [11]
//68  invokestatic java.lang.Long.valueOf(long) : java.lang.Long [13]
//71  aastore
//72  dup
//73  iconst_2
//74  ldc <Integer 2147483647> [15]
//76  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [4]
//79  aastore
//80  dup
//81  iconst_3
//82  ldc <String "abc"> [16]
//84  aastore
//85  invokestatic java.lang.String.format(java.lang.String, java.lang.Object[]) : java.lang.String [5]
//88  putstatic testing.sipka.jvm.constexpr.ArrayParameterInlineTest$Constants.len3 : java.lang.String [17]

		final int storeopcode = ins.getOpcode();
		Class<?> arraytype = context.getReceiverType();
		Class<?> componenttype = arraytype == null ? null : arraytype.getComponentType();

		AsmStackReconstructedValue createarrayval = null;
		ReconstructionContext componentcontext = context.withReceiverType(componenttype);
		ReconstructionContext intreceivercontext = context.withReceiverType(int.class);

		//index to element
		List<Entry<AsmStackReconstructedValue, AsmStackReconstructedValue>> elements = new ArrayList<>();

		for (AbstractInsnNode insit = ins; insit != null;) {
			AsmStackReconstructedValue elementval;
			try {
				elementval = reconstructStackValue(componentcontext, insit.getPrevious());
			} catch (ReconstructionException e) {
				//failed to reconstruct the element, index 2
				throw context.newOpcodeReconstructionException(e, ins, 2, storeopcode);
			}
			if (elementval == null) {
				return null;
			}
			AsmStackReconstructedValue idxval;
			try {
				idxval = reconstructStackValue(intreceivercontext, elementval.getFirstIns().getPrevious());
			} catch (ReconstructionException e) {
				//failed to reconstruct the index in the array, index 1
				throw context.newOpcodeReconstructionException(e, ins, 1, storeopcode);
			}
			if (idxval == null) {
				return null;
			}

			AbstractInsnNode idxprev = idxval.getFirstIns().getPrevious();
			if (idxprev.getOpcode() != Opcodes.DUP) {
				//expected DUP for the array reference
				return null;
			}
			elements.add(new AbstractMap.SimpleEntry<>(idxval, elementval));

			AbstractInsnNode previns = idxprev.getPrevious();
			if (previns == null) {
				return null;
			}
			int prevopcode = previns.getOpcode();
			if (prevopcode == Opcodes.ANEWARRAY || prevopcode == Opcodes.NEWARRAY) {
				//creates the array
				try {
					createarrayval = reconstructStackValue(context, previns);
				} catch (ReconstructionException e) {
					//failed to reconstruct the array, index 0
					throw context.newOpcodeReconstructionException(e, ins, 0, storeopcode);
				}
				if (createarrayval == null) {
					return null;
				}
				break;
			}
			if (prevopcode != storeopcode) {
				//the opcode for the previous elements should be same all the way
				return null;
			}
			//get the next element

			insit = previns;
		}
		Object arrayobj = createarrayval.getValue();
		if (arrayobj == null) {
			return null;
		}
		Class<?> arrayclass = arrayobj.getClass();
		if (!arrayclass.isArray()) {
			return null;
		}
		//replace with the actual component type so it is casted correctly when setting the value
		componenttype = arrayclass.getComponentType();

		AsmStackInfo stackinfo = createarrayval.getStackInfo();
		for (ListIterator<Entry<AsmStackReconstructedValue, AsmStackReconstructedValue>> it = elements
				.listIterator(elements.size()); it.hasPrevious();) {
			Entry<AsmStackReconstructedValue, AsmStackReconstructedValue> entry = it.previous();
			AsmStackReconstructedValue idxval = entry.getKey();
			AsmStackReconstructedValue elementval = entry.getValue();

			int index = ((Number) idxval.getValue()).intValue();
			Object element = elementval.getValue();
			Array.set(arrayobj, index, Utils.asmCastValueToReceiverType(element, componenttype));

			AsmStackInfo[] stackinfoelems = stackinfo.getElements().clone();
			stackinfoelems[index] = elementval.getStackInfo();
			stackinfo = AsmStackInfo.createArray(stackinfo.getType(), (AsmStackInfo) stackinfo.getObject(),
					stackinfoelems);
		}
		return new AsmStackReconstructedValue(createarrayval.getFirstIns(), ins.getNext(), stackinfo, arrayobj);
	}

	/**
	 * Reconstructs the value at the given instruction on the stack.
	 * 
	 * @param context
	 *            The reconstruction context.
	 * @param ins
	 *            The instruction at which the value should be reconstructed.
	 * @return The value, or <code>null</code> if the value at the given instruction is not a constant.
	 * @throws ReconstructionException
	 *             If the constant reconstruction failed due to some error.
	 */
	AsmStackReconstructedValue reconstructStackValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		if (ins == null) {
			//no instruction -> no value -> no constant
			return null;
		}
		TransformedClass transformedclass = context.getTransformedClass();
		Class<?> receivertype = context.getReceiverType();
		AbstractInsnNode endins = ins.getNext();
		//loop, so we don't need to recursively call the function to advance the instruction pointer
		while (true) {
			int opcode = ins.getOpcode();
			switch (opcode) {
				case Opcodes.LDC: {
					LdcInsnNode ldc = (LdcInsnNode) ins;
					Object ldcval;
					if (ldc.cst instanceof Type) {
						//convert Type to Class
						try {
							ldcval = findClass((Type) ldc.cst);
						} catch (ClassNotFoundException e) {
							throw context.newClassNotFoundReconstructionException(e, ins,
									((Type) ldc.cst).getInternalName());
						}
					} else {
						ldcval = Utils.asmCastValueToReceiverType(ldc.cst, receivertype);
					}
					return AsmStackReconstructedValue.createConstant(ins, endins, ldcval);
				}
				case Opcodes.BIPUSH:
				case Opcodes.SIPUSH: {
					IntInsnNode intinsn = (IntInsnNode) ins;
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(intinsn.operand, receivertype));
				}
				case Opcodes.ICONST_M1:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(-1, receivertype));
				case Opcodes.ICONST_0:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(0, receivertype));
				case Opcodes.ICONST_1:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(1, receivertype));
				case Opcodes.ICONST_2:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(2, receivertype));
				case Opcodes.ICONST_3:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(3, receivertype));
				case Opcodes.ICONST_4:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(4, receivertype));
				case Opcodes.ICONST_5:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(5, receivertype));
				case Opcodes.LCONST_0:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(0L, receivertype));
				case Opcodes.LCONST_1:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(1L, receivertype));
				case Opcodes.FCONST_0:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(0f, receivertype));
				case Opcodes.FCONST_1:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(1f, receivertype));
				case Opcodes.FCONST_2:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(2f, receivertype));
				case Opcodes.DCONST_0:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(0d, receivertype));
				case Opcodes.DCONST_1:
					return AsmStackReconstructedValue.createConstant(ins, endins,
							Utils.asmCastValueToReceiverType(1d, receivertype));
				case Opcodes.ACONST_NULL:
					return new AsmStackReconstructedValue(ins, endins, AsmStackInfo.createConstant(null), null);

				case Opcodes.CHECKCAST: {
					return reconstructUnaryOperator(context, ins, endins, opcode, receivertype);
				}
				case Opcodes.I2L:
				case Opcodes.I2F:
				case Opcodes.I2D:
				case Opcodes.I2B:
				case Opcodes.I2C:
				case Opcodes.I2S:
				case Opcodes.INEG: {
					return reconstructUnaryOperator(context.withReceiverType(int.class), ins, endins, opcode,
							receivertype);
				}

				case Opcodes.L2I:
				case Opcodes.L2F:
				case Opcodes.L2D:
				case Opcodes.LNEG: {
					return reconstructUnaryOperator(context.withReceiverType(long.class), ins, endins, opcode,
							receivertype);
				}

				case Opcodes.F2I:
				case Opcodes.F2L:
				case Opcodes.F2D:
				case Opcodes.FNEG: {
					return reconstructUnaryOperator(context.withReceiverType(float.class), ins, endins, opcode,
							receivertype);
				}

				case Opcodes.D2I:
				case Opcodes.D2L:
				case Opcodes.D2F:
				case Opcodes.DNEG: {
					return reconstructUnaryOperator(context.withReceiverType(double.class), ins, endins, opcode,
							receivertype);
				}

				case Opcodes.IADD:
				case Opcodes.ISUB:
				case Opcodes.IMUL:
				case Opcodes.IDIV:
				case Opcodes.IREM:
				case Opcodes.ISHL:
				case Opcodes.ISHR:
				case Opcodes.IUSHR:
				case Opcodes.IAND:
				case Opcodes.IOR:
				case Opcodes.IXOR: {
					return reconstructBinaryOperator(context.withReceiverType(int.class), ins, endins, opcode,
							receivertype);
				}

				case Opcodes.LADD:
				case Opcodes.LSUB:
				case Opcodes.LMUL:
				case Opcodes.LDIV:
				case Opcodes.LREM:
				case Opcodes.LSHL:
				case Opcodes.LSHR:
				case Opcodes.LUSHR:
				case Opcodes.LAND:
				case Opcodes.LOR:
				case Opcodes.LXOR: {
					return reconstructBinaryOperator(context.withReceiverType(long.class), ins, endins, opcode,
							receivertype);
				}
				case Opcodes.ARRAYLENGTH: {
					//Object[] as the receiver type should work
					AsmStackReconstructedValue arrayval = reconstructStackValue(
							context.withReceiverType(Object[].class), ins.getPrevious());
					if (arrayval == null) {
						//Note: here we don't need to reconstruct the complete array, only the length of it
						//however, if we can't reconstruct the array, then we can't optimize the array length
						//because some element creation might have side effects
						// -> so we need to reconstruct the complete array nonetheless
						return null;
					}
					int len = Array.getLength(arrayval.getValue());
					return new AsmStackReconstructedValue(arrayval.getFirstIns(), ins.getNext(),
							AsmStackInfo.createArrayLength(arrayval.getStackInfo()), len);
				}
				case Opcodes.NEWARRAY: {
					IntInsnNode intins = (IntInsnNode) ins;

					AsmStackReconstructedValue sizeval;
					try {
						sizeval = reconstructStackValue(context.withReceiverType(int.class), ins.getPrevious());
					} catch (ReconstructionException e) {
						throw context.newArrayCreationFailureReconstructionException(e, ins, Type
								.getDescriptor(Utils.getComponentTypeForAsmNewArrayOperandInstruction(intins.operand)));
					}
					if (sizeval == null) {
						return null;
					}
					int size = ((Number) sizeval.getValue()).intValue();

					Object array = Utils.createAsmArray(size, intins.operand);
					return new AsmStackReconstructedValue(sizeval.getFirstIns(), endins,
							AsmStackInfo.createArray(Type.getType(array.getClass().getComponentType()),
									sizeval.getStackInfo(), new AsmStackInfo[size]),
							array);
				}
				case Opcodes.ANEWARRAY: {
					TypeInsnNode typeins = (TypeInsnNode) ins;

					AsmStackReconstructedValue sizeval;
					try {
						sizeval = reconstructStackValue(context.withReceiverType(int.class), ins.getPrevious());
					} catch (ReconstructionException e) {
						throw context.newArrayCreationFailureReconstructionException(e, ins, typeins.desc);
					}
					if (sizeval == null) {
						//not constant size
						return null;
					}
					int size = ((Number) sizeval.getValue()).intValue();

					//find the actual component type
					Type componentasmtype = Type.getObjectType(typeins.desc);
					Class<?> arraycomponenttyle = Utils.getClassForType(componentasmtype);
					if (arraycomponenttyle == null) {
						if (receivertype != null) {
							Class<?> receivercomponent = receivertype.getComponentType();
							if (receivercomponent != null
									&& Type.getInternalName(receivercomponent).equals(typeins.desc)) {
								arraycomponenttyle = receivercomponent;
							}
						}
						if (arraycomponenttyle == null) {
							try {
								arraycomponenttyle = findClass(componentasmtype);
							} catch (ClassNotFoundException e) {
								throw context.newArrayCreationFailureReconstructionException(
										context.newClassNotFoundReconstructionException(e, ins, typeins.desc), ins,
										typeins.desc, size);
							}
						}
					}
					Object array = Array.newInstance(arraycomponenttyle, size);

					return new AsmStackReconstructedValue(sizeval.firstIns, endins,
							AsmStackInfo.createArray(componentasmtype, sizeval.getStackInfo(), new AsmStackInfo[size]),
							array);
				}
				case Opcodes.BASTORE:
				case Opcodes.SASTORE:
				case Opcodes.IASTORE:
				case Opcodes.LASTORE:
				case Opcodes.FASTORE:
				case Opcodes.DASTORE:
				case Opcodes.CASTORE:
				case Opcodes.AASTORE: {
					//storing to an array
					//this is likely part of a varargs call, or something

					return reconstructArrayStore(context, ins);
				}

				case Opcodes.BALOAD:
				case Opcodes.SALOAD:
				case Opcodes.IALOAD:
				case Opcodes.LALOAD:
				case Opcodes.FALOAD:
				case Opcodes.DALOAD:
				case Opcodes.CALOAD:
				case Opcodes.AALOAD: {
					AsmStackReconstructedValue idxval = reconstructStackValue(context.withReceiverType(int.class),
							ins.getPrevious());
					if (idxval == null) {
						return null;
					}

					ReconstructionContext ncontext = context;
					if (receivertype != null) {
						//XXX make getting the type more efficient?
						ncontext = context.withReceiverType(Array.newInstance(receivertype, 0).getClass());
					}
					AsmStackReconstructedValue arrayval = reconstructStackValue(ncontext,
							idxval.getFirstIns().getPrevious());
					if (arrayval == null) {
						return null;
					}
					Object element = Array.get(arrayval.getValue(), ((Number) idxval.getValue()).intValue());
					return new AsmStackReconstructedValue(arrayval.getFirstIns(), ins.getNext(),
							AsmStackInfo.createArrayLoad(arrayval.getStackInfo(), idxval.getStackInfo()), element);
				}

				case Opcodes.INVOKESTATIC:
				case Opcodes.INVOKEVIRTUAL:
				case Opcodes.INVOKEINTERFACE:
				case Opcodes.INVOKESPECIAL: {
					MethodInsnNode methodins = (MethodInsnNode) ins;
					MethodKey memberkey = new MethodKey(methodins);
					return reconstructValueImpl(context, ins, memberkey);
				}
				case Opcodes.GETSTATIC:
				case Opcodes.GETFIELD: {
					FieldInsnNode fieldins = (FieldInsnNode) ins;
					FieldKey memberkey = new FieldKey(fieldins);
					return reconstructValueImpl(context, fieldins, memberkey);
				}
				case Opcodes.INVOKEDYNAMIC: {
					//handle string concatenation generated on Java 9+
					InvokeDynamicInsnNode dynins = (InvokeDynamicInsnNode) ins;
					return reconstructInvokeDynamic(context, dynins);
				}
				default: {
					switch (ins.getType()) {
						case AbstractInsnNode.LINE: {
							ins = ins.getPrevious();
							continue;
						}
						case AbstractInsnNode.LABEL: {
							LabelNode lins = (LabelNode) ins;
							if (!transformedclass.nonJumpTargetLabelNodes.contains(lins)) {
								//this label is a jump target, therefore we can't properly reconstruct the value
								return null;
							}
							ins = ins.getPrevious();
							continue;
						}
						default: {
							break;
						}
					}
					return null;
				}
			}
		}
	}

	boolean isConstantType(String typeinternalname) {
		return constantTypes.get(typeinternalname) != null;
	}

	Class<?> findClass(Type asmtype) throws ClassNotFoundException {
		switch (asmtype.getSort()) {
			case Type.BOOLEAN:
				return boolean.class;
			case Type.CHAR:
				return char.class;
			case Type.BYTE:
				return byte.class;
			case Type.SHORT:
				return short.class;
			case Type.INT:
				return int.class;
			case Type.FLOAT:
				return float.class;
			case Type.LONG:
				return long.class;
			case Type.DOUBLE:
				return double.class;
			case Type.VOID:
				return void.class;
			case Type.ARRAY: {
				Class<?> type = Array.newInstance(findClass(asmtype.getElementType()), 0).getClass();
				for (int dim = asmtype.getDimensions(); dim > 1; dim--) {
					type = Array.newInstance(type, 0).getClass();
				}
				return type;
			}
			default: {
				break;
			}
		}
		//try searching the configs before attempting the classloader
		Class<?> found = findConfiguredClass(asmtype);
		if (found != null) {
			return found;
		}
		return Class.forName(asmtype.getClassName(), false, classLoader);
	}

	private Class<?> findConfiguredClass(Type asmtype) {
		String typeinternalname = asmtype.getInternalName();
		InlinerTypeReference ct = constantTypes.get(typeinternalname);
		if (ct != null) {
			return ct.getType(this);
		}
		//try searching the reconstructors, if the class may be there
		FieldKey searchfieldkey = new FieldKey(typeinternalname, "", "");
		NavigableMap<MemberKey, TypeReferencedConstantReconstructor> tailmap = constantReconstructors
				.tailMap(searchfieldkey, true);
		for (Entry<MemberKey, TypeReferencedConstantReconstructor> entry : tailmap.entrySet()) {
			MemberKey em = entry.getKey();
			if (!em.getOwner().equals(searchfieldkey.getOwner())) {
				//not found
				break;
			}
			for (Class<?> reftype : entry.getValue().types) {
				if (reftype != null && Type.getInternalName(reftype).equals(typeinternalname)) {
					return reftype;
				}
			}
		}
		return null;
	}

	private AsmStackReconstructedValue reconstructInvokeDynamic(ReconstructionContext context,
			InvokeDynamicInsnNode dynins) throws ReconstructionException {
		//specifically handle the string concatenation that is used from Java 9+
		Handle bootstraphandle = dynins.bsm;
		if (bootstraphandle.getTag() != Opcodes.H_INVOKESTATIC) {
			return null;
		}
		if (!"java/lang/invoke/StringConcatFactory".equals(bootstraphandle.getOwner())) {
			//only this is handled now
			return null;
		}
		if (!"makeConcatWithConstants".equals(bootstraphandle.getName())) {
			return null;
		}
		if (!"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"
				.equals(bootstraphandle.getDesc())) {
			return null;
		}
		Object[] bsmargs = dynins.bsmArgs;
		if (bsmargs.length < 1 || !(bsmargs[0] instanceof String)) {
			//the first argument is the String recipe
			return null;
		}
		String recipe = (String) bsmargs[0];
		int dynargcount = 0;
		int recipelen = recipe.length();
		for (int i = 0; i < recipelen; i++) {
			if (recipe.charAt(i) == '\u0001') {
				dynargcount++;
			}
		}
		Object[] args = new Object[dynargcount];
		AsmStackReconstructedValue[] derivedargs = new AsmStackReconstructedValue[dynargcount];
		try {
			if (!reconstructArguments(context.forArgumentReconstruction(), null, dynins, args, derivedargs)) {
				return null;
			}
		} catch (ReconstructionException e) {
			throw context.newMethodArgumentsReconstructionException(e, dynins, bootstraphandle.getOwner(),
					bootstraphandle.getName(), bootstraphandle.getDesc());
		}
		//successfully reconstructed.
		//perform the concatenation ourselves

		//XXX could make this more efficient rather than iterating over all characters
		StringBuilder sb = new StringBuilder();
		int dynargidx = 1; // first is the recipe
		int stackargidx = 0;
		for (int i = 0; i < recipelen; i++) {
			char c = recipe.charAt(i);
			switch (c) {
				case '\u0001':
					Object stackarg = derivedargs[stackargidx++].getValue();
					sb.append(stackarg);
					break;
				case '\u0002':
					Object dynarg = bsmargs[dynargidx++];
					if (dynarg instanceof Type) {
						//TODO log unsupported
						return null;
					} else if (dynarg instanceof Handle) {
						//TODO log unsupported
						return null;
					} else {
						sb.append(dynarg);
					}
					break;
				default:
					sb.append(c);
					break;
			}
		}

		return new AsmStackReconstructedValue(derivedargs.length == 0 ? dynins : derivedargs[0].getFirstIns(),
				dynins.getNext(),
				AsmStackInfo.createStaticMethod(Type.getObjectType(bootstraphandle.getOwner()),
						bootstraphandle.getName(), Type.getMethodType(bootstraphandle.getDesc()),
						AsmStackReconstructedValue.toStackInfoArray(derivedargs)),
				sb.toString());
	}

	private AsmStackReconstructedValue reconstructBinaryOperator(ReconstructionContext operandcontext,
			AbstractInsnNode ins, AbstractInsnNode endins, int opcode, Class<?> receivertype)
			throws ReconstructionException {
		AsmStackReconstructedValue rightop;
		try {
			rightop = reconstructStackValue(operandcontext, ins.getPrevious());
		} catch (ReconstructionException e) {
			throw operandcontext.newOpcodeReconstructionException(e, ins, 0, opcode);
		}
		if (rightop == null) {
			return null;
		}
		AsmStackReconstructedValue leftop;
		try {
			leftop = reconstructStackValue(operandcontext, rightop.getFirstIns().getPrevious());
		} catch (ReconstructionException e) {
			throw operandcontext.newOpcodeReconstructionException(e, ins, 1, opcode);
		}
		if (leftop == null) {
			return null;
		}
		Object value = Utils.applyBinaryOperand(opcode, leftop.getValue(), rightop.getValue());
		if (value == null) {
			return null;
		}
		value = Utils.asmCastValueToReceiverType(value, receivertype);
		return new AsmStackReconstructedValue(leftop.getFirstIns(), endins, AsmStackInfo.createOperator(opcode, null,
				new AsmStackInfo[] { leftop.getStackInfo(), rightop.getStackInfo() }), value);
	}

	private AsmStackReconstructedValue reconstructUnaryOperator(ReconstructionContext operandcontext,
			AbstractInsnNode ins, AbstractInsnNode endins, int opcode, Class<?> receivertype)
			throws ReconstructionException {
		AsmStackReconstructedValue val;
		try {
			val = reconstructStackValue(operandcontext, ins.getPrevious());
		} catch (ReconstructionException e) {
			throw operandcontext.newOpcodeReconstructionException(e, ins, 0, opcode);
		}
		if (val == null) {
			return null;
		}
		Object prevval = val.getValue();
		Object nval = Utils.applyUnaryOperand(opcode, prevval);
		if (nval == null && prevval != null) {
			//failed to apply operand on a non-null
			return null;
		}
		Type checkcasttype;
		if (opcode == Opcodes.CHECKCAST) {
			TypeInsnNode typeins = (TypeInsnNode) ins;
			checkcasttype = Type.getObjectType(typeins.desc);
		} else {
			checkcasttype = null;
		}
		nval = Utils.asmCastValueToReceiverType(nval, receivertype);
		return new AsmStackReconstructedValue(val.firstIns, endins,
				AsmStackInfo.createOperator(opcode, checkcasttype, new AsmStackInfo[] { val.getStackInfo() }), nval);
	}

	private boolean inlineFieldValue(TransformedClass fieldowner, TransformedField transfield,
			TransformedClass transclass) {
		Object val = transfield.calculatedConstantValue.orElse(null);
		if (!Utils.isConstantInlineableAsLdc(val, Type.getType(transfield.fieldNode.desc))) {
			//can't inline the value of this field
			//probably because some custom class
			return false;
		}

		FieldNode fieldnode = transfield.fieldNode;
		boolean any = false;
		for (MethodNode mn : transclass.classNode.methods) {
			InsnList instructions = mn.instructions;
			for (AbstractInsnNode ins = instructions.getFirst(); ins != null;) {
				AbstractInsnNode next = ins.getNext();
				switch (ins.getOpcode()) {
					case Opcodes.GETSTATIC: {
						FieldInsnNode fieldins = (FieldInsnNode) ins;
						if (!fieldins.name.equals(fieldnode.name)
								|| !fieldins.owner.equals(fieldowner.classNode.name)) {
							//different field
							break;
						}
						AbstractInsnNode addins;
						if (val == null) {
							addins = new InsnNode(Opcodes.ACONST_NULL);
						} else {
							addins = new LdcInsnNode(val);
						}
						AsmStackInfo replacementinfo = AsmStackInfo.createConstant(val);
						instructions.insert(ins, addins);
						instructions.remove(ins);

						if (logger != null) {
							logger.log(new InstructionReplacementLogEntry(
									Utils.getBytecodeLocation(transclass, mn, addins),
									AsmStackInfo.createStaticField(Type.getObjectType(fieldins.owner), fieldins.name,
											Type.getType(fieldins.desc)),
									replacementinfo, val));
						}

						any = true;
						break;
					}
					default: {
						break;
					}
				}
				ins = next;
			}
		}
		return any;
	}

	private boolean inlineFieldInitializerValueFromStaticInitializers(TransformedClass transclass,
			TransformedField transfield, MethodNode clinitmethodnode) {
		if (transfield.calculatedConstantValue != null) {
			//value already set
			return false;
		}
		FieldNode fieldnode = transfield.fieldNode;
		if (((fieldnode.access & (Opcodes.ACC_FINAL | Opcodes.ACC_STATIC)) != (Opcodes.ACC_FINAL
				| Opcodes.ACC_STATIC))) {
			//not static final field
			return false;
		}
		Field constantfield = optionsConstantFields
				.get(new FieldKey(transclass.classNode.name, fieldnode.name, fieldnode.desc));

		Collection<AsmStackReconstructedValue> nvalues = getAssignedFieldValuesFromMethod(transclass, fieldnode,
				clinitmethodnode, constantfield);
		if (nvalues == null) {
			return false;
		}
		AsmStackReconstructedValue reconstructedval = nvalues.iterator().next();
		Object constantval = reconstructedval.getValue();
		DeconstructionResult deconsresult = null;

		InsnList instructions = clinitmethodnode.instructions;
		if (transfield.setCalculatedConstantValue(constantval)) {
			//ok, no deconstruction necessary

			AsmStackInfo fieldasminfo = null;
			BytecodeLocation bytecodelocation = null;
			if (logger != null) {
				//log for all locations that were updated
				fieldasminfo = AsmStackInfo.createStaticField(Type.getObjectType(transclass.classNode.name),
						fieldnode.name, Type.getType(fieldnode.desc));

				bytecodelocation = new BytecodeLocation(transclass.input, transclass.classNode.name, fieldnode.name,
						fieldnode.desc, -1);
			}

			for (AsmStackReconstructedValue val : nvalues) {
				val.removeInstructions(instructions);

				//remove the PUTSTATIC as well
				instructions.remove(val.getLastIns());
				if (logger != null) {
					logger.log(new InstructionReplacementLogEntry(bytecodelocation, val.getStackInfo(), fieldasminfo,
							constantval));
				}
			}

		} else {
			ConstantDeconstructor deconstructor = getConstantDeconstructor(constantval);
			if (deconstructor == null) {
				//return true, as even if we failed to deconstruct the value, we still assigned it to the constant value
				// of the transformed field
				return true;
			}
			deconsresult = deconstructor.deconstructValue(this, transclass, clinitmethodnode, constantval);
			if (deconsresult == null) {
				//did not deconstruct the value, so we can't replace the instructions in the static initializer
				return true;
			}

			for (AsmStackReconstructedValue val : nvalues) {
				if (val.getStackInfo().equals(deconsresult.getStackInfo())) {
					//no need for replacement in this case, the reconstructed stack equals the deconstructed insructions
					continue;
				}
				val.removeInstructions(instructions);
				//insert before the PUTSTATIC instruction
				instructions.insertBefore(val.getLastIns(), Utils.clone(deconsresult.getInstructions()));
				if (logger != null) {
					//log for all locations that were updated
					logger.log(new InstructionReplacementLogEntry(
							Utils.getBytecodeLocation(transclass, clinitmethodnode, val.getLastIns()),
							val.getStackInfo(), deconsresult.getStackInfo(), constantval));
				}
			}

		}

		return true;
	}

	private static boolean replacePrimitiveTypeGetStaticInstructions(MethodNode methodnode) {
		boolean any = false;
		for (ListIterator<AbstractInsnNode> it = methodnode.instructions.iterator(); it.hasNext();) {
			AbstractInsnNode ins = it.next();

			switch (ins.getOpcode()) {
				case Opcodes.GETSTATIC: {
					//replace Integer.TYPE with int.class
					FieldInsnNode fieldins = (FieldInsnNode) ins;
					Class<?> boxtype;
					if ("TYPE".equals(fieldins.name) && "Ljava/lang/Class;".equals(fieldins.desc)
							&& (boxtype = Utils.getPrimitiveClassForBoxedTypeInternalName(fieldins.owner)) != null) {
						Type type = Type.getType(boxtype);
						LdcInsnNode ldcins = new LdcInsnNode(type);
						it.set(ldcins);
						any = true;
					}
					break;
				}
			}
		}
		return any;
	}

	private static Object castValueFromAsm(Type type, Object value) {
		if (value == null) {
			return null;
		}
		if (type == null) {
			return value;
		}
		switch (type.getSort()) {
			case Type.BOOLEAN:
				if (value instanceof Boolean) {
					return (boolean) value;
				}
				//might be from FieldNode.value, which is not stored as Boolean, but integer
				return ((Number) value).intValue() != 0;
			case Type.CHAR:
				if (value instanceof Character) {
					return (char) value;
				}
				//might be from FieldNode.value, which is not stored as Character, but integer
				return (char) ((Number) value).intValue();
			case Type.BYTE:
				return ((Number) value).byteValue();
			case Type.SHORT:
				return ((Number) value).shortValue();
			case Type.INT:
				return ((Number) value).intValue();
			case Type.FLOAT:
				return ((Number) value).floatValue();
			case Type.LONG:
				return ((Number) value).longValue();
			case Type.DOUBLE:
				return ((Number) value).doubleValue();
			case Type.VOID: // VOID shouldn't really happen
			case Type.ARRAY:
			case Type.OBJECT:
				return value;
			default:
				throw new AssertionError(type);
		}
	}

	/**
	 * Deconstruct the given value with a target type.
	 * 
	 * @param transclass
	 *            The transformed class.
	 * @param val
	 *            The value to deconstruct.
	 * @param methodnode
	 *            The method in which the deconstruction is happening.
	 * @param type
	 *            The target type on the stack.
	 * @return The deconstructed instructions or <code>null</code> if the deconstruction failed.
	 */
	DeconstructionResult deconstructValue(TransformedClass transclass, MethodNode methodnode, Object val, Type type) {
		switch (type.getSort()) {
			case Type.VOID:
			case Type.BOOLEAN:
			case Type.CHAR:
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.FLOAT:
			case Type.LONG:
			case Type.DOUBLE: {
				//no boxing needed
				InsnList result = new InsnList();
				//cast it, so in case of long, double, etc... the proper constant is loaded
				Object cval = castValueFromAsm(type, val);
				result.add(new LdcInsnNode(cval));
				return DeconstructionResult.createConstant(result, cval);
			}
			case Type.OBJECT:
			case Type.ARRAY: {
				if (val == null) {
					InsnList result = new InsnList();
					result.add(new InsnNode(Opcodes.ACONST_NULL));
					return DeconstructionResult.createConstant(result, null);
				}
				if (val instanceof Class) {
					InsnList result = new InsnList();
					result.add(new LdcInsnNode(Type.getType((Class<?>) val)));
					return DeconstructionResult.createConstant(result, val);
				}
				ConstantDeconstructor deconstructor = getConstantDeconstructor(val);
				if (deconstructor == null) {
					//no deconstructor found for this type, can't perform inlining
					if (logger != null) {
						String typeinternalname = Type.getInternalName(val.getClass());
						deconstructorNotConfiguredLogEntries.putIfAbsent(typeinternalname,
								new DeconstructorNotConfiguredLogEntry(typeinternalname));
					}
					return null;
				}
				return deconstructor.deconstructValue(this, transclass, methodnode, val);
			}
			default:
				throw new AssertionError(type);
		}
	}

	private boolean performInstructionInlining(TransformedClass transclass, MethodNode methodnode) {
		boolean any = false;
		InsnList instructions = methodnode.instructions;
		AbstractInsnNode nextnode;
		for (AbstractInsnNode ins = instructions.getFirst(); ins != null; ins = nextnode) {
			nextnode = ins.getNext();

			if (transclass.inlinedInstructions.contains(ins)) {
				//already inlined at this instruction, dont deconstruct and reconstruct again
				continue;
			}

			Type rettype;
			AsmStackReconstructedValue reconstructedval;
			int opcode = ins.getOpcode();
			switch (opcode) {
				case -1: {
					//some line number, or other node
					continue;
				}

				case Opcodes.INVOKEVIRTUAL:
				case Opcodes.INVOKEINTERFACE:
				case Opcodes.INVOKESTATIC:
				case Opcodes.INVOKESPECIAL: {
					MethodInsnNode methodins = (MethodInsnNode) ins;

					rettype = Utils.getInstructionResultAsmType(ins);
					if (rettype.getSort() == Type.VOID) {
						//no inlining for functions that return void
						continue;
					}

					ReconstructionContext reconstructioncontext = ReconstructionContext.createForReceiverType(this,
							transclass, Utils.getClassForType(rettype), methodnode);
					MethodKey methodkey = new MethodKey(methodins);
					try {
						reconstructedval = reconstructValueImpl(reconstructioncontext, ins, methodkey);
					} catch (ReconstructionException e) {
						ReconstructionException exc = reconstructioncontext.newMemberInliningReconstructionException(e,
								ins, methodins.owner, methodins.name, methodins.desc);
						handleReconstructionException(exc);
						continue;
					}
					break;
				}
				case Opcodes.LDC:
				case Opcodes.ACONST_NULL:
				case Opcodes.BIPUSH:
				case Opcodes.SIPUSH:
				case Opcodes.ICONST_M1:
				case Opcodes.ICONST_0:
				case Opcodes.ICONST_1:
				case Opcodes.ICONST_2:
				case Opcodes.ICONST_3:
				case Opcodes.ICONST_4:
				case Opcodes.ICONST_5:
				case Opcodes.LCONST_0:
				case Opcodes.LCONST_1:
				case Opcodes.FCONST_0:
				case Opcodes.FCONST_1:
				case Opcodes.FCONST_2:
				case Opcodes.DCONST_0:
				case Opcodes.DCONST_1: {
					//nothing to inline for these constant loading instructions, just mark as processed
					transclass.inlinedInstructions.add(ins);
					continue;
				}
				case Opcodes.BASTORE:
				case Opcodes.SASTORE:
				case Opcodes.IASTORE:
				case Opcodes.LASTORE:
				case Opcodes.FASTORE:
				case Opcodes.DASTORE:
				case Opcodes.CASTORE:
				case Opcodes.AASTORE:

				case Opcodes.BALOAD:
				case Opcodes.SALOAD:
				case Opcodes.IALOAD:
				case Opcodes.LALOAD:
				case Opcodes.FALOAD:
				case Opcodes.DALOAD:
				case Opcodes.CALOAD:
				case Opcodes.AALOAD: {
					//not handled, these cannot really be optimized in place
					continue;
				}
				case Opcodes.CHECKCAST: {
					//don't optimize this individually
					continue;
				}
				case Opcodes.GETSTATIC: {
					//nothing to optimize on this one, GETSTATIC values are replaced when a static final value is inlined
					continue;
				}
				default: {
					//generic inlining
					rettype = Utils.getInstructionResultAsmType(ins);
					if (rettype != null && rettype.getSort() == Type.VOID) {
						//no inlining for functions that return void
						continue;
					}
					ReconstructionContext reconstructioncontext = ReconstructionContext.createForReceiverType(this,
							transclass, Utils.getClassForType(rettype), methodnode);

					try {
						reconstructedval = reconstructStackValue(reconstructioncontext, ins);
					} catch (ReconstructionException e) {
						handleReconstructionException(e);
						continue;
					}
					break;
				}
			}
			if (reconstructedval == null) {
				continue;
			}
			Object inlineval = reconstructedval.getValue();

			DeconstructionResult deconsresult = deconstructValue(transclass, methodnode, inlineval, rettype);
			if (deconsresult == null) {
				//failed to deconstruct
				//mark the instruction as inlined, so we don't process it again
				//if the deconstruction failed once, it is expected to fail again the next time
				transclass.inlinedInstructions.add(ins);
				continue;
			}
			InsnList deconstructedinstructions = deconsresult.getInstructions();
			AbstractInsnNode lastdeconins = deconstructedinstructions.getLast();

			if (reconstructedval.getStackInfo().equals(deconsresult.getStackInfo())) {
				//the reconstructed instruction is the same as the one we're processing
				//don't replace the instructions
				transclass.inlinedInstructions.add(lastdeconins);
				continue;
			}

			instructions.insertBefore(reconstructedval.getFirstIns(), deconstructedinstructions);

			reconstructedval.removeInstructions(instructions);

			transclass.inlinedInstructions.add(lastdeconins);

			if (logger != null) {
				logger.log(new InstructionReplacementLogEntry(
						Utils.getBytecodeLocation(transclass, methodnode, lastdeconins),
						reconstructedval.getStackInfo(), deconsresult.getStackInfo(), inlineval));
			}
		}
		return any;
	}

	private AsmStackReconstructedValue reconstructValueImpl(ReconstructionContext context, AbstractInsnNode ins,
			MemberKey memberkey) throws ReconstructionException {
		ConstantReconstructor reconstructor = constantReconstructors.get(memberkey);
		if (reconstructor != null) {
			return reconstructor.reconstructValue(context, ins);
		}

		int opcode = ins.getOpcode();
		switch (opcode) {
			case Opcodes.INVOKEVIRTUAL:
			case Opcodes.INVOKEINTERFACE: {
				MethodInsnNode methodins = (MethodInsnNode) ins;

				reconstructor = constantReconstructors.get(new MethodKey("", methodins.name, methodins.desc));
				if (reconstructor != null) {
					return reconstructor.reconstructValue(context, ins);
				}

				if ("toString".equals(methodins.name) && "()Ljava/lang/String;".equals(methodins.desc)) {
					//handle toString specially
					//if the reconstruction succeeds, then we can call toString on it and inline that result
					return ToStringConstantReconstructor.INSTANCE.reconstructValue(context, methodins);
				}
				InlinerTypeReference typeref = constantTypes.get(methodins.owner);
				if (typeref != null) {
					Class<?> type = typeref.getType(this);
					if (type != null) {
						if ("()I".equals(methodins.desc)) {
							if ("hashCode".equals(methodins.name)) {
								//don't inline hashCode by default on constant types, as that might not be stable
								break;
							}
							if ("ordinal".equals(methodins.name) && Enum.class.isAssignableFrom(type)) {
								//ordinal reconstruction disabled for now, as it depends on source compatibility
								break;
							}
						}
						Method m;
						try {
							m = Utils.getMethodForInstruction(type, methodins);
						} catch (NoSuchMethodException e) {
							throw context.newMethodNotFoundReconstructionException(e, methodins, methodins.owner,
									methodins.name, methodins.desc);
						}
						return new MethodBasedConstantReconstructor(m, ReconstructorPredicate.ALLOW_ALL)
								.reconstructValue(context, ins);
					}
				}

				break;
			}
			case Opcodes.INVOKESPECIAL: {
				MethodInsnNode methodins = (MethodInsnNode) ins;
				if (Utils.CONSTRUCTOR_METHOD_NAME.equals(methodins.name)) {
					//constructor, allow if this is a constant type
					InlinerTypeReference typeref = constantTypes.get(methodins.owner);
					if (typeref != null) {
						Class<?> type = typeref.getType(this);
						if (type != null) {
							Constructor<?> constructor;
							try {
								constructor = Utils.getConstructorForMethodDescriptor(type, methodins.desc);
							} catch (NoSuchMethodException e) {
								throw context.newMethodNotFoundReconstructionException(e, methodins, methodins.owner,
										methodins.name, methodins.desc);
							}
							return new ConstructorBasedConstantReconstructor(constructor).reconstructValue(context,
									ins);
						}
					}
				}
				break;
			}
			case Opcodes.INVOKESTATIC: {
				//nothing extra
				break;
			}
			case Opcodes.GETSTATIC: {
				FieldKey fieldkey = (FieldKey) memberkey;
				FieldInsnNode fieldins = (FieldInsnNode) ins;

				TransformedClass fieldownertransclass = inputClasses.get(memberkey.getOwner());
				if (fieldownertransclass != null) {
					TransformedField transfield = fieldownertransclass.getTransformedField(fieldins.desc,
							fieldins.name);
					if (transfield != null && transfield.calculatedConstantValue != null) {
						//the field should exist, but null check just in case

						Object constval = transfield.calculatedConstantValue.orElse(null);
						if (constval == null || constantTypes.get(Type.getInternalName(constval.getClass())) != null
								|| optionsConstantFields.get(fieldkey) != null) {
							//only return if the type of the value is a constant type, otherwise it might get modified by other code
							//and thus result in us using different values
							return new SimpleConstantReconstructor(constval, AsmStackInfo.createStaticField(
									Type.getObjectType(fieldins.owner), fieldins.name, Type.getType(fieldins.desc)))
											.reconstructValue(context, ins);
						}
					}
				}

				Field enumfield = findEnumConstantField(fieldins);
				if (enumfield != null) {
					return new FieldBasedConstantReconstructor(enumfield).reconstructValue(context, ins);
				}
				break;
			}
			case Opcodes.GETFIELD: {
				//getting field of an object
				//the field is not marked as constant, otherwise a constant reconstructor would be set for it
				FieldInsnNode fieldins = (FieldInsnNode) ins;
				if (constantTypes.get(fieldins.owner) != null) {
					//getting field of a constant type
					//allow it
					return DynamicInstanceFieldBasedConstantReconstructor.INSTANCE.reconstructValue(context, ins);
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unknown opcode for constant reconstructor: " + opcode);
		}

		if (context.isForceReconstruct()) {
			switch (opcode) {
				case Opcodes.GETFIELD: {
					return DynamicInstanceFieldBasedConstantReconstructor.INSTANCE.reconstructValue(context, ins);
				}
				case Opcodes.GETSTATIC: {
					FieldInsnNode fieldins = (FieldInsnNode) ins;
					Class<?> type;
					Type ownertype = Type.getObjectType(memberkey.getOwner());
					try {
						type = findClass(ownertype);
					} catch (ClassNotFoundException e) {
						throw context.newClassNotFoundReconstructionException(e, ins, memberkey.getOwner());
					}
					Field field;
					try {
						field = type.getDeclaredField(memberkey.getMemberName());
						field.setAccessible(true);
					} catch (NoSuchFieldException e) {
						throw context.newFieldNotFoundReconstructionException(e, ins, fieldins.owner, fieldins.name,
								fieldins.desc);
					}
					return new FieldBasedConstantReconstructor(field).reconstructValue(context, ins);
				}
				default: {
					MethodInsnNode methodins = (MethodInsnNode) ins;
					Class<?> type;
					Type ownertype = Type.getObjectType(memberkey.getOwner());
					try {
						type = findClass(ownertype);
					} catch (ClassNotFoundException e) {
						throw context.newClassNotFoundReconstructionException(e, ins, memberkey.getOwner());
					}

					MethodKey methodkey = (MethodKey) memberkey;
					String methodname = memberkey.getMemberName();
					if (Utils.CONSTRUCTOR_METHOD_NAME.equals(methodname)) {
						Constructor<?> constructor;
						try {
							constructor = Utils.getConstructorForMethodDescriptor(type,
									methodkey.getMethodDescriptor());
						} catch (NoSuchMethodException e) {
							throw context.newMethodNotFoundReconstructionException(e, methodins, methodins.owner,
									methodins.name, methodins.desc);
						}
						return new ConstructorBasedConstantReconstructor(constructor).reconstructValue(context, ins);
					}
					Method method;
					try {
						method = Utils.getMethodForInstruction(type, methodins);
					} catch (NoSuchMethodException e) {
						throw context.newMethodNotFoundReconstructionException(e, methodins, methodins.owner,
								methodins.name, methodins.desc);
					}
					return new MethodBasedConstantReconstructor(method, ReconstructorPredicate.ALLOW_ALL)
							.reconstructValue(context, ins);
				}
			}
		}
		//INSERT HERE: any further specially handled method calls
		return null;
	}

	private Field findEnumConstantField(FieldInsnNode fieldins) {
		Type fieldtype = Type.getType(fieldins.desc);
		Type ownertype = Type.getObjectType(fieldins.owner);
		if (!fieldtype.equals(ownertype)) {
			//the declaring class is different than the field type, so it can't be an enum constant
			return null;
		}
		InlinerTypeReference typeref = constantTypes.get(fieldtype.getInternalName());
		if (typeref != null) {
			Class<?> type = typeref.getType(this);
			if (type != null) {
				try {
					Field field = type.getField(fieldins.name);
					if (field.isEnumConstant()) {
						return field;
					}
					return null;
				} catch (NoSuchFieldException e) {
					//no such field
					//strange, because it seems to be referenced from other code,
					//but we can't do much about this
				}
			}
			return null;
		}
		//the type is not a constant type
		//try to find it, by searching for the constant reconstructors
		//the field key for the tail map only contains the internal name, as we accept any of the reconstructors
		//that are associated with this owner type
		FieldKey searchfieldkey = new FieldKey(ownertype.getInternalName(), "", "");
		NavigableMap<MemberKey, TypeReferencedConstantReconstructor> tailmap = constantReconstructors
				.tailMap(searchfieldkey, true);
		for (Entry<MemberKey, TypeReferencedConstantReconstructor> entry : tailmap.entrySet()) {
			MemberKey em = entry.getKey();
			if (!em.getOwner().equals(searchfieldkey.getOwner())) {
				//not found
				break;
			}
			//this reconstructor is associated with the same owner type that the field we're looking for
			for (Class<?> type : entry.getValue().types) {
				if (type == null) {
					continue;
				}
				try {
					Field field = type.getField(fieldins.name);
					if (field.isEnumConstant()) {
						return field;
					}
					return null;
				} catch (NoSuchFieldException e) {
					//no such field
					//strange, because it seems to be referenced from other code,
					//but we can't do much about this
					return null;
				}
			}
		}
		return null;
	}

	private ConstantDeconstructor getConstantDeconstructor(Object value) {
		Class<?> valclass = value.getClass();
		if (valclass.isArray()) {
			return ArrayConstantDeconstructor.INSTANCE;
		}
		ConstantDeconstructor deconstructor = constantDeconstructors.get(Type.getInternalName(valclass));
		if (deconstructor != null) {
			return deconstructor;
		}
		if (value instanceof Enum) {
			return EnumFieldConstantDeconstructor.INSTANCE;
		}
		return null;
	}

	private static List<LogContextInfo> getLogContextInfo(ReconstructionException e) {
		List<LogContextInfo> result = new ArrayList<>();
		for (Throwable it = e; it instanceof ReconstructionException; it = it.getCause()) {
			result.add(((ReconstructionException) it).getContextInfo());
		}
		return result;
	}

	private static Throwable getRootCause(ReconstructionException e) {
		for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
			if (!(cause instanceof ReconstructionException)) {
				return cause;
			}
		}
		return null;
	}

	private void handleReconstructionException(ReconstructionException e) {
		if (logger == null) {
			//no need for logging
			return;
		}
		List<LogContextInfo> loginfocontext = getLogContextInfo(e);
		if (reconstructionFailureLogEntries.containsKey(loginfocontext)) {
			//a log entry is already present for this trace
			return;
		}
		//we're recording either
		// - a new log context info stack
		// - or a log context info stack that is larger than one that is present
		//add this entry
		//and overwrite all entries that have a smaller but equal substack
		//this is to avoid duplicate log entries that clutter the output unnecessarily
		int contextcount = loginfocontext.size();
		Throwable rootcause = getRootCause(e);
		ReconstructionFailureLogEntry logentry = new ReconstructionFailureLogEntry(rootcause, loginfocontext);
		reconstructionFailureLogEntries.put(loginfocontext, logentry);
		for (int i = 1; i < contextcount; i++) {
			List<LogContextInfo> subcontext = loginfocontext.subList(i, contextcount);
			ReconstructionFailureLogEntry sublogentry = new ReconstructionFailureLogEntry(rootcause, subcontext);
			reconstructionFailureLogEntries.put(subcontext, sublogentry);
		}
	}

	void logConfigClassMemberInaccessible(String classInternalName, String memberName, String memberDescriptor,
			Throwable exception) {
		if (logger == null) {
			//no need for logging
			return;
		}
		String key = classInternalName;
		if (memberName != null) {
			key += "\t" + memberName + "\t" + memberDescriptor;
		}
		if (!configMemberNotAvailableLoggedEntries.add(key)) {
			//already logged
			return;
		}
		logger.log(
				new ConfigClassMemberInaccessibleLogEntry(classInternalName, memberName, memberDescriptor, exception));
	}

	void logDeconstructionFailure(Object value, DeconstructionDataAccessor accessor, Throwable cause) {
		if (logger == null) {
			//no need for logging
			return;
		}
		logger.log(new DeconstructionFailedLogEntry(value, accessor, cause));
	}

	void logIndeterministicToString(String classinternalname) {
		if (logger == null) {
			//no need for logging
			return;
		}
		toStringLogEntries.put(classinternalname, new IndeterministicToStringLogEntry(classinternalname));
	}

	void logReconstructionNotAllowed(Object obj, Member member, Object[] args) {
		//TODO do we need to log this?
	}
}
