package top.trumandu.patterns.state;

/**
 * @author Truman.P.Du
 * @date 2022/08/09
 * @description
 */
public class Context {
    private State state;

    public Context() {
    }

    public void setState(State state) {
        this.state = state;
    }


    @Override
    public String toString() {
        return "Context{" +
                "state=" + state +
                '}';
    }
}
