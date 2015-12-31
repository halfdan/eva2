package eva2.tools;

public class DummyClass {
    private int i;
    private String text;

    public DummyClass(Integer i) {
        this.i = i;
    }

    public DummyClass() {
        this.i = 42;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
