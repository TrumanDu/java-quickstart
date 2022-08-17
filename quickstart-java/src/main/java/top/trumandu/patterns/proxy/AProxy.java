package top.trumandu.patterns.proxy;

/**
 * @author Truman.P.Du
 * @date 2022/08/17
 * @description
 * 代理模式看起来和装饰器模式有点类似，但是不同的，主要不同点在于：
 * Decorator模式让调用者自己创建核心类，然后组合各种功能，
 * 而Proxy模式决不能让调用者自己创建再组合，否则就失去了代理的功能。
 * Proxy模式让调用者认为获取到的是核心类接口，但实际上是代理类。
 */
public class AProxy implements A {
    private A a;

    public AProxy(A a) {
        this.a = a;
    }

    /**
     * 模拟权限校验
     *
     * @return
     */
    private boolean isHasPower() {
        return true;
    }


    @Override
    public void a() {
        if (isHasPower()) {
            a.a();
        }else {
            throw new SecurityException("Forbidden");
        }
    }
}
