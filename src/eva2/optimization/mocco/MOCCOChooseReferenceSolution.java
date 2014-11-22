package eva2.optimization.mocco;


import eva2.gui.MOCCOStandalone;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.mocco.paretofrontviewer.InterfaceRefSolutionListener;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.problems.InterfaceOptimizationObjective;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public class MOCCOChooseReferenceSolution extends MOCCOPhase implements InterfaceProcessElement, InterfaceRefSolutionListener {

    AbstractEAIndividual referenceSolution = null;
    JPanel selected;

    public MOCCOChooseReferenceSolution(MOCCOStandalone mocco) {
        this.mocco = mocco;
    }

    /**
     * This method will call the initialize method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.mocco.controlPanel.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to strategy parameterization.");
        tmpB.addActionListener(continue2);
        this.mocco.controlPanel.add(tmpB);
        // the parameter panel
        this.mocco.parameterPanel.removeAll();
        this.mocco.parameterPanel.setLayout(new BorderLayout());
        this.mocco.parameterPanel.add(this.makeHelpText("Please choose a reference solution " +
                "from the Pareto-optimal solutions (grey circles) given to the right."), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        tmpP.add(new JLabel("Currently selected solution:"), BorderLayout.NORTH);
        this.selected = new JPanel();
        tmpP.add(this.selected, BorderLayout.CENTER);
        this.updateSelected();
        this.mocco.view.setRefSolutionSelectable(true);
        this.mocco.view.setUniquelySelectable(true);
        this.mocco.view.addRefSolutionSelectionListener(this);
        this.mocco.parameterPanel.add(tmpP, BorderLayout.CENTER);
        this.mocco.parameterPanel.validate();
        this.mocco.controlPanel.validate();
    }

    private void updateSelected() {
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) this.mocco.state.currentProblem).getProblemObjectives();
        this.selected.removeAll();
        this.selected.setLayout(new BorderLayout());
        if (this.referenceSolution == null) {
            this.selected.add(new JLabel("Selected Individual: none"), BorderLayout.NORTH);
        } else {
            this.selected.add(new JLabel("Selected Individual: " + this.referenceSolution), BorderLayout.NORTH);
            //double[] fitness = this.referenceSolution.getFitness();
            JPanel tmpP = new JPanel();
            JTextField textA;
            tmpP.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 2;
            tmpP.add(new JLabel("Objective:"), gbc);
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 1;
            tmpP.add(new JLabel(""), gbc);
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.weightx = 2;
            tmpP.add(new JLabel("Value"), gbc);
            for (int i = 0; i < obj.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i + 1;
                gbc.weightx = 1;
                tmpP.add(new JLabel("" + obj[i].getIdentName()), gbc);
                gbc.gridx = 1;
                gbc.gridy = i + 1;
                gbc.weightx = 1;
                if (obj[i].is2BMinimized()) {
                    textA = new JTextField("min");
                } else {
                    textA = new JTextField("max");
                }
                textA.setEditable(false);
                tmpP.add(textA, gbc);
                gbc.gridx = 2;
                gbc.gridy = i + 1;
                gbc.weightx = 1;
                textA = new JTextField("" + referenceSolution.getData(obj[i].getIdentName()));
                textA.setEditable(false);
                tmpP.add(textA, gbc);

            }
            this.selected.add(tmpP, BorderLayout.CENTER);
        }
        this.selected.validate();
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (referenceSolution != null) {
                mocco.view.setRefSolutionSelectable(false);
                mocco.view.removeRefSolutionSelectionListeners();
                mocco.controlPanel.removeAll();
                mocco.controlPanel.validate();
                mocco.parameterPanel.removeAll();
                hasFinished = true;
            } else {
                JOptionPane.showMessageDialog(mocco.getMainFrame(),
                        "No reference solution selected. Cannot proceed!",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    public AbstractEAIndividual getReferenceSolution() {
        return this.referenceSolution;
    }
    /******************************************************************************
     *     InterfaceSelectionListener
     */

    /**
     * This method will notify the listener that an
     * Individual has been selected
     *
     * @param indy The selected individual
     */
    @Override
    public void individualSelected(AbstractEAIndividual indy) {
        Population pop = this.mocco.state.paretoFront.getMarkedIndividuals();
        if (pop.size() == 1) {
            this.referenceSolution = pop.get(0);
        } else {
            this.referenceSolution = null;
        }
        this.updateSelected();
    }
}