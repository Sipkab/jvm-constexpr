package testing.sipka.jvm.constexpr;

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
import java.time.ZoneOffset;
import java.time.chrono.JapaneseEra;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Test constant inlining of Java chrono API class in some ways.
 * <p>
 * (In the ways that is somewhat relevant to my use cases)
 */
@SakerTest
public class ChronoInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, "CHRONOFIELDDISPLAYNAME");
		//should be null, because getDisplayName cannot be inlined, as it is 
		assertNull(TestUtils.getFields(classnode).get("CHRONOFIELDDISPLAYNAME").value);
	}

	public static class Constants {
		public static final String str;
		public static final String strParse;
		public static final long parsedEpochMillis;
		static {
			str = Instant.ofEpochMilli(100).toString();
			strParse = Instant.parse("2000-01-02T03:04:05.006Z").toString();
			parsedEpochMillis = Instant.parse("2000-01-02T03:04:05.006Z").toEpochMilli();
		}

		public static final long DAYMILLIS = TimeUnit.DAYS.toMillis(1);
		public static final long CONVERT = TimeUnit.HOURS.convert(2, TimeUnit.DAYS);
		public static final String DAYNAME = TimeUnit.DAYS.name();

		public static final boolean CHRONOFIELDTEST = ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.isDateBased();
		public static final String CHRONOFIELDDISPLAYNAME = ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR
				.getDisplayName(Locale.ROOT);

		public static final boolean CHRONOUNITTEST = ChronoUnit.DECADES.isDateBased();

		public static final long VALUERANGE_TEST1 = ValueRange.of(12, 34).getLargestMinimum();

		public static final String DUR1 = Duration.of(10, ChronoUnit.SECONDS).toString();
		public static final String DUR2 = Duration.of(11, ChronoUnit.NANOS).toString();
		public static final String DUR3 = Duration.parse("PT0.000123011S").toString();
		public static final String DUR4 = Duration.parse("PT9.700123011S").toString();
		public static final String DUR5 = Duration.parse("PT0.000123011S").withSeconds(123).toString();
		public static final String DUR6 = Duration.parse("PT0S").toString();
		public static final String DUR7 = Duration.ZERO.toString();
		public static final String DUR8 = Duration.ofSeconds(0, 0).toString();
		public static final String DUR9 = Duration.parse("PT1S").minusSeconds(1).toString();
		public static final long DUR10 = Duration.parse("PT12.34S").get(ChronoUnit.SECONDS);
		public static final long DUR11 = Duration.parse("PT12.34S").get(ChronoUnit.NANOS);
		public static final String DUR12 = Duration.ofMillis(-1).toString();
		public static final String DUR13 = Duration.ofSeconds(10).toString();
		public static final String DUR14 = Duration.ofMinutes(11).toString();
		public static final String DUR15 = Duration.ofSeconds(10, 123000000L).toString();
		public static final String DUR16 = Duration.ofSeconds(10, 123456000L).toString();
		public static final String DUR17 = Duration.ofSeconds(10, 123456789L).toString();
		public static final String DUR18 = Duration.ofSeconds(0, 123000000L).toString();
		public static final String DUR19 = Duration.ofSeconds(0, 123456000L).toString();
		public static final String DUR20 = Duration.ofSeconds(0, 123456789L).toString();

		public static final String PER1 = Period.parse("P2Y").toString();
		public static final String PER2 = Period.parse("P2M").toString();
		public static final String PER3 = Period.parse("P2D").toString();
		public static final String PER4 = Period.parse("P1Y2D").toString();
		public static final String PER5 = Period.ofYears(3).toString();
		public static final String PER6 = Period.ofMonths(4).toString();
		public static final String PER7 = Period.ofWeeks(5).toString();
		public static final String PER8 = Period.ofDays(6).toString();

		public static final String YEAR1 = Year.of(1234).toString();
		public static final String YEAR2 = Year.parse("2456").toString();
		public static final boolean YEAR3 = Year.isLeap(2000);

		public static final String MONTH1 = Month.APRIL.getDisplayName(TextStyle.FULL, Locale.ROOT);
		public static final String MONTH2 = Month.of(3).getDisplayName(TextStyle.FULL, Locale.ROOT);

		public static final String DAY1 = DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL, Locale.ROOT);
		public static final String DAY2 = DayOfWeek.of(3).getDisplayName(TextStyle.FULL, Locale.ROOT);

		public static final String YM1 = YearMonth.of(2000, 12).toString();
		public static final String YM2 = YearMonth.parse("1234-11").toString();

		public static final String MD1 = MonthDay.of(Month.FEBRUARY, 10).toString();
		public static final String MD2 = MonthDay.of(Month.FEBRUARY, 29).toString();
		public static final String MD3 = MonthDay.parse("--03-01").toString();

		public static final String JERA1 = JapaneseEra.HEISEI.toString();
		public static final String JERA2 = JapaneseEra.valueOf("Heisei").toString();
		public static final int JERA3 = JapaneseEra.valueOf("Heisei").getValue();
		public static final JapaneseEra JERA4 = JapaneseEra.valueOf("Heisei");

		public static final String LOCALDATE1 = LocalDate.of(2000, 1, 2).toString();
		public static final String LOCALDATE2 = LocalDate.of(2000, Month.JANUARY, 2).toString();
		public static final String LOCALDATE3 = LocalDate.parse("2000-02-03").toString();
		public static final String LOCALDATE4 = LocalDate.MIN.toString();
		public static final String LOCALDATE5 = LocalDate.parse("-999999999-01-01").toString();

		public static final String LOCALTIME1 = LocalTime.of(12, 10).toString();
		public static final String LOCALTIME2 = LocalTime.of(12, 10, 13).toString();
		public static final String LOCALTIME3 = LocalTime.of(12, 10, 13, 14).toString();
		public static final String LOCALTIME4 = LocalTime.parse("01:02:03.123456789").toString();
		public static final String LOCALTIME5 = LocalTime.ofNanoOfDay(123456789123L).toString();
		public static final String LOCALTIME6 = LocalTime.ofSecondOfDay(12345).toString();

		public static final String LOCALDATETIME1 = LocalDateTime.of(2020, Month.JANUARY, 1, 11, 22).toString();
		public static final String LOCALDATETIME2 = LocalDateTime.of(2020, Month.JANUARY, 1, 11, 22, 33).toString();
		public static final String LOCALDATETIME3 = LocalDateTime.of(2020, Month.JANUARY, 1, 11, 22, 33, 44).toString();
		public static final String LOCALDATETIME4 = LocalDateTime.of(2020, 1, 1, 11, 22).toString();
		public static final String LOCALDATETIME5 = LocalDateTime.of(2020, 2, 1, 11, 22, 33).toString();
		public static final String LOCALDATETIME6 = LocalDateTime.of(2020, 3, 1, 11, 22, 33, 44).toString();
		public static final String LOCALDATETIME7 = LocalDateTime
				.of(LocalDate.of(2020, 3, 1), LocalTime.of(11, 22, 33, 44)).toString();
		public static final String LOCALDATETIME8 = LocalDateTime.parse("2020-05-06T17:28:39.123456789").toString();

		public static final String ZONEOFFSET1 = ZoneOffset.of("+01:00").toString();
		public static final String ZONEOFFSET2 = ZoneOffset.ofHours(2).toString();
		public static final String ZONEOFFSET3 = ZoneOffset.ofHoursMinutes(2, 30).toString();
		public static final String ZONEOFFSET4 = ZoneOffset.ofHoursMinutesSeconds(2, 30, 30).toString();

		public static final String OFFSETTIME1 = OffsetTime.of(1, 2, 3, 4, ZoneOffset.ofHours(1)).toString();
		public static final String OFFSETTIME2 = OffsetTime.parse("01:02:03.123456789+01:30").toString();

		public static final String OFFSETDATETIME1 = OffsetDateTime
				.of(2000, 1, 2, 3, 4, 5, 123456, ZoneOffset.ofHoursMinutes(2, 15)).toString();
		public static final String OFFSETDATETIME2 = OffsetDateTime.parse("2000-01-02T03:04:05.123456789+02:30")
				.toString();

		public static Instant getParsedInstant() {
			//the Instant.ofEpochSecond takes 2 long arguments, but the getNanos method returns int
			//this is to check bytecode verification, that the inlined bytecode is correct
			return Instant.parse("2000-01-02T03:04:05.006Z");
		}

		public static Duration getParsedDuration() {
			return Duration.parse("PT0.000123011S");
		}

		public static Duration getParsedDuration2() {
			return Duration.parse("PT2S");
		}

	}
}
