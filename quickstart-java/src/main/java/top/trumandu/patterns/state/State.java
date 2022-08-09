package top.trumandu.patterns.state;

/**
 * @author Truman.P.Du
 * @date 2022/08/09
 * @description
 */
public interface State {
    /**
     * 改变状态的行为
     *
     * @param context
     */
    void handle(Context context);
}
