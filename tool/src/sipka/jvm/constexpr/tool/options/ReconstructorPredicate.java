package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

import sipka.jvm.constexpr.tool.Utils;

/**
 * Predicate to test if the specified member can be used on the given object for constant reconstruction.
 * <p>
 * Clients may implement this interface.
 */
public interface ReconstructorPredicate {
	/**
	 * Singleton instance that allows all reconstruction for the associated member.
	 */
	public static final ReconstructorPredicate ALLOW_ALL = new ReconstructorPredicate() {
		@Override
		public boolean canReconstruct(Object obj, Member member, Object[] parameters) {
			return true;
		}

		@Override
		public String toString() {
			return ReconstructorPredicate.class.getSimpleName() + "[ALLOW_ALL]";
		}
	};
	/**
	 * Singleton instance that only allows reconstruction where the type of the object is the same as the
	 * {@linkplain Member#getDeclaringClass() declaring class} of the member.
	 * <p>
	 * Only recommended to be used with instance methods.
	 */
	public static final ReconstructorPredicate ALLOW_EXACT_TYPE = new ReconstructorPredicate() {
		@Override
		public boolean canReconstruct(Object obj, Member member, Object[] parameters) {
			return obj != null && obj.getClass() == member.getDeclaringClass();
		}

		@Override
		public String toString() {
			return ReconstructorPredicate.class.getSimpleName() + "[ALLOW_EXACT_TYPE]";
		}
	};
	/**
	 * Singleton instance that only allows reconstruction where the object is instance of the
	 * {@linkplain Member#getDeclaringClass() declaring class} of the member.
	 * <p>
	 * Only recommended to be used with instance methods.
	 */
	public static final ReconstructorPredicate ALLOW_INSTANCE_OF = new ReconstructorPredicate() {
		@Override
		public boolean canReconstruct(Object obj, Member member, Object[] parameters) {
			return member.getDeclaringClass().isInstance(obj);
		}

		@Override
		public String toString() {
			return ReconstructorPredicate.class.getSimpleName() + "[ALLOW_INSTANCE_OF]";
		}
	};

	/**
	 * Tests if the given member can be accessed (or invoked) on the given object, with the specified parameters.
	 * 
	 * @param obj
	 *            The object to access (or invoke) the specified member. May be <code>null</code> if the member is a
	 *            static member or the member is a {@link Constructor}.
	 * @param member
	 *            The member to be accessed or invoked.
	 * @param arguments
	 *            The arguments passed to the invoked method or constructor. <code>null</code> in case of {@link Field
	 *            Fields}.
	 * @return <code>true</code> if the member can be used.
	 */
	public boolean canReconstruct(Object obj, Member member, Object[] arguments);

	public default ReconstructorPredicate or(ReconstructorPredicate other) {
		return new ReconstructorPredicate() {
			@Override
			public boolean canReconstruct(Object obj, Member member, Object[] arguments) {
				return ReconstructorPredicate.this.canReconstruct(obj, member, arguments)
						|| other.canReconstruct(obj, member, arguments);
			}

			@Override
			public String toString() {
				return ReconstructorPredicate.class.getSimpleName() + "[" + ReconstructorPredicate.this + " || " + other
						+ "]";
			}
		};
	}

	public static ReconstructorPredicate allowInstanceOf(Class<?> type) {
		return new ReconstructorPredicate() {
			@Override
			public boolean canReconstruct(Object obj, Member member, Object[] arguments) {
				return type.isInstance(obj);
			}

			@Override
			public String toString() {
				return ReconstructorPredicate.class.getSimpleName() + "[ALLOW_INSTANCE_OF: " + type + "]";
			}
		};
	}

	public static ReconstructorPredicate allowInstanceOf(String typeinternalname) {
		return new ReconstructorPredicate() {
			@Override
			public boolean canReconstruct(Object obj, Member member, Object[] arguments) {
				return obj != null && Utils.hasSuperTypeInternalName(obj.getClass(), typeinternalname);
			}

			@Override
			public String toString() {
				return ReconstructorPredicate.class.getSimpleName() + "[ALLOW_INSTANCE_OF: " + typeinternalname + "]";
			}
		};
	}
}
