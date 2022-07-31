package top.trumandu.patterns.visitor;

/**
 * @author Truman.P.Du
 * @date 2022/07/31
 * @description
 */
public class Counter implements Visitor{
    int cat = 0;
    int dog = 0;
    int fox = 0;

    public void log() {
        System.out.println("There are " + fox + " foxes, " + dog + " dogs, " + cat + " cats.");
    }

    public void visit(Animal animal) {
        animal.accept(this);
    }

    public void visit(Cat cat) {
        this.cat++;
    }

    public void visit(Dog dog) {
        this.dog++;
    }

    public void visit(Fox fox) {
        this.fox++;
    }
}
