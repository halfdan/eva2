package eva2.tools.chart2d;


import eva2.gui.InterfaceSelectablePointIcon;
import eva2.gui.plot.InterfaceDPointWithContent;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.mocco.paretofrontviewer.InterfaceRefSolutionListener;
import eva2.problems.InterfaceOptimizationProblem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 */
public class DPointContentSelectable implements InterfaceDPointWithContent, InterfaceSelectablePointIcon, DPointIcon {

    AbstractEAIndividual individual;
    InterfaceOptimizationProblem optimizationProblem;
    private InterfaceRefSolutionListener refSolutionListener;
    private Color borderColor = Color.BLACK;
    private Color fillColor = null;
    private int size = 4;

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    @Override
    public void paint(Graphics g) {
        Color prev = g.getColor();
        if (this.individual.isMarked()) {
            this.fillColor = Color.RED;
        } else {
            this.fillColor = Color.LIGHT_GRAY;
        }
        g.setColor(fillColor);
        g.fillOval(-this.size, -this.size, 2 * this.size + 1, 2 * this.size + 1);
        if (this.borderColor != null) {
            g.setColor(borderColor);
        }
        g.drawOval(-this.size, -this.size, 2 * this.size, 2 * this.size);
        g.setColor(prev);
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

    public void setBorderColor(Color c) {
        this.borderColor = c;
    }

    public void setFillColor(Color c) {
        this.fillColor = c;
    }

    public void setSize(int d) {
        this.size = d;
    }

    /**
     * This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     *
     * @param a The selection listener
     */
    @Override
    public void addSelectionListener(InterfaceRefSolutionListener a) {
        this.refSolutionListener = a;
    }

    /**
     * This method returns the selection listner to the PointIcon
     *
     * @return InterfaceSelectionListener
     */
    @Override
    public InterfaceRefSolutionListener getSelectionListener() {
        return this.refSolutionListener;
    }

    /**
     * This method allows to remove the selection listner to the PointIcon
     */
    @Override
    public void removeSelectionListeners() {
        this.refSolutionListener = null;
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
    }
}