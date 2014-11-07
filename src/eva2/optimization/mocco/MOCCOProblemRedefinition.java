package eva2.optimization.mocco;


import eva2.gui.JParaPanel;
import eva2.optimization.go.MOCCOStandalone;
import eva2.problems.InterfaceOptimizationProblem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 */
public class MOCCOProblemRedefinition extends MOCCOPhase implements InterfaceProcessElement {

    private InterfaceOptimizationProblem optimizationProblem;

    public MOCCOProblemRedefinition(MOCCOStandalone mocco) {
        this.mocco = mocco;
        this.optimizationProblem = (InterfaceOptimizationProblem) this.mocco.state.currentProblem.clone();
    }

    /**
     * This method will call the initialize method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.mocco.controlPanel.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to choose the optimization strategy.");
        tmpB.setToolTipText("This finializes the problem redefinition process.");
        tmpB.addActionListener(continue2);
        this.mocco.controlPanel.add(tmpB);

        // the parameter panel
        this.mocco.parameterPanel.removeAll();
        JComponent tmpC = new JPanel();
        tmpC.setLayout(new BorderLayout());
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.gridx = 0;
        gbc.gridy = 0;
        tmpP.add(this.makeHelpText("Choose and parameterize the optimization problem to solve by means of MOCCO. " +
                "Please note that here you got the opportunity to reduce the problem dimension by means of" +
                " weight aggregation, contraints or even by removing secondary objectives completely. Whenever" +
                " possbile make use of this chance. This not only reduces the complexity of the optimization " +
                "problem by also reduces the complecity of the decision process, when it comes to select from " +
                "the solution alternatives."), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        tmpB = new JButton("Reevaluate Problem");
        tmpB.addActionListener(reevaluate);
        tmpP.add(tmpB, gbc);
        this.mocco.parameterPanel.setLayout(new BorderLayout());
        this.mocco.parameterPanel.add(tmpP, BorderLayout.NORTH);

        JParaPanel paraPanel = new JParaPanel(this.optimizationProblem, "MyGUI");
        tmpC = (paraPanel.makePanel());
        this.mocco.parameterPanel.add(tmpC, BorderLayout.CENTER);

        this.mocco.getMainFrame().setVisible(true);
        this.mocco.getMainFrame().validate();
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            mocco.state.currentProblem = (InterfaceOptimizationProblem) optimizationProblem.clone();
            mocco.parameterPanel.removeAll();
            mocco.controlPanel.removeAll();
            mocco.controlPanel.validate();
            hasFinished = true;
        }
    };
    ActionListener reevaluate = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            mocco.state.currentProblem = (InterfaceOptimizationProblem) optimizationProblem.clone();
            mocco.state.currentProblem = optimizationProblem;
            mocco.state.makeFitnessCache(true);
            mocco.view.problemChanged(true);
        }
    };
}