package eva2.optimization.mocco;


import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.operator.migration.SOBestMigration;
import eva2.optimization.operator.moso.MOSOLpMetric;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
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

    private double[] refPoint;
    private MOSOLpMetric lpMetric;
    private IslandModelEA islandModelEA;
    private GeneralOptimizationEditorProperty optimizationEditorProperty, optimizationEditorProperty1;
    private int perturbations = 4;
    private double perturbation = 0.01;
    private JTextField numPerTextField, sizePerTextField;
    JPanel parameterPanel;

    public MOCCOParameterizeRefPoint(MOCCOStandalone mocco) {
        this.mocco = mocco;
    }

    /**
     * This method will call the initialize method and will go to stall
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
        this.mocco.parameterPanel.removeAll();
        this.mocco.parameterPanel.setLayout(new BorderLayout());
        this.mocco.parameterPanel.add(this.makeHelpText("Please parameterized the reference point method." +
                " Typically this methods generates a set of solutions by using k perturbations of the reference point given." +
                " Also the choice of the optimization algorithms and migration rate for the heterogeneuos island model EA is critical." +
                " Please note that the server settings will override k!"), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        this.parameterPanel = new JPanel();
//        this.choicePanel.setBorder(BorderFactory.createCompoundBorder(
//		    BorderFactory.createTitledBorder("Step Method:"),
//			BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        tmpP.add(this.parameterPanel, BorderLayout.CENTER);
        this.installChoice();
        this.mocco.parameterPanel.add(tmpP, BorderLayout.CENTER);
        this.mocco.parameterPanel.validate();
        this.mocco.controlPanel.validate();
    }

    private void installChoice() {
        this.parameterPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2;
        this.parameterPanel.add(new JLabel("Choose number of Perturbations k:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        this.numPerTextField = new JTextField("" + this.perturbations);
        this.parameterPanel.add(this.numPerTextField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 2;
        this.parameterPanel.add(new JLabel("Choose amount of Perturbation:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        this.sizePerTextField = new JTextField("" + this.perturbation);
        this.parameterPanel.add(this.sizePerTextField, gbc);
        // lpmetric
        this.optimizationEditorProperty = new GeneralOptimizationEditorProperty();
        this.lpMetric = new MOSOLpMetric();
        this.lpMetric.getReference().setDoubleArray(this.refPoint);
        this.optimizationEditorProperty.name = "Lp-Metric";
        try {
            this.optimizationEditorProperty.value = this.lpMetric;
            this.optimizationEditorProperty.editor = PropertyEditorProvider.findEditor(this.optimizationEditorProperty.value.getClass());
            if (this.optimizationEditorProperty.editor == null) {
                this.optimizationEditorProperty.editor = PropertyEditorProvider.findEditor(MOSOLpMetric.class);
            }
            if (this.optimizationEditorProperty.editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) this.optimizationEditorProperty.editor).setClassType(MOSOLpMetric.class);
            }
            this.optimizationEditorProperty.editor.setValue(this.optimizationEditorProperty.value);
            AbstractObjectEditor.findViewFor(this.optimizationEditorProperty);
            if (this.optimizationEditorProperty.view != null) {
                this.optimizationEditorProperty.view.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 2;
        this.parameterPanel.add(new JLabel("" + this.optimizationEditorProperty.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        this.parameterPanel.add(this.optimizationEditorProperty.view, gbc);
        // IslandModelEA
        this.optimizationEditorProperty1 = new GeneralOptimizationEditorProperty();
        this.islandModelEA = new IslandModelEA();
        this.islandModelEA.setHeterogeneousProblems(true);
        this.islandModelEA.setLocalOnly(true);
        this.islandModelEA.setMigrationRate(2);
        this.islandModelEA.setMigrationStrategy(new SOBestMigration());
        this.islandModelEA.setNumberLocalCPUs(this.perturbations);
        this.islandModelEA.setProblem(this.mocco.state.currentProblem);
        this.mocco.state.optimizer = this.islandModelEA;
        this.optimizationEditorProperty1.name = "Island Model EA";
        try {
            this.optimizationEditorProperty1.value = this.islandModelEA;
            this.optimizationEditorProperty1.editor = PropertyEditorProvider.findEditor(this.optimizationEditorProperty1.value.getClass());
            if (this.optimizationEditorProperty1.editor == null) {
                this.optimizationEditorProperty1.editor = PropertyEditorProvider.findEditor(IslandModelEA.class);
            }
            if (this.optimizationEditorProperty1.editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) this.optimizationEditorProperty1.editor).setClassType(IslandModelEA.class);
            }
            this.optimizationEditorProperty1.editor.setValue(this.optimizationEditorProperty1.value);
            AbstractObjectEditor.findViewFor(this.optimizationEditorProperty1);
            if (this.optimizationEditorProperty1.view != null) {
                this.optimizationEditorProperty1.view.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 2;
        this.parameterPanel.add(new JLabel("" + this.optimizationEditorProperty1.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        this.parameterPanel.add(this.optimizationEditorProperty1.view, gbc);
        // Terminator
        GeneralOptimizationEditorProperty editor = new GeneralOptimizationEditorProperty();
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
        gbc.gridy = 4;
        gbc.weightx = 2;
        this.parameterPanel.add(new JLabel("" + editor.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1;
        this.parameterPanel.add(editor.view, gbc);
    }

    /**
     * Method method allows mocco to set the previously selected
     * reference point
     *
     * @param point the reference point
     */
    public void setReferencePoint(double[] point) {
        this.refPoint = point;
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
                perturbations = Integer.parseInt(numPerTextField.getText());
            } catch (java.lang.NumberFormatException e) {
                System.out.println("Can't read k.");
            }
            try {
                perturbation = Double.parseDouble(sizePerTextField.getText());
            } catch (java.lang.NumberFormatException e) {
                System.out.println("Can't read amount of perturbation.");
            }
            if (optimizationEditorProperty1.value instanceof IslandModelEA) {
                islandModelEA = (IslandModelEA) optimizationEditorProperty1.value;
            } else {
                System.out.println("The selected optimizer does not allow heterogenuous multi-starts!");
            }
            if (optimizationEditorProperty.value instanceof MOSOLpMetric) {
                lpMetric = (MOSOLpMetric) optimizationEditorProperty.value;
            } else {
                System.out.println("The selected MOSO conversion is not suited for the reference point approach!");
            }
            // then set the values
