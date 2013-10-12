package eva2.optimization.mocco;


import eva2.gui.JParaPanel;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.problems.InterfaceOptimizationProblem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 25.10.2005
 * Time: 10:07:27
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOProblemRedefinition extends MOCCOPhase implements InterfaceProcessElement {

    private InterfaceOptimizationProblem m_Problem;

    public MOCCOProblemRedefinition(MOCCOStandalone mocco) {
        this.m_Mocco = mocco;
        this.m_Problem = (InterfaceOptimizationProblem) this.m_Mocco.m_State.m_CurrentProblem.clone();
    }

    /**
     * This method will call the init method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.m_Mocco.m_JPanelControl.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to choose the optimization strategy.");
        tmpB.setToolTipText("This finializes the problem redefinition process.");
        tmpB.addActionListener(continue2);
        this.m_Mocco.m_JPanelControl.add(tmpB);

        // the parameter panel
        this.m_Mocco.m_JPanelParameters.removeAll();
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
        this.m_Mocco.m_JPanelParameters.setLayout(new BorderLayout());
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.NORTH);

        JParaPanel paraPanel = new JParaPanel(this.m_Problem, "MyGUI");
        tmpC = (paraPanel.makePanel());
        this.m_Mocco.m_JPanelParameters.add(tmpC, BorderLayout.CENTER);

        this.m_Mocco.m_JFrame.setVisible(true);
        this.m_Mocco.m_JFrame.validate();
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            m_Mocco.m_State.m_CurrentProblem = (InterfaceOptimizationProblem) m_Problem.clone();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelControl.validate();
            m_Finished = true;
        }
    };
    ActionListener reevaluate = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            m_Mocco.m_State.m_CurrentProblem = (InterfaceOptimizationProblem) m_Problem.clone();
            m_Mocco.m_State.m_CurrentProblem = m_Problem;
            m_Mocco.m_State.makeFitnessCache(true);
            m_Mocco.m_View.problemChanged(true);
        }
    };
}