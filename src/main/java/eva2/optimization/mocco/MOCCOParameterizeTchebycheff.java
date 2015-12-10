package eva2.optimization.mocco;


import eva2.gui.MOCCOStandalone;
import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.operator.migration.SOBestMigration;
import eva2.optimization.operator.moso.MOSOWeightedLPTchebycheff;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.strategies.IslandModelEA;
import eva2.optimization.tools.AbstractObjectEditor;
import eva2.optimization.tools.GeneralOptimizationEditorProperty;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.tools.math.RNG;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public class MOCCOParameterizeTchebycheff extends MOCCOPhase implements InterfaceProcessElement {

    private double[] refPoint;
    private IslandModelEA islandModelEA;
    private GeneralOptimizationEditorProperty optimizationEditorProperty;
    private int perturbations = 4;
    private JTextField numPer;
    JPanel parameterPanel;
    private JTextField[] upperLimit, lowerLimit;

    public MOCCOParameterizeTchebycheff(MOCCOStandalone mocco) {
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
        this.mocco.parameterPanel.add(this.makeHelpText("Please parameterized the Tchebycheff method." +
                " Typically this methods generates a set of solutions by using k perturbations of the weights used for the weighted Tchebycheff metric." +
                " But also the choice of the optimization algorithms and migration rate for the heterogeneuos island model EA is critical." +
                " Please note that any server server settings will override k!"), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        this.parameterPanel = new JPanel();
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
        this.numPer = new JTextField("" + this.perturbations);
        this.parameterPanel.add(this.numPer, gbc);
        // weight constraints
        JComponent tmpP = this.makeLimits4Weigths();
        tmpP.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Choose Weight Bounds (0,1)"),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 1;
        gbc.weightx = 2;
        this.parameterPanel.add(tmpP, gbc);
        // IslandModelEA
        this.optimizationEditorProperty = new GeneralOptimizationEditorProperty();
        this.islandModelEA = new IslandModelEA();
        this.islandModelEA.setHeterogeneousProblems(true);
        this.islandModelEA.setLocalOnly(true);
        this.islandModelEA.setMigrationRate(2);
        this.islandModelEA.setMigrationStrategy(new SOBestMigration());
        this.islandModelEA.setNumberLocalCPUs(this.perturbations);
        this.islandModelEA.setProblem(this.mocco.state.currentProblem);
        this.mocco.state.optimizer = this.islandModelEA;
        this.optimizationEditorProperty.name = "Island Model EA";
        try {
            this.optimizationEditorProperty.value = this.islandModelEA;
            this.optimizationEditorProperty.editor = PropertyEditorProvider.findEditor(this.optimizationEditorProperty.value.getClass());
            if (this.optimizationEditorProperty.editor == null) {
                this.optimizationEditorProperty.editor = PropertyEditorProvider.findEditor(IslandModelEA.class);
            }
            if (this.optimizationEditorProperty.editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) this.optimizationEditorProperty.editor).setClassType(IslandModelEA.class);
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
        gbc.gridy = 3;
        gbc.weightx = 2;
        this.parameterPanel.add(new JLabel("" + editor.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        this.parameterPanel.add(editor.view, gbc);
    }

    private JComponent makeLimits4Weigths() {
        JPanel result = new JPanel();
        result.setLayout(new GridBagLayout());
        this.upperLimit = new JTextField[this.refPoint.length];
        this.lowerLimit = new JTextField[this.refPoint.length];
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
        for (int i = 0; i < this.refPoint.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weightx = 1;
            result.add(new JLabel("Fitness " + i), gbc);
            gbc.gridx = 1;
            gbc.gridy = i + 1;
            gbc.weightx = 2;
            result.add(new JLabel("" + this.refPoint[i]), gbc);
            gbc.gridx = 2;
            gbc.gridy = i + 1;
            gbc.weightx = 1;
            this.lowerLimit[i] = new JTextField("0.0");
            result.add(this.lowerLimit[i], gbc);
            gbc.gridx = 3;
            gbc.gridy = i + 1;
            this.upperLimit[i] = new JTextField("1.0");
            result.add(this.upperLimit[i], gbc);

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
        this.refPoint = point;
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            // first read the values
            try {
                perturbations = Integer.parseInt(numPer.getText());
            } catch (NumberFormatException e) {
                System.out.println("Can't read k.");
            }
            if (optimizationEditorProperty.value instanceof IslandModelEA) {
                islandModelEA = (IslandModelEA) optimizationEditorProperty.value;
            } else {
                System.out.println("The selected optimizer does not allow heterogenuous multi-starts!");
            }
            // then set the values
//            if (!islandModelEA.getLocalOnly()) {
            // ToDo: Think of new ways to do this!
/*                PropertyRemoteServers servs = islandModelEA.getServers();
                String[] servers = servs.getServerNodes();
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
            double sum = 0, l = 0, u = 1;
            MOSOWeightedLPTchebycheff[] tmpLPs = new MOSOWeightedLPTchebycheff[perturbations];
            for (int i = 0; i < perturbations; i++) {
                tmpD = new double[refPoint.length];
                sum = 0;
                for (int j = 0; j < tmpD.length; j++) {
                    try {
                        l = Double.parseDouble(lowerLimit[j].getText());
                    } catch (NumberFormatException e) {
                    }
                    try {
                        u = Double.parseDouble(upperLimit[j].getText());
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
                ((AbstractMultiObjectiveOptimizationProblem) islandModelEA.getOptimizers()[i].getProblem()).setMOSOConverter(tmpLPs[i]);
                tmpLPs[i].setOutputDimension(tmpD.length);
                tmpLPs[i].getIdealPWeights().idealValue = refPoint;
                tmpLPs[i].getIdealPWeights().weights = tmpD;
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