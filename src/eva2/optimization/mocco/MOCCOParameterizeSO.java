package eva2.optimization.mocco;


import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.go.MOCCOStandalone;
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
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 26.10.2005
 * Time: 16:04:38
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOParameterizeSO extends MOCCOPhase implements InterfaceProcessElement {


    public MOCCOParameterizeSO(MOCCOStandalone mocco) {
        this.m_Mocco = mocco;
    }

    /**
     * This method will call the init method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.m_Mocco.m_JPanelControl.removeAll();

        // The button panel
        JButton tmpB = new JButton("Start optimization.");
        tmpB.setToolTipText("Start the adhoc online optimization.");
        tmpB.addActionListener(continue2);
        this.m_Mocco.m_JPanelControl.add(tmpB);
        tmpB = new JButton("Save task.");
        tmpB.setToolTipText("Save the optimization problem and algorithm to *.ser file for offline optimization.");
        tmpB.addActionListener(saveState2FileForOfflineOptimization);
        this.m_Mocco.m_JPanelControl.add(tmpB);

        // the parameter panel
        this.init();

        this.m_Mocco.m_JFrame.setVisible(true);
        this.m_Mocco.m_JFrame.validate();
    }

    private void init() {
        if (this.m_Mocco.m_State.m_Optimizer instanceof MultiObjectiveEA) {
            JOptionPane.showMessageDialog(this.m_Mocco.m_JFrame,
                    "The current " + this.m_Mocco.m_State.m_Optimizer.getName() +
                            " is no single-objective optimizer. I'm defaulting to " +
                            "a Genetic Algorithms, please parameterize accordingly.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            this.m_Mocco.m_State.m_Optimizer = new GeneticAlgorithm();
            this.m_Mocco.m_State.m_Optimizer.setProblem(this.m_Mocco.m_State.m_CurrentProblem);
        }
        this.m_Mocco.m_JPanelParameters.removeAll();
        this.m_Mocco.m_JPanelParameters.setLayout(new BorderLayout());
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        GeneralOptimizationEditorProperty editor = new GeneralOptimizationEditorProperty();
        editor.m_Name = "Optimizer";
        try {
            editor.m_Value = this.m_Mocco.m_State.m_Optimizer;
            editor.m_Editor = PropertyEditorProvider.findEditor(editor.m_Value.getClass());
            if (editor.m_Editor == null) {
                editor.m_Editor = PropertyEditorProvider.findEditor(InterfaceOptimizer.class);
            }
            if (editor.m_Editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) editor.m_Editor).setClassType(InterfaceOptimizer.class);
            }
            editor.m_Editor.setValue(editor.m_Value);
            AbstractObjectEditor.findViewFor(editor);
            if (editor.m_View != null) {
                editor.m_View.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        tmpP.add(new JLabel("" + editor.m_Name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 2;
        tmpP.add(editor.m_View, gbc);

        editor = new GeneralOptimizationEditorProperty();
        editor.m_Name = "Terminator";
        try {
            editor.m_Value = this.m_Mocco.m_State.m_Terminator;
            editor.m_Editor = PropertyEditorProvider.findEditor(editor.m_Value.getClass());
            if (editor.m_Editor == null) {
                editor.m_Editor = PropertyEditorProvider.findEditor(InterfaceTerminator.class);
            }
            if (editor.m_Editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) editor.m_Editor).setClassType(InterfaceTerminator.class);
            }
            editor.m_Editor.setValue(editor.m_Value);
            AbstractObjectEditor.findViewFor(editor);
            if (editor.m_View != null) {
                editor.m_View.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        tmpP.add(new JLabel("" + editor.m_Name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 2;
        tmpP.add(editor.m_View, gbc);
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.CENTER);
        this.m_Mocco.m_JPanelParameters.add(this.makeInformationText("Single-Objective Optimiaztion", "" +
                "Please choose an appropriate single-objecitve optimizer."), BorderLayout.NORTH);

    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Finished = true;
        }
    };
}
