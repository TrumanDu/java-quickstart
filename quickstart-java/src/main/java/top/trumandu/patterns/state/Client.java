package top.trumandu.patterns.state;

/**
 * @author Truman.P.Du
 * @date 2022/08/09
 * @description 将对象的状态抽象出一个接口，然后根据它的不同状态封装其行为，将状态和行为绑定封装
 */
public class Client {
    public static void main(String[] args) {
        Context context = new Context();
        StartState startState = new StartState();
        startState.handle(context);

        StopState stopState = new StopState();
        stopState.handle(context);
    }
}
