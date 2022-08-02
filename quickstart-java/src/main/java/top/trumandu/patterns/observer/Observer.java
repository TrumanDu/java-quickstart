package top.trumandu.patterns.observer;

/**
 * @author Truman.P.Du
 * @date 2022/08/02
 * @description
 */
public interface Observer {
    /**
     * 接受事件
     *
     * @param message
     */
    void onEvent(String message);
}
