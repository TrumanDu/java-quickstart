package top.trumandu.patterns.strategy;

/**
 * @author Truman.P.Du
 * @date 2022/08/24
 * @description
 */
public interface Processor {


    /**
     * 处理逻辑
     *
     * @param event
     */
    public abstract void handle(String event);
}
