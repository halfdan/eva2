package eva2.tools.chart2d;


import eva2.gui.plot.InterfaceDPointWithContent;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.problems.InterfaceOptimizationProblem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.04.2004
 * Time: 13:31:48
 * To change this template use File | Settings | File Templates.
 */
public class Chart2DDPointIconContent implements InterfaceDPointWithContent, DPointIcon {

    AbstractEAIndividual individual;
    InterfaceOptimizationProblem optimizationProblem;

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    @Override
    public void paint(Graphics g) {
        g.drawOval(-4, -4, 8, 8);
        g.drawLine(-2, 2, 2, -2);
        g.drawLine(-2, -2, 2, 2);
    }

    /**
     * the border which is necessary to be paint around the DPoint that the whole
     * icon is visible
     *
     * @return the border
     */
    @Override
    public DBorder getDBorder() {
        return new DBorder(4, 4, 4, 4);
    }

    /**
     * This method allows you to set the according individual
     *
     * @param indy AbstractEAIndividual
     */
    @Override
    public void setEAIndividual(AbstractEAIndividual indy) {
        this.individual = indy;
    }

    @Override
    public AbstractEAIndividual getEAIndividual() {
        return this.individual;
    }

    /**
     * This method allows you to set the according optimization problem
     *
     * @param problem InterfaceOptimizationProblem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.optimizationProblem = problem;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.optimizationProblem;
    }

    /**
     * This method allows you to draw additional data of the individual
     */
    @Override
    public void showIndividual() {
        JFrame newFrame = new JFrame();
        if (this.individual == null) {
            System.out.println("No individual!");
            return;
        }
        newFrame.setTitle(this.individual.getName() + ": " + this.individual);
        newFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.gc();
            }
        });
        newFrame.getContentPane().add(this.optimizationProblem.drawIndividual(-1, -1, this.individual));
        newFrame.setSize(200, 300);
        newFrame.pack();
        newFrame.validate();
        newFrame.setVisible(true);
        newFrame.show();
    }
}