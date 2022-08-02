package top.trumandu.patterns.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Truman.P.Du
 * @date 2022/08/02
 * @description
 */
public abstract class Subject {
    public List<Observer> observers = new ArrayList<>();

    public void register(Observer observer) {
        observers.add(observer);
    }

    public void remove(Observer observer) {
        observers.remove(observer);
    }

    /**
     * 通知所有的观察者
     *
     * @param message
     */
    public abstract void notifyAllObjects(String message);
}
