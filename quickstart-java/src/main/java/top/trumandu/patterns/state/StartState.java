package top.trumandu.patterns.state;

/**
 * @author Truman.P.Du
 * @date 2022/08/09
 * @description
 */
public class StartState implements State {
    private String name = "start";

    @Override
    public void handle(Context context) {
        System.out.println("start action");
        context.setState(this);
        System.out.println(context.toString());
    }

    @Override
    public String toString() {
        return "StartState{" +
                "name='" + name + '\'' +
                '}';
    }
}
