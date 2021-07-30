package top.trumandu.date;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * @author Truman.P.Du
 * @date 2019/12/15
 * @description
 */
public class DateUtil {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static DateTimeFormatter zone_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static String getPstTime(String utc) {
        ZonedDateTime utcTime = ZonedDateTime.parse(utc);
        ZonedDateTime pst = utcTime.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
        return formatter.format(pst);
    }

    public static String getPstDate(String utc) {
        ZonedDateTime utcTime = ZonedDateTime.parse(utc);
        ZonedDateTime pst = utcTime.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
        return date_formatter.format(pst);
    }

    public static String getPstZoneTime(String utc) {
        ZonedDateTime utcTime = ZonedDateTime.parse(utc);
        ZonedDateTime pst = utcTime.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
        return zone_formatter.format(pst);
    }

    public static String getCttTime(String utc) {
        ZonedDateTime utcTime = ZonedDateTime.parse(utc);
        ZonedDateTime pst = utcTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
        return formatter.format(pst);
    }

    public static void main(String[] args) {
        String time = "2019-12-14T11:58:43.308Z";
        String time2 = "2019-12-14T14:58:43.308Z";
        System.out.println("between:"+ Duration.between(Instant.parse(time),Instant.parse(time2)).toMinutes());
        System.out.println("utc:"+time);
        System.out.println("cn:" + DateUtil.getCttTime(time));
        System.out.println("pst:" + DateUtil.getPstTime(time));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        System.out.println(dateTimeFormatter.format(ZonedDateTime.parse(time)));

        System.out.println(DateUtil.getPstZoneTime(time));
        System.out.println(LocalDateTime.parse("2019-12-13T03:58:43.308-0800", zone_formatter).atZone(ZoneId.of("America/Los_Angeles")).toEpochSecond());
        System.out.println(ZonedDateTime.parse("2019-12-13T03:58:43.308-0800", zone_formatter).toEpochSecond());
        //System.out.println("between:"+ Duration.between(Instant.parse("2019-12-13T03:58:43.308-0800"),Instant.parse("2019-12-14T03:58:43.308-0800")).toMinutes());
    }
}
