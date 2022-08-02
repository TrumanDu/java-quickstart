package top.trumandu.patterns.observer;

/**
 * @author Truman.P.Du
 * @date 2022/08/02
 * @description
 */
public class TwoObserver implements Observer {
    @Override
    public void onEvent(String message) {
        System.out.println("two:" + message);
    }
}
