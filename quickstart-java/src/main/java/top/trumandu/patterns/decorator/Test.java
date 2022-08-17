package top.trumandu.patterns.decorator;

/**
 * @author Truman.P.Du
 * @date 2020/06/27
 * @description
 */
public class Test {
    public static void main(String[] args) {
        Component component  = new ConcreteDecoratorB(new ConcreteDecoratorA(new ConcreteComponent()));
        component.operation();
    }
}
