package eva2.optimization.mocco;


import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.MultiObjectiveEA;
import eva2.optimization.tools.AbstractObjectEditor;
import eva2.optimization.tools.GeneralOptimizationEditorProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public class MOCCOParameterizeMO extends MOCCOPhase implements InterfaceProcessElement {

    public MOCCOParameterizeMO(MOCCOStandalone mocco) {
        this.mocco = mocco;
//        this.optimizer = (InterfaceOptimizer)this.mocco.state.optimizer.clone();
    }

    /**
     * This method will call the init method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.mocco.controlPanel.removeAll();

        // The button panel
        JButton tmpB = new JButton("Start optimization.");
        tmpB.setToolTipText("Start the adhoc online optimization.");
        tmpB.addActionListener(continue2);
        this.mocco.controlPanel.add(tmpB);
        tmpB = new JButton("Save task.");
        tmpB.setToolTipText("Save the optimization problem and algorithm to *.ser file for offline optimization.");
        tmpB.addActionListener(saveState2FileForOfflineOptimization);
        this.mocco.controlPanel.add(tmpB);

        // the parameter panel
        this.init();

        this.mocco.getMainFrame().setVisible(true);
        this.mocco.getMainFrame().validate();
    }

    private void init() {
        if (!(this.mocco.state.optimizer instanceof MultiObjectiveEA)) {
            if (this.mocco.state.optimizer instanceof GeneticAlgorithm) {
                JOptionPane.showMessageDialog(this.mocco.getMainFrame(),
                        "The current " + this.mocco.state.optimizer.getName() +
                                " is not necessarily a good multi-objective optimizer, please " +
                                "parameterize accordingly or change to MultiObjectiveEA",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
//            JOptionPane.showMessageDialog(this.mocco.m_JFrame,
//                "The current "+this.mocco.state.optimizer.getName() +
//                " is typically a single-objective optimizer. I'm defaulting to a " +
//                "multi-objective EA instead, please parameterize accordingly.",
//                "Warning", JOptionPane.WARNING_MESSAGE);
//            this.mocco.state.optimizer = new MultiObjectiveEA();
        }
        this.mocco.parameterPanel.removeAll();
        this.mocco.parameterPanel.setLayout(new BorderLayout());
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        GeneralOptimizationEditorProperty editor = new GeneralOptimizationEditorProperty();
        editor = new GeneralOptimizationEditorProperty();
        editor.name = "Optimizer";
        try {
            editor.value = this.mocco.state.optimizer;
            editor.editor = PropertyEditorProvider.findEditor(editor.value.getClass());
            if (editor.editor == null) {
                editor.editor = PropertyEditorProvider.findEditor(InterfaceOptimizer.class);
            }
            if (editor.editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) editor.editor).setClassType(InterfaceOptimizer.class);
            }
            editor.editor.setValue(editor.value);
            AbstractObjectEditor.findViewFor(editor);
            if (editor.view != null) {
                editor.view.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        tmpP.add(new JLabel("" + editor.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 2;
        tmpP.add(editor.view, gbc);

        editor = new GeneralOptimizationEditorProperty();
        editor.name = "Terminator";
        try {
            editor.value = this.mocco.state.terminator;
            editor.editor = PropertyEditorProvider.findEditor(editor.value.getClass());
            if (editor.editor == null) {
                editor.editor = PropertyEditorProvider.findEditor(InterfaceTerminator.class);
            }
            if (editor.editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) editor.editor).setClassType(InterfaceTerminator.class);
            }
            editor.editor.setValue(editor.value);
            AbstractObjectEditor.findViewFor(editor);
            if (editor.view != null) {
                editor.view.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        tmpP.add(new JLabel("" + editor.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 2;
        tmpP.add(editor.view, gbc);
        this.mocco.parameterPanel.add(tmpP, BorderLayout.CENTER);
        this.mocco.parameterPanel.add(this.makeInformationText("Multi-Objective Optimiaztion", "" +
                "Please choose an appropriate multi-objecitve optimizer."), BorderLayout.NORTH);
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            //mocco.state.optimizer = (InterfaceOptimizer)optimizer.clone();
            mocco.controlPanel.removeAll();
            mocco.parameterPanel.removeAll();
            mocco.state.optimizer.setProblem(mocco.state.currentProblem);
            Population pop = mocco.state.optimizer.getPopulation();
            pop.clear();
            if (pop.getArchive() != null) {
                pop.getArchive().clear();
            }
            if (mocco.state.populationHistory.length > 0) {
                pop = mocco.state.getSelectedPopulations();
                mocco.state.optimizer.initByPopulation(pop, false);
                if (pop.size() == 0) {
                    mocco.state.optimizer.init();
                }
            }
            hasFinished = true;
        }
    };
}
