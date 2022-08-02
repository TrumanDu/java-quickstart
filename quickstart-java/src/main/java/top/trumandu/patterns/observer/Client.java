package top.trumandu.patterns.observer;

/**
 * @author Truman.P.Du
 * @date 2022/08/02
 * @description
 */
public class Client {
    public static void main(String[] args) {
        Observer one = new OneObserver();
        Observer two = new TwoObserver();

        Subject subject = new ConcreteSubject();
        subject.register(one);
        subject.register(two);

        subject.notifyAllObjects("hi ");
    }
}
