package wxg.datetime;

import android.text.format.Time;

/**
 * Created by wxg on 2018/3/8.
 */

public class DateTime {
    /** The millis from 1970-01-01T00:00:00Z */
    private volatile long iMillis;
    /** The chronology to use */
//    private volatile Chronology iChronology;
//    public static DateTime now() {
//        return new DateTime();
//    }
//    public static DateTime now(DateTimeZone zone) {
//        if (zone == null) {
//            throw new NullPointerException("Zone must not be null");
//        }
//        return new DateTime(zone);
//    }
//    public DateTime() {
//        Time time = Time.
//        super();
//    }
//    public DateTime(long instant) {
//        super(instant);
//    }
//    public DateTime(long instant, DateTimeZone zone) {
//        super(instant, zone);
//    }
//    public DateTime(long instant, Chronology chronology) {
//        super(instant, chronology);
//    }
//    public DateTime(Object instant) {
//        super(instant, (Chronology) null);
//    }
//    public DateTime(
//            int year,
//            int monthOfYear,
//            int dayOfMonth,
//            int hourOfDay,
//            int minuteOfHour) {
//        iChronology = checkChronology(chronology);
//        long instant = iChronology.getDateTimeMillis(year, monthOfYear, dayOfMonth,
//                hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
//        iMillis = checkInstant(instant, iChronology);
//        adjustForMinMax();
//    }
//    public DateTime(
//            int year,
//            int monthOfYear,
//            int dayOfMonth,
//            int hourOfDay,
//            int minuteOfHour,
//            DateTimeZone zone) {
//        super(year, monthOfYear, dayOfMonth,
//                hourOfDay, minuteOfHour, 0, 0, zone);
//    }
//    public DateTime(
//            int year,
//            int monthOfYear,
//            int dayOfMonth,
//            int hourOfDay,
//            int minuteOfHour,
//            Chronology chronology) {
//        super(year, monthOfYear, dayOfMonth,
//                hourOfDay, minuteOfHour, 0, 0, chronology);
//    }
//    public DateTime(
//            int year,
//            int monthOfYear,
//            int dayOfMonth,
//            int hourOfDay,
//            int minuteOfHour,
//            int secondOfMinute,
//            int millisOfSecond) {
//        super(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
//    }
//    private void adjustForMinMax() {
//        if (iMillis == Long.MIN_VALUE || iMillis == Long.MAX_VALUE) {
//            iChronology = iChronology.withUTC();
//        }
//    }
//
//    public static DateTime now(Chronology chronology) {
//        if (chronology == null) {
//            throw new NullPointerException("Chronology must not be null");
//        }
//        return new DateTime(chronology);
//    }
//    public DateTime(int year, int month, int day, int i, int i1, int i2, int i3) {
//    }
//
//    public DateTime dayOfWeek() {
//    }
//
//    public DateTime withTime(int i, int i1, int i2, int i3) {
//    }
//
//    public static DateTime now() {
//    }
//
//    public DateTime withMinimumValue() {
//    }
//
//    public DateTime dayOfYear() {
//    }
//
//    public Object getMillis() {
//        return millis;
//    }
//
//    public DateTime withMaximumValue() {
//    }
//    public static DateTime parse(){
//
//    }
//    @FromString
//    public static DateTime parse(String str) {
//        return parse(str, DateTimeFormat.dateTimeParser().withOffsetParsed());
//    }
//    public static DateTime parse(String str, DateTimeFormatter formatter) {
//        return formatter.parseDateTime(str);
//    }
//    public DateTime plusDays(int i) {
//    }
//
//    public DateTime dayOfMonth() {
//    }
//
//    public int getDayOfWeek() {
//        return dayOfWeek;
//    }
//
//    public DateTime millisOfDay() {
//    }
}
