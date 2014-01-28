package eva2.optimization.mocco;


import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.operator.migration.SOBestMigration;
import eva2.optimization.operator.moso.MOSOWeightedLPTchebycheff;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.optimization.strategies.IslandModelEA;
import eva2.optimization.tools.AbstractObjectEditor;
import eva2.optimization.tools.GeneralOptimizationEditorProperty;
import eva2.tools.math.RNG;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2005
 * Time: 18:42:04
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOParameterizeTchebycheff extends MOCCOPhase implements InterfaceProcessElement {

    private double[] m_RefPoint;
    private IslandModelEA m_Island;
    private GeneralOptimizationEditorProperty m_EIMEA;
    private int m_Perturbations = 4;
    private JTextField m_NumPer;
    JPanel m_Parameters;
    private JTextField[] m_UpperLimit, m_LowerLimit;

    public MOCCOParameterizeTchebycheff(MOCCOStandalone mocco) {
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
        this.m_Mocco.m_JPanelParameters.removeAll();
        this.m_Mocco.m_JPanelParameters.setLayout(new BorderLayout());
        this.m_Mocco.m_JPanelParameters.add(this.makeHelpText("Please parameterized the Tchebycheff method." +
                " Typically this methods generates a set of solutions by using k perturbations of the weights used for the weighted Tchebycheff metric." +
                " But also the choice of the optimization algorithms and migration rate for the heterogeneuos island model EA is critical." +
                " Please note that any server server settings will override k!"), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        this.m_Parameters = new JPanel();
//        this.m_Choice.setBorder(BorderFactory.createCompoundBorder(
//		    BorderFactory.createTitledBorder("Step Method:"),
//			BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        tmpP.add(this.m_Parameters, BorderLayout.CENTER);
        this.installChoice();
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.CENTER);
        this.m_Mocco.m_JPanelParameters.validate();
        this.m_Mocco.m_JPanelControl.validate();
    }

    private void installChoice() {
        this.m_Parameters.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2;
        this.m_Parameters.add(new JLabel("Choose number of Perturbations k:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        this.m_NumPer = new JTextField("" + this.m_Perturbations);
        this.m_Parameters.add(this.m_NumPer, gbc);
        // weight constraints
        JComponent tmpP = this.makeLimits4Weigths();
        tmpP.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Choose Weight Bounds (0,1)"),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 1;
        gbc.weightx = 2;
        this.m_Parameters.add(tmpP, gbc);
        // IslandModelEA
        this.m_EIMEA = new GeneralOptimizationEditorProperty();
        this.m_Island = new IslandModelEA();
        this.m_Island.setHeterogeneousProblems(true);
        this.m_Island.setLocalOnly(true);
        this.m_Island.setMigrationRate(2);
        this.m_Island.setMigrationStrategy(new SOBestMigration());
        this.m_Island.setNumberLocalCPUs(this.m_Perturbations);
        this.m_Island.setProblem(this.m_Mocco.m_State.m_CurrentProblem);
        this.m_Mocco.m_State.m_Optimizer = this.m_Island;
        this.m_EIMEA.name = "Island Model EA";
        try {
            this.m_EIMEA.value = this.m_Island;
            this.m_EIMEA.editor = PropertyEditorProvider.findEditor(this.m_EIMEA.value.getClass());
            if (this.m_EIMEA.editor == null) {
                this.m_EIMEA.editor = PropertyEditorProvider.findEditor(IslandModelEA.class);
            }
            if (this.m_EIMEA.editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) this.m_EIMEA.editor).setClassType(IslandModelEA.class);
            }
            this.m_EIMEA.editor.setValue(this.m_EIMEA.value);
            AbstractObjectEditor.findViewFor(this.m_EIMEA);
            if (this.m_EIMEA.view != null) {
                this.m_EIMEA.view.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 2;
        this.m_Parameters.add(new JLabel("" + this.m_EIMEA.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        this.m_Parameters.add(this.m_EIMEA.view, gbc);
        // Terminator
        GeneralOptimizationEditorProperty editor = new GeneralOptimizationEditorProperty();
        editor.name = "Terminator";
        try {
            editor.value = this.m_Mocco.m_State.m_Terminator;
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
        gbc.gridy = 3;
        gbc.weightx = 2;
        this.m_Parameters.add(new JLabel("" + editor.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        this.m_Parameters.add(editor.view, gbc);
    }

    private JComponent makeLimits4Weigths() {
        JPanel result = new JPanel();
        result.setLayout(new GridBagLayout());
        this.m_UpperLimit = new JTextField[this.m_RefPoint.length];
        this.m_LowerLimit = new JTextField[this.m_RefPoint.length];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        result.add(new JLabel("Objective"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 2;
        result.add(new JLabel("Ideal Value"), gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1;
        result.add(new JLabel("Lower"), gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        result.add(new JLabel("Upper"), gbc);
        for (int i = 0; i < this.m_RefPoint.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weightx = 1;
            result.add(new JLabel("Fitness " + i), gbc);
            gbc.gridx = 1;
            gbc.gridy = i + 1;
            gbc.weightx = 2;
            result.add(new JLabel("" + this.m_RefPoint[i]), gbc);
            gbc.gridx = 2;
            gbc.gridy = i + 1;
            gbc.weightx = 1;
            this.m_LowerLimit[i] = new JTextField("0.0");
            result.add(this.m_LowerLimit[i], gbc);
            gbc.gridx = 3;
            gbc.gridy = i + 1;
            this.m_UpperLimit[i] = new JTextField("1.0");
            result.add(this.m_UpperLimit[i], gbc);

        }
        return result;
    }

    /**
     * Method method allows mocco to set the previously selected
     * reference point
     *
     * @param point the reference point
     */
    public void setReferencePoint(double[] point) {
        this.m_RefPoint = point;
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            // first read the values
            try {
                m_Perturbations = new Integer(m_NumPer.getText()).intValue();
            } catch (NumberFormatException e) {
                System.out.println("Can't read k.");
            }
            if (m_EIMEA.value instanceof IslandModelEA) {
                m_Island = (IslandModelEA) m_EIMEA.value;
            } else {
                System.out.println("The selected optimizer does not allow heterogenuous multi-starts!");
            }
            // then set the values
//            if (!m_Island.getLocalOnly()) {
            // ToDo: Think of new ways to do this!
/*                PropertyRemoteServers servs = m_Island.getServers();
                String[] servers = servs.getServerNodes();
                if (servers.length != m_Perturbations) {
                    System.out.println("Warning: Number of servers overrides number of perturbations!");
                    m_Perturbations = servers.length;
                }*/
//            } else {
//                m_Island.setNumberLocalCPUs(m_Perturbations);
//            }
            m_Mocco.m_State.m_Optimizer = m_Island;
            m_Mocco.m_State.m_Optimizer.setProblem(m_Mocco.m_State.m_CurrentProblem);
            m_Island.init();
            double[] tmpD;
            double sum = 0, l = 0, u = 1;
            MOSOWeightedLPTchebycheff[] tmpLPs = new MOSOWeightedLPTchebycheff[m_Perturbations];
            for (int i = 0; i < m_Perturbations; i++) {
                tmpD = new double[m_RefPoint.length];
                sum = 0;
                for (int j = 0; j < tmpD.length; j++) {
                    try {
                        l = new Double(m_LowerLimit[j].getText()).doubleValue();
                    } catch (NumberFormatException e) {
                    }
                    try {
                        u = new Double(m_UpperLimit[j].getText()).doubleValue();
                    } catch (NumberFormatException e) {
                    }
                    if (l < 0) {
                        l = 0;
                    }
                    if (u > 1) {
                        u = 1;
                    }
                    if (u < 0) {
                        u = 0;
                    }
                    if (l > 1) {
                        l = 1;
                    }
                    if (u < l) {
                        double t = u;
                        u = l;
                        l = t;
                    }

                    if (i > 0) {
                        tmpD[j] = RNG.randomDouble(l, u);
                    } else {
                        tmpD[j] = 1;
                    }
                    sum += tmpD[j];
                }
                for (int j = 0; j < tmpD.length; j++) {
                    tmpD[j] /= sum;
                }
                tmpLPs[i] = new MOSOWeightedLPTchebycheff();
                // I've to set this before I change the parameters, because the problem sets the
                // output dimension based on the AbstractMultiObjectiveOptimizationProblem and
                // that one has not the faintest idea about the true output dimension
                ((AbstractMultiObjectiveOptimizationProblem) m_Island.getOptimizers()[i].getProblem()).setMOSOConverter(tmpLPs[i]);
                tmpLPs[i].setOutputDimension(tmpD.length);
                tmpLPs[i].getIdealPWeights().m_IdealValue = m_RefPoint;
                tmpLPs[i].getIdealPWeights().m_Weights = tmpD;
            }
            m_Mocco.m_View.removeReferencePoint();
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelControl.validate();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Mocco.m_JPanelParameters.validate();
            m_Finished = true;
        }
    };
}