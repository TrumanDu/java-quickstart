package top.trumandu.patterns.visitor;

/**
 * @author Truman.P.Du
 * @date 2022/07/31
 * @description
 */
public class Client {
    public static void main(String[] args) {
        Animal[] animals = {new Dog(), new Cat(), new Fox(), new Cat(), new Dog(), new Dog()};
        Speaker s = new Speaker();
        Counter c = new Counter();
        for (Animal animal : animals) {
            c.visit(animal);
        }
        c.log();

        for (Animal animal : animals) {
            s.visit(animal);
        }
    }
}
