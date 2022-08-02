package top.trumandu.patterns.observer;

/**
 * @author Truman.P.Du
 * @date 2022/08/02
 * @description
 */
public class OneObserver implements Observer {
    @Override
    public void onEvent(String message) {
        System.out.println("one:" + message);
    }
}
