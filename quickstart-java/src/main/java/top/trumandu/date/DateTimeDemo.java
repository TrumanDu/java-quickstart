package top.trumandu.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Truman.P.Du
 * @date 2021/07/30
 * @description
 */
public class DateTimeDemo {
    public static void main(String[] args) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2021-07-30T00:03:33.609Z");
        System.out.println(zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH")));
    }
}
