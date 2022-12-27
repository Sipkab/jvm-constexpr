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
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.lang.model.SourceVersion;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

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

		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, "name");
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, "ordinal");
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, "compareTo", Object.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, "compareTo", Enum.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, "getDeclaringClass");
		addMethodConstantReconstructor(baseConstantReconstructors, Enum.class, "valueOf", Class.class, String.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "valueOf", byte.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, "valueOf", short.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "valueOf", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, "valueOf", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Float.class, "valueOf", float.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Double.class, "valueOf", double.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Boolean.class, "valueOf", boolean.class);

		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "getBytes", Charset.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "ofEpochSecond", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "ofEpochSecond", long.class,
				long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "ofEpochMilli", long.class);

		addMethodConstantReconstructor(baseConstantReconstructors, ValueRange.class, "of", long.class, long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, ValueRange.class, "of", long.class, long.class,
				long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, ValueRange.class, "of", long.class, long.class,
				long.class, long.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofDays", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofHours", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofMinutes", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofSeconds", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofSeconds", long.class, long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofMillis", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "ofNanos", long.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Period.class, "ofYears", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Period.class, "ofMonths", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Period.class, "ofDays", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Period.class, "of", int.class, int.class, int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Year.class, "of", int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "of", int.class, int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "of", int.class, Month.class,
				int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "of", int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "of", int.class, int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "of", int.class, int.class,
				int.class, int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "ofSecondOfDay", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "ofNanoOfDay", long.class);

		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", LocalDate.class,
				LocalTime.class);

		addMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "ofTotalSeconds", int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "of", LocalTime.class,
				ZoneOffset.class);

		addMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "of", LocalDateTime.class,
				ZoneOffset.class);

		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "length");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "isEmpty");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "charAt", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "codePointAt", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "codePointBefore", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "codePointCount", int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "offsetByCodePoints", int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "compareTo", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "compareToIgnoreCase", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "regionMatches", int.class,
				String.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "regionMatches", boolean.class,
				int.class, String.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "startsWith", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "startsWith", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "endsWith", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "hashCode");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "indexOf", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "indexOf", int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "lastIndexOf", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "lastIndexOf", int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "indexOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "indexOf", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "lastIndexOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "lastIndexOf", String.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "substring", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "substring", int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "subSequence", int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "concat", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "replace", char.class, char.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "join", CharSequence.class,
				CharSequence[].class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "join", CharSequence.class,
				Iterable.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "toLowerCase", Locale.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "toUpperCase", Locale.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "matches", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "contains", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "replaceFirst", String.class,
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "replaceAll", String.class,
				String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "replace", CharSequence.class,
				CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "trim");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "strip");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "stripLeading");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "stripTrailing");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "isBlank");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "indent", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "stripIndent");
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "translateEscapes");
		//non locale version is not constant, because depends on the executing environment
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "format", Locale.class, String.class,
				Object[].class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", Object.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", boolean.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", char.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", float.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "valueOf", double.class);
		addMethodConstantReconstructor(baseConstantReconstructors, String.class, "repeat", int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, "intValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, "longValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, "floatValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, "doubleValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, "byteValue");
		addMethodConstantReconstructor(baseConstantReconstructors, Number.class, "shortValue");

		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "valueOf", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "parseByte", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "parseByte", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "decode", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Byte.class, "hashCode");

		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, "valueOf", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, "parseShort", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, "parseShort", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, "decode", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Short.class, "hashCode");

		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "valueOf", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "parseInt", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "parseInt", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "decode", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Integer.class, "hashCode");

		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, "valueOf", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, "parseLong", String.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, "parseLong", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, "decode", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Long.class, "hashCode");

		addMethodConstantReconstructor(baseConstantReconstructors, Float.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Float.class, "parseFloat", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Float.class, "hashCode");

		addMethodConstantReconstructor(baseConstantReconstructors, Double.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Double.class, "parseDouble", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Double.class, "hashCode");

		addMethodConstantReconstructor(baseConstantReconstructors, Character.class, "valueOf", char.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Character.class, "hashCode");

		addMethodConstantReconstructor(baseConstantReconstructors, Boolean.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Boolean.class, "parseBoolean", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Boolean.class, "hashCode");

		addMethodConstantReconstructor(baseConstantReconstructors, UUID.class, "fromString", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, UUID.class, "nameUUIDFromBytes", byte[].class);

		addMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Instant.class, "toEpochMilli");

		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "of", long.class,
				TemporalUnit.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "from", TemporalAmount.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Duration.class, "between", Temporal.class,
				Temporal.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Period.class, "ofWeeks", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Period.class, "from", TemporalAmount.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Period.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Period.class, "between", LocalDate.class,
				LocalDate.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Year.class, "from", TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Year.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Year.class, "parse", CharSequence.class,
				DateTimeFormatter.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Year.class, "isLeap", long.class);

		addMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "of", int.class, Month.class);
		addMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "from", TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, YearMonth.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addMethodConstantReconstructor(baseConstantReconstructors, MonthDay.class, "of", Month.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, MonthDay.class, "from", TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, MonthDay.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, MonthDay.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addMethodConstantReconstructor(baseConstantReconstructors, Month.class, "of", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, Month.class, "from", TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, DayOfWeek.class, "of", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, DayOfWeek.class, "from", TemporalAccessor.class);

		addMethodConstantReconstructor(baseConstantReconstructors, HijrahEra.class, "of", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, IsoEra.class, "of", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, MinguoEra.class, "of", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, ThaiBuddhistEra.class, "of", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, JapaneseEra.class, "valueOf", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, JapaneseEra.class, "of", int.class);

		addMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "of", int.class, int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "ofYearDay", int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "ofInstant", Instant.class,
				ZoneId.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "ofEpochDay", long.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "from", TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDate.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "from", TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalTime.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class, Month.class,
				int.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class, Month.class,
				int.class, int.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class, Month.class,
				int.class, int.class, int.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class, int.class,
				int.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class, int.class,
				int.class, int.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "of", int.class, int.class,
				int.class, int.class, int.class, int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "ofInstant", Instant.class,
				ZoneId.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "ofEpochSecond", long.class,
				int.class, ZoneOffset.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "from", TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, LocalDateTime.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "of", String.class);
		addMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "ofHours", int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "ofHoursMinutes", int.class,
				int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "ofHoursMinutesSeconds", int.class,
				int.class, int.class);
		addMethodConstantReconstructor(baseConstantReconstructors, ZoneOffset.class, "from", TemporalAccessor.class);

		addMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "of", int.class, int.class,
				int.class, int.class, ZoneOffset.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "ofInstant", Instant.class,
				ZoneId.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "from", TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetTime.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "of", LocalDate.class,
				LocalTime.class, ZoneOffset.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "of", int.class, int.class,
				int.class, int.class, int.class, int.class, int.class, ZoneOffset.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "ofInstant", Instant.class,
				ZoneId.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "from",
				TemporalAccessor.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "parse", CharSequence.class);
		addMethodConstantReconstructor(baseConstantReconstructors, OffsetDateTime.class, "parse", CharSequence.class,
				DateTimeFormatter.class);

		addMethodConstantReconstructor(baseConstantReconstructors, SourceVersion.class, "isKeyword", CharSequence.class,
				SourceVersion.class);
		addMethodConstantReconstructor(baseConstantReconstructors, SourceVersion.class, "isName", CharSequence.class,
				SourceVersion.class);

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

		addConstantFields(baseConstantReconstructors, StandardCharsets.class, "US_ASCII", "ISO_8859_1", "UTF_8",
				"UTF_16BE", "UTF_16LE", "UTF_16");

		//XXX should we get all the declared fields instead, and add them based on that? seems more forward compatible
		addConstantFields(baseConstantReconstructors, Locale.class, "ENGLISH", "FRENCH", "GERMAN", "ITALIAN",
				"JAPANESE", "KOREAN", "CHINESE", "SIMPLIFIED_CHINESE", "TRADITIONAL_CHINESE", "FRANCE", "GERMANY",
				"ITALY", "JAPAN", "KOREA", "UK", "US", "CANADA", "CANADA_FRENCH", "ROOT", "CHINA", "PRC", "TAIWAN");

		addConstantFields(baseConstantReconstructors, Duration.class, "ZERO");

		addConstantFields(baseConstantReconstructors, Period.class, "ZERO");

		addConstantFields(baseConstantReconstructors, JapaneseEra.class, "MEIJI", "TAISHO", "SHOWA", "HEISEI", "REIWA");

		addConstantFields(baseConstantReconstructors, LocalDate.class, "MIN", "MAX");
		addConstantFields(baseConstantReconstructors, LocalTime.class, "MIN", "MAX", "MIDNIGHT", "NOON");
		addConstantFields(baseConstantReconstructors, LocalDateTime.class, "MIN", "MAX");
		addConstantFields(baseConstantReconstructors, ZoneOffset.class, "UTC", "MIN", "MAX");
		addConstantFields(baseConstantReconstructors, OffsetTime.class, "MIN", "MAX");
		addConstantFields(baseConstantReconstructors, OffsetDateTime.class, "MIN", "MAX");
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
				new Type[] { null, Type.getType(long.class), }, "getEpochSecond", "getNano");

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

		addConstructorBasedDeconstructor(baseConstantDeconstructors, StringBuilder.class, "toString");
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
		addConstructorBasedDeconstructor(constantDeconstructors, type, new Type[argumentsgettermethodnames.length],
				argumentsgettermethodnames);
	}

	private static void addConstructorBasedDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors,
			Class<?> type, Type[] asmargtypes, String... argumentsgettermethodnames) {
		try {
			ConstantDeconstructor deconstructor = ConstructorBasedDeconstructor.create(type, asmargtypes,
					argumentsgettermethodnames);
			Object prev = constantDeconstructors.putIfAbsent(type, deconstructor);
			if (prev != null) {
				throw new IllegalArgumentException("Duplicate constant deconstructor for: " + type);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static void addStaticMethodBasedDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors,
			Class<?> type, String methodname, String... argumentsgettermethodnames) {
		addStaticMethodBasedDeconstructor(constantDeconstructors, type, methodname,
				new Type[argumentsgettermethodnames.length], argumentsgettermethodnames);
	}

	private static void addStaticMethodBasedDeconstructor(Map<Class<?>, ConstantDeconstructor> constantDeconstructors,
			Class<?> type, String methodname, Type[] asmargtypes, String... argumentsgettermethodnames) {
		try {
			ConstantDeconstructor deconstructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(type,
					methodname, asmargtypes, argumentsgettermethodnames);
			Object prev = constantDeconstructors.putIfAbsent(type, deconstructor);
			if (prev != null) {
				throw new IllegalArgumentException("Duplicate constant deconstructor for: " + type);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static void initEnumConfig(Map<String, Class<?>> baseConstantTypes,
			Map<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors,
			Class<? extends Enum<?>> enumtype) {
		Utils.addToInternalNameMap(baseConstantTypes, enumtype);
		//inline the valueOf functions that look up an enum value based on a string 
		addMethodConstantReconstructor(baseConstantReconstructors, enumtype, "valueOf", String.class);
	}

	private static void addMethodConstantReconstructor(
			Map<? super MethodKey, ? super TypeReferencedConstantReconstructor> reconstructors, Class<?> type,
			String methodname, Class<?>... parameterTypes) {
		try {
			Method m = type.getMethod(methodname, parameterTypes);

			ConstantReconstructor reconstructor = new MethodBasedConstantReconstructor(m);
			Object prev = reconstructors.putIfAbsent(new MethodKey(m),
					new TypeReferencedConstantReconstructor(reconstructor, type));
			if (prev != null) {
				throw new IllegalArgumentException("Duplicate constant reconstructor for: " + m);
			}
		} catch (Exception e) {
			System.out.println("Method not found for constant reconstruction: " + type + "." + methodname
					+ " with args: " + Arrays.toString(parameterTypes));
			return;
		}
	}

	private static void addConstructorConstantReconstructor(
			Map<? super MethodKey, ? super TypeReferencedConstantReconstructor> reconstructors, Class<?> type,
			Class<?>... parameterTypes) {
		try {
			Constructor<?> constructor = type.getConstructor(parameterTypes);
			ConstantReconstructor reconstructor = new ConstructorBasedConstantReconstructor(constructor);
			Object prev = reconstructors.putIfAbsent(new MethodKey(constructor),
					new TypeReferencedConstantReconstructor(reconstructor, type));
			if (prev != null) {
				throw new IllegalArgumentException("Duplicate constant reconstructor for: " + constructor);
			}
		} catch (Exception e) {
			System.out.println("Constructor not found for constant inlining: " + type + " with args: "
					+ Arrays.toString(parameterTypes));
			return;
		}
	}

	private static void addConstantFields(
			Map<? super MemberKey, ? super TypeReferencedConstantReconstructor> constantFields, Class<?> type,
			String... fieldnames) {
		for (String fn : fieldnames) {
			addConstantField(constantFields, type, fn);
		}
	}

	private static void addConstantField(
			Map<? super MemberKey, ? super TypeReferencedConstantReconstructor> constantFields, Class<?> type,
			String fieldname) {
		try {
			Field f = type.getField(fieldname);
//			Object prev = constantFields.putIfAbsent(new FieldKey(f), new FieldValueRetriever() {
//				@Override
//				public Optional<?> getValue(ConstantExpressionInliner context, TransformedClass transclass,
//						Object subject) {
//					try {
//						return Optional.ofNullable(f.get(subject));
//					} catch (IllegalArgumentException | IllegalAccessException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						return null;
//					}
//				}
//			});
			Object prev = constantFields.putIfAbsent(new FieldKey(f),
					new TypeReferencedConstantReconstructor(new FieldBasedConstantReconstructor(f), type));
			if (prev != null) {
				throw new IllegalArgumentException("Duplicate constant fields for: " + f);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return;
		}
	}

	private static final class ClassCanonicalNameConstantReconstructor extends ClassNameConstantReconstructor {
		public static final ClassCanonicalNameConstantReconstructor INSTANCE = new ClassCanonicalNameConstantReconstructor();

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
		public static final ClassSimpleNameConstantReconstructor INSTANCE = new ClassSimpleNameConstantReconstructor();

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
		public static final ClassNameConstantReconstructor INSTANCE = new ClassNameConstantReconstructor();

		@Override
		public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins) {
			AsmStackReconstructedValue typeval = context.getInliner()
					.reconstructStackValue(context.withReceiverType(Class.class), ins.getPrevious());
			if (typeval == null) {
				return null;
			}
			Object val = typeval.getValue();
			String result;
			if (val instanceof Type) {
				result = getName((Type) val);
			} else if (val instanceof Class) {
				result = getName((Class<?>) val);
			} else {
				//TODO log?
				return null;
			}
			return new AsmStackReconstructedValue(typeval.getFirstIns(), ins.getNext(), result);
		}

		protected String getName(Type t) {
			return Utils.getNameOfClass(t);
		}

		protected String getName(Class<?> c) {
			return c.getName();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[]";
		}

	}
}
