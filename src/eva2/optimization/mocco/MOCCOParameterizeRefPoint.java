package eva2.optimization.mocco;


import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.operator.migration.SOBestMigration;
import eva2.optimization.operator.moso.MOSOLpMetric;
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
public class MOCCOParameterizeRefPoint extends MOCCOPhase implements InterfaceProcessElement {

    private double[] m_RefPoint;
    private MOSOLpMetric m_LpMetric;
    private IslandModelEA m_Island;
    private GeneralOptimizationEditorProperty m_EMOSO, m_EIMEA;
    private int m_Perturbations = 4;
    private double m_Perturbation = 0.01;
    private JTextField m_NumPer, m_SizePer;
    JPanel m_Parameters;

    public MOCCOParameterizeRefPoint(MOCCOStandalone mocco) {
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
        this.m_Mocco.m_JPanelParameters.add(this.makeHelpText("Please parameterized the reference point method." +
                " Typically this methods generates a set of solutions by using k perturbations of the reference point given." +
                " Also the choice of the optimization algorithms and migration rate for the heterogeneuos island model EA is critical." +
                " Please note that the server settings will override k!"), BorderLayout.NORTH);
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
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 2;
        this.m_Parameters.add(new JLabel("Choose amount of Perturbation:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        this.m_SizePer = new JTextField("" + this.m_Perturbation);
        this.m_Parameters.add(this.m_SizePer, gbc);
        // lpmetric
        this.m_EMOSO = new GeneralOptimizationEditorProperty();
        this.m_LpMetric = new MOSOLpMetric();
        this.m_LpMetric.getReference().setDoubleArray(this.m_RefPoint);
        this.m_EMOSO.m_Name = "Lp-Metric";
        try {
            this.m_EMOSO.m_Value = this.m_LpMetric;
            this.m_EMOSO.m_Editor = PropertyEditorProvider.findEditor(this.m_EMOSO.m_Value.getClass());
            if (this.m_EMOSO.m_Editor == null) {
                this.m_EMOSO.m_Editor = PropertyEditorProvider.findEditor(MOSOLpMetric.class);
            }
            if (this.m_EMOSO.m_Editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) this.m_EMOSO.m_Editor).setClassType(MOSOLpMetric.class);
            }
            this.m_EMOSO.m_Editor.setValue(this.m_EMOSO.m_Value);
            AbstractObjectEditor.findViewFor(this.m_EMOSO);
            if (this.m_EMOSO.m_View != null) {
                this.m_EMOSO.m_View.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 2;
        this.m_Parameters.add(new JLabel("" + this.m_EMOSO.m_Name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        this.m_Parameters.add(this.m_EMOSO.m_View, gbc);
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
        this.m_EIMEA.m_Name = "Island Model EA";
        try {
            this.m_EIMEA.m_Value = this.m_Island;
            this.m_EIMEA.m_Editor = PropertyEditorProvider.findEditor(this.m_EIMEA.m_Value.getClass());
            if (this.m_EIMEA.m_Editor == null) {
                this.m_EIMEA.m_Editor = PropertyEditorProvider.findEditor(IslandModelEA.class);
            }
            if (this.m_EIMEA.m_Editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) this.m_EIMEA.m_Editor).setClassType(IslandModelEA.class);
            }
            this.m_EIMEA.m_Editor.setValue(this.m_EIMEA.m_Value);
            AbstractObjectEditor.findViewFor(this.m_EIMEA);
            if (this.m_EIMEA.m_View != null) {
                this.m_EIMEA.m_View.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 2;
        this.m_Parameters.add(new JLabel("" + this.m_EIMEA.m_Name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        this.m_Parameters.add(this.m_EIMEA.m_View, gbc);
        // Terminator
        GeneralOptimizationEditorProperty editor = new GeneralOptimizationEditorProperty();
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
        gbc.gridy = 4;
        gbc.weightx = 2;
        this.m_Parameters.add(new JLabel("" + editor.m_Name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1;
        this.m_Parameters.add(editor.m_View, gbc);
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

    ActionListener satisfiedChanged = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {

        }
    };

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            // first read the values
            try {
                m_Perturbations = new Integer(m_NumPer.getText()).intValue();
            } catch (java.lang.NumberFormatException e) {
                System.out.println("Can't read k.");
            }
            try {
                m_Perturbation = new Double(m_SizePer.getText()).doubleValue();
            } catch (java.lang.NumberFormatException e) {
                System.out.println("Can't read amount of perturbation.");
            }
            if (m_EIMEA.m_Value instanceof IslandModelEA) {
                m_Island = (IslandModelEA) m_EIMEA.m_Value;
            } else {
                System.out.println("The selected optimizer does not allow heterogenuous multi-starts!");
            }
            if (m_EMOSO.m_Value instanceof MOSOLpMetric) {
                m_LpMetric = (MOSOLpMetric) m_EMOSO.m_Value;
            } else {
                System.out.println("The selected MOSO conversion is not suited for the reference point approach!");
            }
            // then set the values
//            if (!m_Island.getLocalOnly()) {
            // ToDo: Think of new ways to do this!
                /*PropertyRemoteServers t = m_Island.getServers();
                String[] servers = t.getServerNodes();
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
            MOSOLpMetric[] tmpLPs = new MOSOLpMetric[m_Perturbations];
            for (int i = 0; i < m_Perturbations; i++) {
                tmpD = new double[m_RefPoint.length];
                for (int j = 0; j < tmpD.length; j++) {
                    if (i > 0) {
                        tmpD[j] = m_RefPoint[j] + RNG.gaussianDouble(m_Perturbation);
                    } else {
                        tmpD[j] = m_RefPoint[j];
                    }
                }
                tmpLPs[i] = (MOSOLpMetric) m_LpMetric.clone();
                // I've to set this before I change the parameters, because the problem sets the
                // output dimension based on the AbstractMultiObjectiveOptimizationProblem and
                // that one has not the faintest idea about the true output dimension                
                ((AbstractMultiObjectiveOptimizationProblem) m_Island.getOptimizers()[i].getProblem()).setMOSOConverter(tmpLPs[i]);
                tmpLPs[i].setOutputDimension(tmpD.length);
                tmpLPs[i].getReference().setDoubleArray(tmpD);
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