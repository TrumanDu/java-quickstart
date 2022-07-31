package top.trumandu.patterns.visitor;

public interface Animal {
    public void accept(Visitor v);
}

class Cat implements Animal {
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}

class Dog implements Animal {
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}

class Fox implements Animal {
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
