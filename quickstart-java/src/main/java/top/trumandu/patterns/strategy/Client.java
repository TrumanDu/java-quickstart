package top.trumandu.patterns.strategy;

/**
 * @author Truman.P.Du
 * @date 2022/08/24
 * @description
 */
public class Client {
    public static void main(String[] args) {

        String event = "hello truman";
        Processor email = new EmailProcessor();
        email.handle(event);
        Processor sms = new SmsProcessor();
        sms.handle(event);
    }
}
