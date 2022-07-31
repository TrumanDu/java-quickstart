package top.trumandu.patterns.visitor;

/**
 * @author Truman.P.Du
 * @date 2022/07/31
 * @description 访问者模式
 * 适合数据结构（例如本例子中Animal）不发生变化，操作(Speaker/Counter)需要频繁增加的场景
 */
public interface Visitor {
    void visit(Animal a);

    void visit(Dog d);

    void visit(Cat c);

    void visit(Fox f);
}

