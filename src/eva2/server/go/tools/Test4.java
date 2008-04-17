package eva2.server.go.tools;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.03.2004
 * Time: 13:20:21
 * To change this template use File | Settings | File Templates.
 */

class A {
    public String getString() {
        return this.getMyString();
    }

    private String getMyString() {
        return "A";
    }
}

class B extends A {
    private String getMyString() {
        return "B";
    }
}

public class Test4 {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        System.out.println("A.getString(): " + a.getString());
        System.out.println("B.getString(): " + b.getString());
        for (int i = 0; i < 10; i++) System.out.println("i: " + (i%5));
    }
}
