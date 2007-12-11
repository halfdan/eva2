package javaeva.server.go.tools;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.03.2004
 * Time: 13:24:03
 * To change this template use File | Settings | File Templates.
 */

abstract class M {

    protected String m = "M";

    public abstract void init();

    public void setM(String m) {
        this.m = m;
    }
    public String getM() {
        return this.m;
    }

}

class N extends M implements java.io.Serializable {
    public void init() {
        this.m = "N";
    }
}
public class Test5 {
    public static void main(String[] args) {
        N n = new N();
        N a = null;
        n.init();
        System.out.println("N.getM(): " + n.getM());
        try {
            store(n, "N.ser");
        } catch (java.io.IOException e) {
            System.out.println("java.io.IOException while writing: " + e.getMessage());
        }
        try {
            try {
                a = (N)load("N.ser");
            } catch (java.io.IOException e) {
                System.out.println("java.io.IOException while reading: " + e.getMessage());
            }
        } catch (java.lang.ClassNotFoundException e) {
            System.out.println("java.lang.ClassNotFoundException: " + e.getMessage());
        }
        if (a != null) System.out.println("A.getM(): " + a.getM());
    }

    static private void store(Serializable o, String Filename) throws IOException {
        File f = new File(Filename);
        FileOutputStream file = new FileOutputStream(f);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(o);
        out.flush();
        out.close();
        file.close();
    }
    static private Object load(String Filename) throws IOException, ClassNotFoundException {
        File f = new File(Filename);
        FileInputStream file = new FileInputStream(f);
        ObjectInputStream in = new ObjectInputStream(file);
        Object ret = in.readObject();
        in.close();
        file.close();
        return ret;
    }
}
