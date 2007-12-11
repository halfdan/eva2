package javaeva.server.go.mocco;

import javaeva.server.go.MOCCOStandalone;
import javaeva.server.go.problems.InterfaceMultiObjectiveDeNovoProblem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.GeneralGOEProperty;
import javaeva.tools.ReflectPackage;
import javaeva.gui.JParaPanel;
import javaeva.gui.GenericObjectEditor;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.beans.PropertyEditorManager;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 25.10.2005
 * Time: 09:49:20
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOProblemInitialization extends MOCCOPhase implements InterfaceProcessElement {

    private JComboBox   m_ProblemChooser;

    public MOCCOProblemInitialization(MOCCOStandalone mocco) {
        this.m_Mocco = mocco;
    }

    /** This method will call the init method and will go to stall
     */
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
        this.m_ProblemChooser   = new JComboBox();
        JComponent  tmpC        = new JPanel();
        tmpC.setLayout(new BorderLayout());
        
//        this.m_ProblemChooser.setModel(new DefaultComboBoxModel(this.getClassAlternatives4("javaeva.server.oa.go.OptimizationProblems.InterfaceMultiObjectiveDeNovoProblem")));
        Class[] altern = null;
		try {
			altern = ReflectPackage.getAssignableClassesInPackage("javaeva.server.oa.go.OptimizationProblems", Class.forName("javaeva.server.oa.go.OptimizationProblems.InterfaceMultiObjectiveDeNovoProblem"), true, true);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.m_ProblemChooser.setModel(new DefaultComboBoxModel(altern));
        		
        String objectName = (this.m_Mocco.m_State.m_OriginalProblem.getClass().getName());
        this.m_ProblemChooser.getModel().setSelectedItem(objectName);
        this.m_ProblemChooser.addActionListener(problemChanged);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill        = GridBagConstraints.HORIZONTAL;
        gbc.gridx       = 0;
        gbc.gridy       = 0;
        tmpP.add(this.makeHelpText("Choose and parameterize the optimization problem to solve by means of MOCCO. " +
                "Please note that it is not necessary to include MOSO converters yet and that only problems complying " +
                "with the InterfaceMultiObjectiveDeNovoProblem can be optimized using the MOCCO approach."),gbc);
        gbc.gridx       = 0;
        gbc.gridy       = 1;
        tmpP.add(this.m_ProblemChooser, gbc);
        this.m_Mocco.m_JPanelParameters.setLayout(new BorderLayout());
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.NORTH);
        JParaPanel paraPanel = new JParaPanel(this.m_Mocco.m_State.m_OriginalProblem, "MyGUI");
        this.m_Mocco.m_JPanelParameters.add(paraPanel.installActions(), BorderLayout.CENTER);
    }

    ActionListener continue2 = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelControl.validate();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Finished = true;
        }
    };

    ActionListener problemChanged = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            String className = (String)m_ProblemChooser.getSelectedItem();
            m_Mocco.m_JPanelParameters.removeAll();
            Object n = null;
            try {
                n = (Object)Class.forName(className).newInstance();
            } catch (Exception ex) {
            }
            m_Mocco.m_State.m_OriginalProblem = (InterfaceOptimizationProblem) n;
            initProblemDefinition();
            m_Mocco.m_JPanelParameters.validate();
        }
    };
}