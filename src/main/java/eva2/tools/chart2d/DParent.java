package eva2.tools.chart2d;

public interface DParent {
    void addDElement(DElement e);

    boolean removeDElement(DElement e);

    void repaint(DRectangle r);

    DElement[] getDElements();

    boolean contains(DElement e);

    void addDBorder(DBorder b);

    void restoreBorder();
}