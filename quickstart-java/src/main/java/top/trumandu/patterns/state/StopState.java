package top.trumandu.patterns.state;

/**
 * @author Truman.P.Du
 * @date 2022/08/09
 * @description
 */
public class StopState implements State {
    private String name = "stop";

    @Override
    public void handle(Context context) {
        System.out.println("stop action");
        context.setState(this);
        System.out.println(context.toString());
    }

    @Override
    public String toString() {
        return "StopState{" +
                "name='" + name + '\'' +
                '}';
    }
}
