package eva2.tools.chart2d;


import eva2.gui.InterfaceDPointWithContent;
import eva2.gui.InterfaceSelectablePointIcon;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.mocco.paretofrontviewer.InterfaceRefSolutionListener;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.09.2005
 * Time: 10:15:30
 * To change this template use File | Settings | File Templates.
 */
public class Chart2DDPointContentSelectable implements InterfaceDPointWithContent, InterfaceSelectablePointIcon, DPointIcon {

    AbstractEAIndividual                        m_Indy;
    InterfaceOptimizationProblem                m_Problem;
    private InterfaceRefSolutionListener        m_Listener;
    private Color   m_Border    = Color.BLACK;
    private Color   m_Fill      = null;
    private int     m_Size      = 4;

    /** this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    @Override
    public void paint( Graphics g ){
        Color prev = g.getColor();
        if (this.m_Indy.isMarked()) {
            this.m_Fill = Color.RED;
        }
        else {
            this.m_Fill = Color.LIGHT_GRAY;
        }
        g.setColor(m_Fill);
        g.fillOval(-this.m_Size, -this.m_Size, 2*this.m_Size +1, 2*this.m_Size +1);
        if (this.m_Border != null) {
            g.setColor(m_Border);
        }
        g.drawOval(-this.m_Size, -this.m_Size, 2*this.m_Size, 2*this.m_Size);
        g.setColor(prev);
    }

    /** the border which is necessary to be paint around the DPoint that the whole
     * icon is visible
     * @return the border
     */
    @Override
    public DBorder getDBorder() {
        return new DBorder(4, 4, 4, 4);
    }

    public void setBorderColor(Color c) {
        this.m_Border = c;
    }
    public void setFillColor(Color c) {
        this.m_Fill = c;
    }
    public void setSize(int d) {
        this.m_Size = d;
    }

    /*********************************************************************************************
     *  The InterfaceSelectablePointIcon
     */

    /** This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     * @param a The selection listener
     */
    @Override
    public void addSelectionListener(InterfaceRefSolutionListener a) {
        this.m_Listener = a;
    }

    /** This method returns the selection listner to the PointIcon
     * @return InterfaceSelectionListener
     */
    @Override
    public InterfaceRefSolutionListener getSelectionListener() {
        return this.m_Listener;
    }

    /** This method allows to remove the selection listner to the PointIcon
     */
    @Override
    public void removeSelectionListeners() {
        this.m_Listener = null;
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
    }
}