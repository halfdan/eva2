package eva2.optimization.mocco;

import eva2.gui.JParaPanel;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.ReflectPackage;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 25.10.2005
 * Time: 09:49:20
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOProblemInitialization extends MOCCOPhase implements InterfaceProcessElement {

    private JComboBox m_ProblemChooser;

    public MOCCOProblemInitialization(MOCCOStandalone mocco) {
        this.m_Mocco = mocco;
    }

    /**
     * This method will call the init method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.m_Mocco.m_JPanelControl.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to choose the initial number of solution alternatives.");
        tmpB.setToolTipText("This finializes the original problem definition.");
        tmpB.addActionListener(continue2);
        this.m_Mocco.m_JPanelControl.add(tmpB);

        // the parameter panel
        this.initProblemDefinition();

        this.m_Mocco.m_JFrame.setVisible(true);
        this.m_Mocco.m_JFrame.validate();
    }

    private void initProblemDefinition() {
        this.m_Mocco.m_JPanelParameters.removeAll();
        this.m_ProblemChooser = new JComboBox();
        JComponent tmpC = new JPanel();
        tmpC.setLayout(new BorderLayout());

        Class[] altern = null;
        try {
            altern = ReflectPackage.getAssignableClassesInPackage("eva2.optimization.problems", Class.forName("eva2.optimization.problems.InterfaceMultiObjectiveDeNovoProblem"), true, true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.m_ProblemChooser.setModel(new DefaultComboBoxModel(altern));

        String objectName = (this.m_Mocco.m_State.m_OriginalProblem.getClass().getName());
        this.m_ProblemChooser.getModel().setSelectedItem(objectName);
        this.m_ProblemChooser.addActionListener(problemChanged);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        tmpP.add(this.makeHelpText("Choose and parameterize the optimization problem to solve by means of MOCCO. " +
                "Please note that it is not necessary to include MOSO converters yet and that only problems complying " +
                "with the InterfaceMultiObjectiveDeNovoProblem can be optimized using the MOCCO approach."), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        tmpP.add(this.m_ProblemChooser, gbc);
        this.m_Mocco.m_JPanelParameters.setLayout(new BorderLayout());
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.NORTH);
        JParaPanel paraPanel = new JParaPanel(this.m_Mocco.m_State.m_OriginalProblem, "MyGUI");
        this.m_Mocco.m_JPanelParameters.add(paraPanel.makePanel(), BorderLayout.CENTER);
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelControl.validate();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Finished = true;
        }
    };

    ActionListener problemChanged = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            String className = (String) m_ProblemChooser.getSelectedItem();
            m_Mocco.m_JPanelParameters.removeAll();
            Object n = null;
            try {
                n = (Object) Class.forName(className).newInstance();
            } catch (Exception ex) {
            }
            m_Mocco.m_State.m_OriginalProblem = (InterfaceOptimizationProblem) n;
            initProblemDefinition();
            m_Mocco.m_JPanelParameters.validate();
        }
    };
}