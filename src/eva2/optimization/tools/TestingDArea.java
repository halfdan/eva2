package eva2.optimization.tools;

import eva2.tools.chart2d.*;

import java.awt.*;
import javax.swing.*;

/**
 *
 */
public class TestingDArea {

    public TestingDArea() {
        this.init();
    }

    private void init() {
        JFrame frame = new JFrame("Testing DArea");
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);

        DArea area = new DArea();
        area.setBackground(Color.white);
        area.setPreferredSize(new Dimension(600, 500));
        area.setVisibleRectangle(1, 1, 100000, 1000);
        area.setVisibleRectangle(0, -3, 10, 10);//plotArea.setAutoFocus(true);
        area.setMinRectangle(0, 0, 1, 1);
        ScaledBorder myBorder = new ScaledBorder();
        myBorder.x_label = "x";//"App. " + Name + " func. calls";
        myBorder.y_label = "y";//"fitness";
        area.setBorder(myBorder);
        area.setAutoGrid(true);
        area.setGridVisible(true);
        DRectangle rect = new DRectangle(1, 1, 2, 2);
        rect.setColor(Color.black);
        rect.setFillColor(Color.red);
        DPointSet points = new DPointSet();
        points.addDPoint(2, 3);
        points.addDPoint(4, 5);
        area.addDElement(rect);
        area.addDElement(points);
        panel.add(area);


        frame.setSize(100, 200);
        frame.pack();
        frame.validate();
        frame.setVisible(true);
        frame.show();
    }

    public static void main(String[] args) {
        TestingDArea a = new TestingDArea();
    }
}
