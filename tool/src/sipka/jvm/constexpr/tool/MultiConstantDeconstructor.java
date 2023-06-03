package sipka.jvm.constexpr.tool;

import java.util.ArrayList;
import java.util.List;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

final class MultiConstantDeconstructor implements ConstantDeconstructor {
	protected final List<? extends ConstantDeconstructor> deconstructors;

	private MultiConstantDeconstructor(List<? extends ConstantDeconstructor> deconstructors) {
		this.deconstructors = deconstructors;
	}

	public List<? extends ConstantDeconstructor> getDeconstructors() {
		return deconstructors;
	}

	public ConstantDeconstructor replacedAt(int index, ConstantDeconstructor deconstructor) {
		List<ConstantDeconstructor> nlist = new ArrayList<>(this.deconstructors);
		if (deconstructor == null) {
			nlist.remove(index);
			if (nlist.isEmpty()) {
				return null;
			}
			if (nlist.size() == 1) {
				return nlist.get(0);
			}
		} else {
			nlist.set(index, deconstructor);
		}
		return new MultiConstantDeconstructor(nlist);
	}

	public static ConstantDeconstructor getMulti(ConstantDeconstructor... deconstructors) {
		if (deconstructors.length == 0) {
			return null;
		}

		List<ConstantDeconstructor> createdelegates = new ArrayList<>();
		for (int i = 0; i < deconstructors.length; i++) {
			ConstantDeconstructor selector = deconstructors[i];
			if (selector == null) {
				continue;
			}
			if (selector instanceof MultiConstantDeconstructor) {
				createdelegates.addAll(((MultiConstantDeconstructor) selector).deconstructors);
			} else {
				createdelegates.add(selector);
			}
		}
		if (createdelegates.isEmpty()) {
			return null;
		}
		if (createdelegates.size() == 1) {
			return createdelegates.get(0);
		}
		return new MultiConstantDeconstructor(createdelegates);
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		for (ConstantDeconstructor decons : deconstructors) {
			DeconstructionResult result = decons.deconstructValue(context, transclass, methodnode, value);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[deconstructors=");
		builder.append(deconstructors);
		builder.append("]");
		return builder.toString();
	}

}
