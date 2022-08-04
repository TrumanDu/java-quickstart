package top.trumandu.patterns.decorator;

/**
 * @author Truman.P.Du
 * @date 2020/06/27
 * @description
 */
public class Decorator implements Component {
    Component component;

    public Decorator(Component component) {
        this.component = component;
    }

    @Override
    public void operation() {
        component.operation();
    }
}
