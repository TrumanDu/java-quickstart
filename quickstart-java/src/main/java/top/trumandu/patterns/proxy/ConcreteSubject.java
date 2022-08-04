package top.trumandu.patterns.proxy;

/**
 * @author: Truman.P.Du
 * @date: 2020/6/26 22:33
 * @description:
 */
public class ConcreteSubject implements Subject {
    @Override
    public void request() {
        System.out.println("执行请求");
    }
}