//            if (!islandModelEA.getLocalOnly()) {
            // ToDo: Think of new ways to do this!
                /*PropertyRemoteServers t = islandModelEA.getServers();
                String[] servers = t.getServerNodes();
                if (servers.length != perturbations) {
                    System.out.println("Warning: Number of servers overrides number of perturbations!");
                    perturbations = servers.length;
                }*/
//            } else {
//                islandModelEA.setNumberLocalCPUs(perturbations);
//            }
            mocco.state.optimizer = islandModelEA;
            mocco.state.optimizer.setProblem(mocco.state.currentProblem);
            islandModelEA.initialize();
            double[] tmpD;
            MOSOLpMetric[] tmpLPs = new MOSOLpMetric[perturbations];
            for (int i = 0; i < perturbations; i++) {
                tmpD = new double[refPoint.length];
                for (int j = 0; j < tmpD.length; j++) {
                    if (i > 0) {
                        tmpD[j] = refPoint[j] + RNG.gaussianDouble(perturbation);
                    } else {
                        tmpD[j] = refPoint[j];
                    }
                }
                tmpLPs[i] = (MOSOLpMetric) lpMetric.clone();
                // I've to set this before I change the parameters, because the problem sets the
                // output dimension based on the AbstractMultiObjectiveOptimizationProblem and
                // that one has not the faintest idea about the true output dimension                
                ((AbstractMultiObjectiveOptimizationProblem) islandModelEA.getOptimizers()[i].getProblem()).setMOSOConverter(tmpLPs[i]);
                tmpLPs[i].setOutputDimension(tmpD.length);
                tmpLPs[i].getReference().setDoubleArray(tmpD);
            }
            mocco.view.removeReferencePoint();
            mocco.controlPanel.removeAll();
            mocco.controlPanel.validate();
            mocco.parameterPanel.removeAll();
            mocco.parameterPanel.validate();
            hasFinished = true;
        }
    };
}