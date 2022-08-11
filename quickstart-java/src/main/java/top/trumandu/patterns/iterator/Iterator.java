package top.trumandu.patterns.iterator;

/**
 * @author Truman.P.Du
 * @date 2022/08/11
 * @description
 */
public interface Iterator {
    /**
     * 是否存在下一个元素
     *
     * @return
     */
    boolean hasNext();

    /**
     * 获取下一个元素
     *
     * @return
     */
    Object next();
}
