package sipka.jvm.constexpr.tool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.HijrahEra;
import java.time.chrono.IsoEra;
import java.time.chrono.JapaneseEra;
import java.time.chrono.MinguoEra;
import java.time.chrono.ThaiBuddhistEra;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.lang.model.SourceVersion;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * Contains the default configurations for the inliner tool.
 */
//this class is separate from ConstantExpressionInliner because if this code were in clinit, then that could run
//slower, because the static initializer may be slower to run 
class BaseConfig {
	public static void configure(Map<String, Class<?>> baseConstantTypes,
			Map<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors,
			Map<Class<?>, ConstantDeconstructor> baseConstantDeconstructors) {
		initConstantTypes(baseConstantTypes, baseConstantReconstructors);
		initReconstructors(baseConstantReconstructors);
		initDeconstructors(baseConstantDeconstructors);
	}

	private static void initReconstructors(
			Map<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors) {
		//specifies ways of creating instances of types from the stack data

		baseConstantReconstructors.put(new MethodKey("java/lang/Class", "getName", "()Ljava/lang/String;"),
				new TypeReferencedConstantReconstructor(ClassNameConstantReconstructor.INSTANCE, Class.class));
		baseConstantReconstructors.put(new MethodKey("java/lang/Class", "getSimpleName", "()Ljava/lang/String;"),
				new TypeReferencedConstantReconstructor(ClassSimpleNameConstantReconstructor.INSTANCE, Class.class));
		baseConstantReconstructors.put(new MethodKey("java/lang/Class", "getCanonicalName", "()Ljava/lang/String;"),
				new TypeReferencedConstantReconstructor(ClassCanonicalNameConstantReconstructor.INSTANCE, Class.class));

		addMethodConstantReconstructor(baseConstantReconstructors, Comparable.class, int.class, "compareTo",
				Object.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, String.class, "name");
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, int.class, "ordinal");
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, int.class, "compareTo", Object.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, int.class, "compareTo", Enum.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, Class.class, "getDeclaringClass");
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, Enum.class, "valueOf", Class.class,
				String.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "valueOf", byte.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Short.class, "valueOf", short.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "valueOf", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Long.class, "valueOf", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Float.class, "valueOf", float.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Double.class, "valueOf", double.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Boolean.class, "valueOf", boolean.class);

		addMethodConstantReconstructor(baseConstantReconstructors, String.class, byte[].class, "getBytes",
				Charset.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "ofEpochSecond", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "ofEpochSecond", long.class,
				long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "ofEpochMilli", long.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ValueRange.class, "of", long.class,
				long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ValueRange.class, "of", long.class,
				long.class, long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ValueRange.class, "of", long.class,
				long.class, long.class, long.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofDays", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofHours", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofMinutes", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofSeconds", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofSeconds", long.class,
				long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofMillis", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofNanos", long.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Period.class, "ofYears", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Period.class, "ofMonths", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Period.class, "ofDays", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Period.class, "of", int.class, int.class,
				int.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Year.class, "of", int.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "of", int.class, int.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "of", int.class, Month.class,
				int.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "of", int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "of", int.class, int.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "of", int.class, int.class,
				int.class, int.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "ofSecondOfDay", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "ofNanoOfDay", long.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", LocalDate.class,
				LocalTime.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "ofTotalSeconds",
				int.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "of", LocalTime.class,
				ZoneOffset.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "of",
				LocalDateTime.class, ZoneOffset.class);

		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "length");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "isEmpty");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, char.class, "charAt", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "codePointAt", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "codePointBefore",
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "codePointCount", int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "offsetByCodePoints",
				int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "compareTo", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "compareToIgnoreCase",
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "regionMatches",
				int.class, String.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "regionMatches",
				boolean.class, int.class, String.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "startsWith",
				String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "startsWith",
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "endsWith",
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "hashCode");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "indexOf", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "indexOf", int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "lastIndexOf", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "lastIndexOf", int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "indexOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "indexOf", String.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "lastIndexOf",
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, int.class, "lastIndexOf", String.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "substring", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "substring", int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, CharSequence.class, "subSequence",
				int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "concat", String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "replace", char.class,
				char.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "join", CharSequence.class,
				CharSequence[].class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "join", CharSequence.class,
				Iterable.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "toLowerCase", Locale.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "toUpperCase", Locale.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "matches",
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "contains",
				CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "replaceFirst", String.class,
				String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "replaceAll", String.class,
				String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "replace", CharSequence.class,
				CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "trim");
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "strip");
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "stripLeading");
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "stripTrailing");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, boolean.class, "isBlank");
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "indent", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "stripIndent");
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "translateEscapes");
		//non locale version is not constant, because depends on the executing environment
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "format", Locale.class,
				String.class, Object[].class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", Object.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", boolean.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", char.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", float.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", double.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, String.class, "repeat", int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, int.class, "intValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, long.class, "longValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, float.class, "floatValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, double.class, "doubleValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, byte.class, "byteValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, short.class, "shortValue");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "valueOf", String.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, byte.class, "parseByte", String.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, byte.class, "parseByte", String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "decode", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, int.class, "hashCode");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Short.class, "valueOf", String.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Short.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, short.class, "parseShort", String.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, short.class, "parseShort",
				String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Short.class, "decode", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, int.class, "hashCode");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "valueOf", String.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, int.class, "parseInt", String.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, int.class, "parseInt", String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "decode", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, int.class, "hashCode");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Long.class, "valueOf", String.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Long.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, long.class, "parseLong", String.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, long.class, "parseLong", String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Long.class, "decode", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, int.class, "hashCode");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Float.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Float.class, float.class, "parseFloat",
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Float.class, int.class, "hashCode");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Double.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Double.class, double.class, "parseDouble",
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Double.class, int.class, "hashCode");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Character.class, "valueOf", char.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Character.class, int.class, "hashCode");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Boolean.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Boolean.class, boolean.class, "parseBoolean",
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Boolean.class, int.class, "hashCode");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, UUID.class, "fromString", String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, UUID.class, "nameUUIDFromBytes",
				byte[].class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Instant.class, long.class, "toEpochMilli");

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "of", long.class,
				TemporalUnit.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "from", TemporalAmount.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "parse", CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "between", Temporal.class,
				Temporal.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Period.class, "ofWeeks", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Period.class, "from", TemporalAmount.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Period.class, "parse", CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Period.class, "between", LocalDate.class,
				LocalDate.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Year.class, "from", TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Year.class, "parse", CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Year.class, "parse", CharSequence.class,
				DateTimeFormatter.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Year.class, boolean.class, "isLeap", long.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "of", int.class,
				Month.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "from",
				TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "parse", CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, MonthDay.class, "of", Month.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, MonthDay.class, "from",
				TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, MonthDay.class, "parse", CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, MonthDay.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Month.class, "of", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, Month.class, "from", TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, DayOfWeek.class, "of", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, DayOfWeek.class, "from",
				TemporalAccessor.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, HijrahEra.class, "of", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, IsoEra.class, "of", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, MinguoEra.class, "of", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ThaiBuddhistEra.class, "of", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, JapaneseEra.class, "valueOf", String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, JapaneseEra.class, "of", int.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "of", int.class, int.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "ofYearDay", int.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "ofInstant", Instant.class,
				ZoneId.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "ofEpochDay", long.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "from",
				TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "parse", CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "from",
				TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "parse", CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class,
				Month.class, int.class, int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class,
				Month.class, int.class, int.class, int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class,
				Month.class, int.class, int.class, int.class, int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class,
				int.class, int.class, int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class,
				int.class, int.class, int.class, int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class,
				int.class, int.class, int.class, int.class, int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "ofInstant",
				Instant.class, ZoneId.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "ofEpochSecond",
				long.class, int.class, ZoneOffset.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "from",
				TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "parse",
				CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "parse",
				CharSequence.class, DateTimeFormatter.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "of", String.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "ofHours", int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "ofHoursMinutes", int.class,
				int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "ofHoursMinutesSeconds",
				int.class, int.class, int.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "from",
				TemporalAccessor.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "of", int.class, int.class,
				int.class, int.class, ZoneOffset.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "ofInstant", Instant.class,
				ZoneId.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "from",
				TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "parse",
				CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "of", LocalDate.class,
				LocalTime.class, ZoneOffset.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "of", int.class,
				int.class, int.class, int.class, int.class, int.class, int.class, ZoneOffset.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "ofInstant",
				Instant.class, ZoneId.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "from",
				TemporalAccessor.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "parse",
				CharSequence.class);
		addFactoryMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "parse",
				CharSequence.class, DateTimeFormatter.class);

		addMethodConstantReconstructor(baseConstantReconstructors, SourceVersion.class, boolean.class, "isKeyword",
				CharSequence.class, SourceVersion.class);
		addMethodConstantReconstructor(baseConstantReconstructors, SourceVersion.class, boolean.class, "isName",
				CharSequence.class, SourceVersion.class);

		addConstructorConstantReconstructor(baseConstantReconstructors, StringBuilder.class);
		addConstructorConstantReconstructor(baseConstantReconstructors, StringBuilder.class, int.class);
		addConstructorConstantReconstructor(baseConstantReconstructors, StringBuilder.class, String.class);
		addConstructorConstantReconstructor(baseConstantReconstructors, StringBuilder.class, CharSequence.class);
		for (Method sbm : StringBuilder.class.getMethods()) {
			if (!"append".equals(sbm.getName())) {
				continue;
			}
			if (sbm.getReturnType() != StringBuilder.class) {
				continue;
			}
			baseConstantReconstructors.putIfAbsent(new MethodKey(sbm), new TypeReferencedConstantReconstructor(
					new MethodBasedConstantReconstructor(sbm), StringBuilder.class));
		}

		addConstantFieldReconstructors(baseConstantReconstructors, StandardCharsets.class, Charset.class, "US_ASCII",
				"ISO_8859_1", "UTF_8", "UTF_16BE", "UTF_16LE", "UTF_16");

		//XXX should we get all the declared fields instead, and add them based on that? seems more forward compatible
		addConstantFieldReconstructors(baseConstantReconstructors, Locale.class, Locale.class, "ENGLISH", "FRENCH",
				"GERMAN", "ITALIAN", "JAPANESE", "KOREAN", "CHINESE", "SIMPLIFIED_CHINESE", "TRADITIONAL_CHINESE",
				"FRANCE", "GERMANY", "ITALY", "JAPAN", "KOREA", "UK", "US", "CANADA", "CANADA_FRENCH", "ROOT", "CHINA",
				"PRC", "TAIWAN");

		addConstantFieldReconstructors(baseConstantReconstructors, Duration.class, Duration.class, "ZERO");

		addConstantFieldReconstructors(baseConstantReconstructors, Period.class, Period.class, "ZERO");

		addConstantFieldReconstructors(baseConstantReconstructors, JapaneseEra.class, JapaneseEra.class, "MEIJI",
				"TAISHO", "SHOWA", "HEISEI", "REIWA");

		addConstantFieldReconstructors(baseConstantReconstructors, LocalDate.class, LocalDate.class, "MIN", "MAX");
		addConstantFieldReconstructors(baseConstantReconstructors, LocalTime.class, LocalTime.class, "MIN", "MAX",
				"MIDNIGHT", "NOON");
		addConstantFieldReconstructors(baseConstantReconstructors, LocalDateTime.class, LocalDateTime.class, "MIN",
				"MAX");
		addConstantFieldReconstructors(baseConstantReconstructors, ZoneOffset.class, ZoneOffset.class, "UTC", "MIN",
				"MAX");
		addConstantFieldReconstructors(baseConstantReconstructors, OffsetTime.class, OffsetTime.class, "MIN", "MAX");
		addConstantFieldReconstructors(baseConstantReconstructors, OffsetDateTime.class, OffsetDateTime.class, "MIN",
				"MAX");
	}

	private static void initDeconstructors(Map<Class<?>, ConstantDeconstructor> baseConstantDeconstructors) {
		//specifies ways of writing the type instances to the stack
		baseConstantDeconstructors.put(String.class, StringConstantDeconstructor.INSTANCE);

		baseConstantDeconstructors.put(byte[].class, ArrayConstantDeconstructor.INSTANCE);
		baseConstantDeconstructors.put(short[].class, ArrayConstantDeconstructor.INSTANCE);
		baseConstantDeconstructors.put(int[].class, ArrayConstantDeconstructor.INSTANCE);
		baseConstantDeconstructors.put(long[].class, ArrayConstantDeconstructor.INSTANCE);
		baseConstantDeconstructors.put(float[].class, ArrayConstantDeconstructor.INSTANCE);
		baseConstantDeconstructors.put(double[].class, ArrayConstantDeconstructor.INSTANCE);
		baseConstantDeconstructors.put(boolean[].class, ArrayConstantDeconstructor.INSTANCE);
		baseConstantDeconstructors.put(char[].class, ArrayConstantDeconstructor.INSTANCE);

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Byte.class, "valueOf", "byteValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Short.class, "valueOf", "shortValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Integer.class, "valueOf", "intValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Long.class, "valueOf", "longValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Float.class, "valueOf", "floatValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Double.class, "valueOf", "doubleValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Boolean.class, "valueOf", "booleanValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Character.class, "valueOf", "charValue");

		addConstructorBasedDeconstructor(baseConstantDeconstructors, UUID.class, "getMostSignificantBits",
				"getLeastSignificantBits");

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Instant.class, "ofEpochSecond",
				new Class<?>[] { null, long.class, }, "getEpochSecond", "getNano");

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, ValueRange.class, "of", "getMinimum",
				"getLargestMinimum", "getSmallestMaximum", "getMaximum");

		baseConstantDeconstructors.putIfAbsent(Duration.class, DurationConstantDeconstructor.INSTANCE);
		baseConstantDeconstructors.putIfAbsent(Period.class, PeriodConstantDeconstructor.INSTANCE);

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, Year.class, "of", "getValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, YearMonth.class, "of", "getYear",
				"getMonthValue");
		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, MonthDay.class, "of", "getMonth",
				"getDayOfMonth");

		baseConstantDeconstructors.putIfAbsent(JapaneseEra.class, new StaticFieldEqualityDelegateConstantDeconstructor(
				null, JapaneseEra.class, "MEIJI", "TAISHO", "SHOWA", "HEISEI", "REIWA"));

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, LocalDate.class, "of", "getYear", "getMonth",
				"getDayOfMonth");
		baseConstantDeconstructors.computeIfPresent(LocalDate.class, (k,
				decon) -> new StaticFieldEqualityDelegateConstantDeconstructor(decon, LocalDate.class, "MIN", "MAX"));

		baseConstantDeconstructors.putIfAbsent(LocalTime.class, LocalTimeConstantDeconstructor.INSTANCE);

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, LocalDateTime.class, "of", "toLocalDate",
				"toLocalTime");
		baseConstantDeconstructors.computeIfPresent(LocalDateTime.class,
				(k, decon) -> new StaticFieldEqualityDelegateConstantDeconstructor(decon, LocalDateTime.class, "MIN",
						"MAX"));

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, ZoneOffset.class, "ofTotalSeconds",
				"getTotalSeconds");
		baseConstantDeconstructors.computeIfPresent(ZoneOffset.class,
				(k, decon) -> new StaticFieldEqualityDelegateConstantDeconstructor(decon, ZoneOffset.class, "UTC",
						"MIN", "MAX"));

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, OffsetTime.class, "of", "toLocalTime",
				"getOffset");
		baseConstantDeconstructors.computeIfPresent(OffsetTime.class, (k,
				decon) -> new StaticFieldEqualityDelegateConstantDeconstructor(decon, OffsetTime.class, "MIN", "MAX"));

		addStaticMethodBasedDeconstructor(baseConstantDeconstructors, OffsetDateTime.class, "of", "toLocalDateTime",
				"getOffset");
		baseConstantDeconstructors.computeIfPresent(OffsetDateTime.class,
				(k, decon) -> new StaticFieldEqualityDelegateConstantDeconstructor(decon, OffsetDateTime.class, "MIN",
						"MAX"));

		setDeconstructor(baseConstantDeconstructors, StringBuilder.class, StringBuilderConstantDeconstructor.INSTANCE);
	}

	private static void initConstantTypes(Map<String, Class<?>> baseConstantTypes,
			Map<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors) {
		Utils.addToInternalNameMap(baseConstantTypes, Byte.class);
		Utils.addToInternalNameMap(baseConstantTypes, Short.class);
		Utils.addToInternalNameMap(baseConstantTypes, Integer.class);
		Utils.addToInternalNameMap(baseConstantTypes, Long.class);
		Utils.addToInternalNameMap(baseConstantTypes, Float.class);
		Utils.addToInternalNameMap(baseConstantTypes, Double.class);
		Utils.addToInternalNameMap(baseConstantTypes, Character.class);
		Utils.addToInternalNameMap(baseConstantTypes, Boolean.class);
		Utils.addToInternalNameMap(baseConstantTypes, UUID.class);
		Utils.addToInternalNameMap(baseConstantTypes, ValueRange.class);
		Utils.addToInternalNameMap(baseConstantTypes, Duration.class);
		Utils.addToInternalNameMap(baseConstantTypes, Period.class);
		Utils.addToInternalNameMap(baseConstantTypes, Year.class);
		Utils.addToInternalNameMap(baseConstantTypes, YearMonth.class);
		Utils.addToInternalNameMap(baseConstantTypes, MonthDay.class);
		Utils.addToInternalNameMap(baseConstantTypes, LocalDate.class);
		Utils.addToInternalNameMap(baseConstantTypes, LocalTime.class);
		Utils.addToInternalNameMap(baseConstantTypes, LocalDateTime.class);
		Utils.addToInternalNameMap(baseConstantTypes, ZoneOffset.class);
		Utils.addToInternalNameMap(baseConstantTypes, OffsetTime.class);
		Utils.addToInternalNameMap(baseConstantTypes, OffsetDateTime.class);
		Utils.addToInternalNameMap(baseConstantTypes, JapaneseEra.class);

		initEnumConfig(baseConstantTypes, baseConstantReconstructors, TimeUnit.class);
		initEnumConfig(baseConstantTypes, baseConstantReconstructors, ChronoField.class);
		//blacklist this method, as it depends on locale, and resources, so on the executing environment
		baseConstantReconstructors.putIfAbsent(
				new MethodKey(Type.getInternalName(ChronoField.class), "getDisplayName",
						Type.getMethodDescriptor(Type.getType(String.class), Type.getType(Locale.class))),
				new TypeReferencedConstantReconstructor(NotReconstructableConstantReconstructor.INSTANCE,
						ChronoField.class));

		initEnumConfig(baseConstantTypes, baseConstantReconstructors, ChronoUnit.class);
		initEnumConfig(baseConstantTypes, baseConstantReconstructors, TextStyle.class);
		initEnumConfig(baseConstantTypes, baseConstantReconstructors, Month.class);
		initEnumConfig(baseConstantTypes, baseConstantReconstructors, DayOfWeek.class);

		initEnumConfig(baseConstantTypes, baseConstantReconstructors, HijrahEra.class);
		initEnumConfig(baseConstantTypes, baseConstantReconstructors, IsoEra.class);
		initEnumConfig(baseConstantTypes, baseConstantReconstructors, MinguoEra.class);
		initEnumConfig(baseConstantTypes, baseConstantReconstructors, ThaiBuddhistEra.class);

		initEnumConfig(baseConstantTypes, baseConstantReconstructors, SourceVersion.class);
	}

	private static void addConstructorBasedDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors,
			Class<?> type, String... argumentsgettermethodnames) {
		addConstructorBasedDeconstructor(constantDeconstructors, type, new Class<?>[argumentsgettermethodnames.length],
				argumentsgettermethodnames);
	}

	private static void addConstructorBasedDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors,
			Class<?> type, Class<?>[] asmargtypes, String... argumentsgettermethodnames) {
		DeconstructionDataAccessor[] accessors;
		try {
			accessors = toDeconstructorDataAccessors(type, asmargtypes, argumentsgettermethodnames);
		} catch (NoSuchMethodException e) {
			setDeconstructor(constantDeconstructors, type,
					new BaseConfigMemberNotAvailableConstantDeconstructor(Utils.CONSTRUCTOR_METHOD_NAME,
							Type.getMethodDescriptor(Type.VOID_TYPE, Utils.toAsmTypes(asmargtypes)),
							Type.getType(type).getInternalName(), e));
			return;
		}
		ConstantDeconstructor deconstructor = ConstructorBasedDeconstructor.create(type, accessors);
		setDeconstructor(constantDeconstructors, type, deconstructor);
	}

	private static void addStaticMethodBasedDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors,
			Class<?> type, String methodname, String... argumentsgettermethodnames) {
		addStaticMethodBasedDeconstructor(constantDeconstructors, type, methodname,
				new Class<?>[argumentsgettermethodnames.length], argumentsgettermethodnames);
	}

	private static void addStaticMethodBasedDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors,
			Class<?> type, String methodname, Class<?>[] asmargtypes, String... argumentsgettermethodnames) {
		DeconstructionDataAccessor[] accessors;
		try {
			accessors = toDeconstructorDataAccessors(type, asmargtypes, argumentsgettermethodnames);
		} catch (NoSuchMethodException e) {
			setDeconstructor(constantDeconstructors, type,
					new BaseConfigMemberNotAvailableConstantDeconstructor(methodname,
							Type.getMethodDescriptor(Type.getType(type), Utils.toAsmTypes(asmargtypes)),
							Type.getType(type).getInternalName(), e));
			return;
		}
		addStaticMethodBasedDeconstructor(constantDeconstructors, type, methodname, accessors);
	}

	private static DeconstructionDataAccessor[] toDeconstructorDataAccessors(Class<?> type, Class<?>[] asmargtypes,
			String... argumentsgettermethodnames) throws NoSuchMethodException {
		DeconstructionDataAccessor[] accessors = new DeconstructionDataAccessor[argumentsgettermethodnames.length];
		for (int i = 0; i < accessors.length; i++) {
			Class<?> receivertype = asmargtypes[i];
			if (receivertype == null) {
				accessors[i] = DeconstructionDataAccessor.createForMethod(type, argumentsgettermethodnames[i]);
			} else {
				accessors[i] = DeconstructionDataAccessor.createForMethodWithReceiver(type,
						argumentsgettermethodnames[i], receivertype);
			}
		}
		return accessors;
	}

	private static void addStaticMethodBasedDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors,
			Class<?> type, String methodname, DeconstructionDataAccessor... argumentdataaccessors) {
		ConstantDeconstructor deconstructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(type,
				methodname, argumentdataaccessors);
		setDeconstructor(constantDeconstructors, type, deconstructor);
	}

	private static void setDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors, Class<?> type,
			ConstantDeconstructor deconstructor) {
		Object prev = constantDeconstructors.putIfAbsent(type, deconstructor);
		if (prev != null) {
			throw new IllegalArgumentException("Duplicate constant deconstructor for: " + type);
		}
	}

	private static void initEnumConfig(Map<String, Class<?>> baseConstantTypes,
			Map<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors,
			Class<? extends Enum<?>> enumtype) {
		Utils.addToInternalNameMap(baseConstantTypes, enumtype);
		//inline the valueOf functions that look up an enum value based on a string 
		addMethodConstantReconstructor(baseConstantReconstructors, enumtype, enumtype, "valueOf", String.class);
	}

	private static void addFactoryMethodConstantReconstructor(
			Map<? super MethodKey, ? super TypeReferencedConstantReconstructor> reconstructors, Class<?> type,
			String methodname, Class<?>... parameterTypes) {
		addMethodConstantReconstructor(reconstructors, type, type, methodname, parameterTypes);
	}

	private static void addMethodConstantReconstructor(
			Map<? super MethodKey, ? super TypeReferencedConstantReconstructor> reconstructors, Class<?> type,
			Class<?> returntype, String methodname, Class<?>... parameterTypes) {
		MethodKey methodkey = new MethodKey(Type.getType(type).getInternalName(), methodname,
				Type.getMethodDescriptor(Type.getType(returntype), Utils.toAsmTypes(parameterTypes)));

		ConstantReconstructor reconstructor;
		try {
			Method m = type.getMethod(methodname, parameterTypes);
			Class<?> actualreturntype = m.getReturnType();
			if (actualreturntype != returntype) {
				throw new IllegalArgumentException("Method return type mismatch for: " + type.getName() + "."
						+ methodname + " expected: " + returntype + " actual: " + actualreturntype);
			}

			reconstructor = new MethodBasedConstantReconstructor(m);
		} catch (Exception e) {
			reconstructor = new MemberNotAvailableConstantReconstructor(methodkey.getOwner(), methodkey.getMemberName(),
					methodkey.getMethodDescriptor(), e);
		}
		Object prev = reconstructors.putIfAbsent(methodkey,
				new TypeReferencedConstantReconstructor(reconstructor, type));
		if (prev != null) {
			throw new IllegalArgumentException("Duplicate constant reconstructor for: " + methodkey);
		}
	}

	private static void addConstructorConstantReconstructor(
			Map<? super MethodKey, ? super TypeReferencedConstantReconstructor> reconstructors, Class<?> type,
			Class<?>... parameterTypes) {
		MethodKey methodkey = new MethodKey(Type.getType(type).getInternalName(), Utils.CONSTRUCTOR_METHOD_NAME,
				Type.getMethodDescriptor(Type.VOID_TYPE, Utils.toAsmTypes(parameterTypes)));
		ConstantReconstructor reconstructor;
		try {
			Constructor<?> constructor = type.getConstructor(parameterTypes);
			reconstructor = new ConstructorBasedConstantReconstructor(constructor);
		} catch (Exception e) {
			reconstructor = new MemberNotAvailableConstantReconstructor(methodkey.getOwner(), methodkey.getMemberName(),
					methodkey.getMethodDescriptor(), e);
		}
		Object prev = reconstructors.putIfAbsent(methodkey,
				new TypeReferencedConstantReconstructor(reconstructor, type));
		if (prev != null) {
			throw new IllegalArgumentException("Duplicate constant reconstructor for: " + methodkey);
		}
	}

	private static void addConstantFieldReconstructors(
			Map<? super MemberKey, ? super TypeReferencedConstantReconstructor> constantFields, Class<?> type,
			Class<?> fieldtype, String... fieldnames) {
		for (String fn : fieldnames) {
			addConstantFieldReconstructor(constantFields, type, fieldtype, fn);
		}
	}

	private static void addConstantFieldReconstructor(
			Map<? super MemberKey, ? super TypeReferencedConstantReconstructor> constantFields, Class<?> type,
			Class<?> fieldtype, String fieldname) {
		FieldKey fieldkey = new FieldKey(type, fieldname, fieldtype);
		ConstantReconstructor reconstructor;
		try {
			Field f = type.getField(fieldname);
			Class<?> actualfieldtype = f.getType();
			if (actualfieldtype != fieldtype) {
				throw new IllegalArgumentException("Field type mismatch for: " + type.getName() + "." + fieldname
						+ " expected: " + fieldtype + " actual: " + actualfieldtype);
			}
			reconstructor = new FieldBasedConstantReconstructor(f);
		} catch (Exception e) {
			reconstructor = new MemberNotAvailableConstantReconstructor(fieldkey.getOwner(), fieldkey.getMemberName(),
					fieldkey.getFieldDescriptor(), e);
		}
		Object prev = constantFields.putIfAbsent(fieldkey,
				new TypeReferencedConstantReconstructor(reconstructor, type));
		if (prev != null) {
			throw new IllegalArgumentException("Duplicate constant fields for: " + fieldkey);
		}
	}

	private static final class BaseConfigMemberNotAvailableConstantDeconstructor implements ConstantDeconstructor {
		private final String methodName;
		private final String methodDescr;
		private final String classInternalName;
		private final NoSuchMethodException e;

		private BaseConfigMemberNotAvailableConstantDeconstructor(String methodname, String methoddescr,
				String internalname, NoSuchMethodException e) {
			this.methodName = methodname;
			this.methodDescr = methoddescr;
			this.classInternalName = internalname;
			this.e = e;
		}

		@Override
		public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
				MethodNode methodnode, Object value) {
			context.logConfigClassMemberNotAvailable(classInternalName, methodName, methodDescr, e);
			return null;
		}
	}

	private static final class MemberNotAvailableConstantReconstructor implements ConstantReconstructor {

		private final String className;
		private final String memberName;
		private final String memberDescriptor;
		private final Throwable exception;

		public MemberNotAvailableConstantReconstructor(String className, String memberName, String memberDescriptor,
				Throwable exception) {
			this.className = className;
			this.memberName = memberName;
			this.memberDescriptor = memberDescriptor;
			this.exception = exception;
		}

		@Override
		public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
				throws ReconstructionException {
			context.getInliner().logConfigClassMemberNotAvailable(className, memberName, memberDescriptor, exception);
			return null;
		}

	}

	private static final class ClassCanonicalNameConstantReconstructor extends ClassNameConstantReconstructor {
		public static final ClassCanonicalNameConstantReconstructor INSTANCE = new ClassCanonicalNameConstantReconstructor(
				"getCanonicalName");

		public ClassCanonicalNameConstantReconstructor(String memberName) {
			super(memberName);
		}

		@Override
		protected String getName(Type t) {
			return Utils.getCanonicalNameOfClass(t);
		}

		@Override
		protected String getName(Class<?> c) {
			return c.getCanonicalName();
		}
	}

	private static final class ClassSimpleNameConstantReconstructor extends ClassNameConstantReconstructor {
		public static final ClassSimpleNameConstantReconstructor INSTANCE = new ClassSimpleNameConstantReconstructor(
				"getSimpleName");

		public ClassSimpleNameConstantReconstructor(String memberName) {
			super(memberName);
		}

		@Override
		protected String getName(Type t) {
			return Utils.getSimpleNameOfClass(t);
		}

		@Override
		protected String getName(Class<?> c) {
			return c.getSimpleName();
		}
	}

	private static class ClassNameConstantReconstructor implements ConstantReconstructor {
		public static final ClassNameConstantReconstructor INSTANCE = new ClassNameConstantReconstructor("getName");

		private final String memberName;

		public ClassNameConstantReconstructor(String memberName) {
			this.memberName = memberName;
		}

		@Override
		public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
				throws ReconstructionException {
			AsmStackReconstructedValue typeval;
			try {
				typeval = context.getInliner().reconstructStackValue(context.withReceiverType(Class.class),
						ins.getPrevious());
			} catch (ReconstructionException e) {
				throw context.newInstanceAccessFailureReconstructionException(e, ins, "java/lang/Class", memberName,
						"()Ljava/lang/String;");
			}
			if (typeval == null) {
				return null;
			}
			Object val = typeval.getValue();
			if (val == null) {
				return null;
			}
			String result;
			if (val instanceof Type) {
				result = getName((Type) val);
			} else if (val instanceof Class) {
				result = getName((Class<?>) val);
			} else {
				//some unrecognized type for Class method call
				//can't do much, maybe programming error, or something?
				return null;
			}
			return new AsmStackReconstructedValue(typeval.getFirstIns(), ins.getNext(),
					AsmStackInfo.createMethod(Type.getType(Class.class), memberName,
							Type.getMethodType("()Ljava/lang/String;"), typeval.getStackInfo(),
							AsmStackInfo.EMPTY_ASMSTACKINFO_ARRAY),
					result);
		}

		@SuppressWarnings("static-method")
		protected String getName(Type t) {
			return Utils.getNameOfClass(t);
		}

		@SuppressWarnings("static-method")
		protected String getName(Class<?> c) {
			return c.getName();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[]";
		}

	}
}
