package eva2.optimization.mocco;

import eva2.gui.JParaPanel;
import eva2.optimization.go.MOCCOStandalone;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.ReflectPackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 25.10.2005
 * Time: 09:49:20
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOProblemInitialization extends MOCCOPhase implements InterfaceProcessElement {

    private JComboBox problemChooser;

    public MOCCOProblemInitialization(MOCCOStandalone mocco) {
        this.mocco = mocco;
    }

    /**
     * This method will call the initialize method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.mocco.controlPanel.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to choose the initial number of solution alternatives.");
        tmpB.setToolTipText("This finializes the original problem definition.");
        tmpB.addActionListener(continue2);
        this.mocco.controlPanel.add(tmpB);

        // the parameter panel
        this.initProblemDefinition();

        this.mocco.getMainFrame().setVisible(true);
        this.mocco.getMainFrame().validate();
    }

    private void initProblemDefinition() {
        this.mocco.parameterPanel.removeAll();
        this.problemChooser = new JComboBox();
        JComponent tmpC = new JPanel();
        tmpC.setLayout(new BorderLayout());

        Class[] altern = null;
        try {
            altern = ReflectPackage.getAssignableClassesInPackage("eva2.problems", Class.forName("eva2.problems.InterfaceMultiObjectiveDeNovoProblem"), true, true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.problemChooser.setModel(new DefaultComboBoxModel(altern));

        String objectName = (this.mocco.state.originalProblem.getClass().getName());
        this.problemChooser.getModel().setSelectedItem(objectName);
        this.problemChooser.addActionListener(problemChanged);
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
        tmpP.add(this.problemChooser, gbc);
        this.mocco.parameterPanel.setLayout(new BorderLayout());
        this.mocco.parameterPanel.add(tmpP, BorderLayout.NORTH);
        JParaPanel paraPanel = new JParaPanel(this.mocco.state.originalProblem, "MyGUI");
        this.mocco.parameterPanel.add(paraPanel.makePanel(), BorderLayout.CENTER);
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            mocco.controlPanel.removeAll();
            mocco.controlPanel.validate();
            mocco.parameterPanel.removeAll();
            hasFinished = true;
        }
    };

    ActionListener problemChanged = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            String className = (String) problemChooser.getSelectedItem();
            mocco.parameterPanel.removeAll();
            Object n = null;
            try {
                n = (Object) Class.forName(className).newInstance();
            } catch (Exception ex) {
            }
            mocco.state.originalProblem = (InterfaceOptimizationProblem) n;
            initProblemDefinition();
            mocco.parameterPanel.validate();
        }
    };
}