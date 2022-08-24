package top.trumandu.patterns.strategy;

/**
 * @author Truman.P.Du
 * @date 2022/08/24
 * @description
 */
public class SmsProcessor implements Processor {
    public String type = "sms";

    @Override
    public void handle(String event) {
        System.out.println(type + ":  " + event);
    }
}
