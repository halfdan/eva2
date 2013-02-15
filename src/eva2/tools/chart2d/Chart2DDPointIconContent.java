package eva2.tools.chart2d;


import eva2.gui.InterfaceDPointWithContent;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.04.2004
 * Time: 13:31:48
 * To change this template use File | Settings | File Templates.
 */
public class Chart2DDPointIconContent implements InterfaceDPointWithContent, DPointIcon {

    AbstractEAIndividual            m_Indy;
    InterfaceOptimizationProblem    m_Problem;

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    @Override
    public void paint( Graphics g ){
        g.drawOval(-4, -4, 8, 8);
        g.drawLine(-2, 2, 2,-2);
        g.drawLine(-2,-2, 2, 2);
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

    /** This method allows you to set the according individual
     * @param indy  AbstractEAIndividual
     */
    @Override
    public void setEAIndividual(AbstractEAIndividual indy) {
        this.m_Indy = indy;
    }
    @Override
    public AbstractEAIndividual getEAIndividual() {
        return this.m_Indy;
    }

    /** This method allows you to set the according optimization problem
     * @param problem  InterfaceOptimizationProblem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
    }
    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.m_Problem;
    }

    /** This method allows you to draw additional data of the individual
     */
    @Override
    public void showIndividual() {
        JFrame newFrame = new JFrame();
        if (this.m_Indy == null) {
            System.out.println("No individual!");
            return;
        }
        newFrame.setTitle(this.m_Indy.getName()+": "+this.m_Indy);
        newFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.gc();
            }
        });
        newFrame.getContentPane().add(this.m_Problem.drawIndividual(-1, -1, this.m_Indy));
        newFrame.setSize(200, 300);
        newFrame.pack();
        newFrame.validate();
        newFrame.setVisible(true);
        newFrame.show();
    }
}