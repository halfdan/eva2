package eva2.optimization.mocco;


import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.mocco.paretofrontviewer.InterfaceRefPointListener;
import eva2.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.problems.InterfaceOptimizationObjective;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public class MOCCOChooseReferencePoint extends MOCCOPhase implements InterfaceProcessElement, InterfaceRefPointListener {

    private JTextField[] textField;
    private double[] referencePoint;
    JPanel selectedPanel;

    public MOCCOChooseReferencePoint(MOCCOStandalone mocco) {
        this.mocco = mocco;
    }

    /**
     * This method will call the initialize method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.mocco.controlPanel.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to algorithm parametrization.");
        tmpB.addActionListener(continue2);
        this.mocco.controlPanel.add(tmpB);
        // the parameter panel
        this.mocco.parameterPanel.removeAll();
        this.mocco.parameterPanel.setLayout(new BorderLayout());
        this.mocco.parameterPanel.add(this.makeHelpText("Choose a reference point of aspiration levels" +
                " which are not necessarily attainable, but will be used as goal for the following optimzation" +
                " process. For the sake of simplicity this reference point has to be selected in fitness space only!"), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        tmpP.add(new JLabel("Currently selected solution:"), BorderLayout.NORTH);
        this.selectedPanel = new JPanel();
        tmpP.add(this.selectedPanel, BorderLayout.CENTER);
        this.mocco.view.setRefPointSelectable(true);
        this.mocco.view.addRefPointSelectionListener(this);
        this.updateSelected();
        this.mocco.parameterPanel.add(tmpP, BorderLayout.CENTER);
        this.mocco.parameterPanel.validate();
        this.mocco.controlPanel.validate();
    }

    private void updateSelected() {
        this.selectedPanel.removeAll();
        InterfaceOptimizationObjective[] objectives = ((InterfaceMultiObjectiveDeNovoProblem) this.mocco.state.currentProblem).getProblemObjectives();
        this.selectedPanel.setLayout(new BorderLayout());
        if (this.mocco.view.referencePoint == null) {
            this.selectedPanel.add(new JLabel("No reference point! Wierd there should be a default value!?"), BorderLayout.NORTH);
        } else {
            this.selectedPanel.add(new JLabel("Selected Reference Point:"), BorderLayout.NORTH);
            JPanel tmpP = new JPanel();
            this.textField = new JTextField[this.mocco.view.referencePoint.length];
            tmpP.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            for (int i = 0; i < this.mocco.view.referencePoint.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.weightx = 1;
                tmpP.add(new JLabel("Fitness " + (i + 1)), gbc);
                gbc.gridx = 1;
                gbc.gridy = i;
                gbc.weightx = 1;
                this.textField[i] = new JTextField("" + this.mocco.view.referencePoint[i]);
                this.textField[i].setEditable(true);
                this.textField[i].addActionListener(refPointEdited);
                tmpP.add(this.textField[i], gbc);
            }
            this.selectedPanel.add(tmpP, BorderLayout.CENTER);
        }
        this.selectedPanel.validate();
    }

    /**
     * This method returns the reference point
     *
     * @return double[]     The reference point
     */
    public double[] getReferencePoint() {
        return this.referencePoint;
    }

    ActionListener refPointEdited = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            for (int i = 0; i < textField.length; i++) {
                mocco.view.referencePoint[i] = Double.parseDouble(textField[i].getText());
            }
            mocco.view.problemChanged(false);
        }
    };

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            referencePoint = mocco.view.referencePoint;
            mocco.view.setRefPointSelectable(false);
            mocco.view.removeRefPointSelectionListeners();
            mocco.controlPanel.removeAll();
            mocco.controlPanel.validate();
            mocco.parameterPanel.removeAll();
            hasFinished = true;
        }
    };

    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /**
     * This method will notify the listener that a point has been selected
     * It has to be noted though that at this point the reference point is not
     * a full vector since it is only 2d
     *
     * @param point The selected point, most likely 2d
     */
    @Override
    public void refPointGiven(double[] point) {
        this.referencePoint = point;
        this.updateSelected();
    }
}