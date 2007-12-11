package javaeva.server.go.mocco;

import javaeva.server.go.MOCCOStandalone;
import javaeva.server.go.mocco.paretofrontviewer.InterfaceRefPointListener;
import javaeva.server.go.problems.InterfaceMultiObjectiveDeNovoProblem;
import javaeva.server.go.problems.InterfaceOptimizationObjective;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2005
 * Time: 18:42:30
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOChooseReferencePoint extends MOCCOPhase implements InterfaceProcessElement, InterfaceRefPointListener {

    private JTextField[]    m_JTextField;
    private double[]        m_ReferencePoint;
    JPanel                  m_Selected;

    public MOCCOChooseReferencePoint(MOCCOStandalone mocco) {
        this.m_Mocco = mocco;
    }

    /** This method will call the init method and will go to stall
     */
    public void initProcessElementParametrization() {
        this.m_Mocco.m_JPanelControl.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to algorithm parametrization.");
        tmpB.addActionListener(continue2);
        this.m_Mocco.m_JPanelControl.add(tmpB);
        // the parameter panel
        this.m_Mocco.m_JPanelParameters.removeAll();
        this.m_Mocco.m_JPanelParameters.setLayout(new BorderLayout());
        this.m_Mocco.m_JPanelParameters.add(this.makeHelpText("Choose a reference point of aspiration levels" +
                " which are not necessarily attainable, but will be used as goal for the following optimzation" +
                " process. For the sake of simplicity this reference point has to be selected in fitness space only!"), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        tmpP.add(new JLabel("Currently selected solution:"), BorderLayout.NORTH);
        this.m_Selected = new JPanel();
        tmpP.add(this.m_Selected, BorderLayout.CENTER);
        this.m_Mocco.m_View.setRefPointSelectable(true);
        this.m_Mocco.m_View.addRefPointSelectionListener(this);
        this.updateSelected();
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.CENTER);
        this.m_Mocco.m_JPanelParameters.validate();
        this.m_Mocco.m_JPanelControl.validate();
    }

    private void updateSelected() {
        this.m_Selected.removeAll();
        InterfaceOptimizationObjective[] objectives = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();
        this.m_Selected.setLayout(new BorderLayout());
        if (this.m_Mocco.m_View.m_ReferencePoint == null) {
            this.m_Selected.add(new JLabel("No reference point! Wierd there should be a default value!?"), BorderLayout.NORTH);
        } else {
            this.m_Selected.add(new JLabel("Selected Reference Point:"), BorderLayout.NORTH);
            JPanel tmpP = new JPanel();
            this.m_JTextField = new JTextField[this.m_Mocco.m_View.m_ReferencePoint.length];
            tmpP.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor      = GridBagConstraints.WEST;
            gbc.fill        = GridBagConstraints.BOTH;
            for (int i = 0; i < this.m_Mocco.m_View.m_ReferencePoint.length; i++) {
                gbc.gridx       = 0;
                gbc.gridy       = i;
                gbc.weightx     = 1;
                tmpP.add(new JLabel("Fitness "+(i+1)), gbc);
                gbc.gridx       = 1;
                gbc.gridy       = i;
                gbc.weightx     = 1;
                this.m_JTextField[i] = new JTextField(""+this.m_Mocco.m_View.m_ReferencePoint[i]);
                this.m_JTextField[i].setEditable(true);
                this.m_JTextField[i].addActionListener(refPointEdited);
                tmpP.add(this.m_JTextField[i], gbc);
            }
            this.m_Selected.add(tmpP, BorderLayout.CENTER);
        }
        this.m_Selected.validate();
    }

    /** This method returns the reference point
     * @return double[]     The reference point
     */
    public double[] getReferencePoint() {
        return this.m_ReferencePoint;
    }

    ActionListener refPointEdited = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            for (int i = 0; i < m_JTextField.length; i++) {
                m_Mocco.m_View.m_ReferencePoint[i] = new Double(m_JTextField[i].getText()).doubleValue();
            }
            m_Mocco.m_View.problemChanged(false);
        }
    };

    ActionListener continue2 = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            m_ReferencePoint = m_Mocco.m_View.m_ReferencePoint;
            m_Mocco.m_View.setRefPointSelectable(false);
            m_Mocco.m_View.removeRefPointSelectionListeners();
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelControl.validate();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Finished = true;
        }
    };

    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /** This method will notify the listener that a point has been selected
     * It has to be noted though that at this point the reference point is not
     * a full vector since it is only 2d
     * @param point  The selected point, most likely 2d
     */
    public void refPointGiven(double[] point) {
        this.m_ReferencePoint = point;
        this.updateSelected();
    }
}