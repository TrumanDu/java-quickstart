package top.trumandu.patterns.observer;

/**
 * @author Truman.P.Du
 * @date 2022/08/02
 * @description
 */
public class ConcreteSubject extends Subject {
    @Override
    public void notifyAllObjects(String message) {
        for (Observer observer : observers) {
            observer.onEvent(message);
        }
    }
}
