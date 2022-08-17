package top.trumandu.patterns.decorator;

/**
 * @author Truman.P.Du
 * @date 2020/06/27
 * @description
 */
public abstract class Decorator implements Component {
    Component component;

    public Decorator(Component component) {
        this.component = component;
    }
}
