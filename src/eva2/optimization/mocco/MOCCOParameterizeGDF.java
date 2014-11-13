package eva2.optimization.mocco;


import eva2.gui.PropertyDoubleArray;
import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.moso.MOSOWeightedFitness;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.tools.AbstractObjectEditor;
import eva2.optimization.tools.GeneralOptimizationEditorProperty;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.problems.InterfaceOptimizationObjective;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 */
public class MOCCOParameterizeGDF extends MOCCOPhase implements InterfaceProcessElement {

    private AbstractEAIndividual refSolution;
    private JTextField[][] tradeOffTextFields;
    JPanel choicePanel;
    private InterfaceOptimizer optimizer;
    private GeneralOptimizationEditorProperty optimizationEditorProperty;

    public MOCCOParameterizeGDF(MOCCOStandalone mocco) {
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
        this.mocco.parameterPanel.add(this.makeHelpText("Please give weights (white) for the individual objectives," +
                " or choose trade-off (gray) between individual objectives. Please note that these values are interdependent.")
                , BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        this.choicePanel = new JPanel();
        //this.choicePanel.setBorder(BorderFactory.createCompoundBorder(
        //    BorderFactory.createTitledBorder("Geoffrion-Dyer-Feinberg Method:"),
        //	BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        tmpP.add(this.choicePanel, BorderLayout.CENTER);
        this.installChoice();
        this.mocco.parameterPanel.add(tmpP, BorderLayout.CENTER);
        this.mocco.parameterPanel.validate();
        this.mocco.controlPanel.validate();
    }

    private void installChoice() {
        this.choicePanel.setLayout(new GridBagLayout());
        JPanel panelGDF = new JPanel();
        panelGDF.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Parameterize Perferences:"),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        panelGDF.setLayout(new GridBagLayout());
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) this.mocco.state.currentProblem).getProblemObjectives();
        this.tradeOffTextFields = new JTextField[obj.length][obj.length];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        for (int i = 0; i < obj.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 2;
            panelGDF.add(new JLabel("" + obj[i].getIdentName()), gbc);
            for (int j = 0; j < obj.length; j++) {
                this.tradeOffTextFields[i][j] = new JTextField("");
                if (i == j) {
                    this.tradeOffTextFields[i][j].setBackground(Color.WHITE);
                    if (obj[i].getOptimizationMode().contains("Objective")) {
                        this.tradeOffTextFields[i][j].setEditable(true);
                    } else {
                        this.tradeOffTextFields[i][j].setEditable(false);
                    }
                    this.tradeOffTextFields[i][j].setText("1.0");
                    this.tradeOffTextFields[i][j].addActionListener(weightListener);
                } else {
                    this.tradeOffTextFields[i][j].setBackground(Color.LIGHT_GRAY);
                    this.tradeOffTextFields[i][j].setEditable(false);
                    this.tradeOffTextFields[i][j].setText("0.5");
                    //this.tradeOffTextFields[i][j].addActionListener(tradeoffListener);
                }
                gbc.gridx = j + 1;
                gbc.gridy = i;
                gbc.weightx = 1;
                panelGDF.add(this.tradeOffTextFields[i][j], gbc);
            }
        }
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 0;
        this.choicePanel.add(panelGDF, gbc);

        // the optimizer
        gbc.gridwidth = 1;
        this.optimizationEditorProperty = new GeneralOptimizationEditorProperty();
        this.optimizer = new GeneticAlgorithm();
        this.optimizer.setProblem(this.mocco.state.currentProblem);
        this.mocco.state.optimizer = this.optimizer;
        this.optimizationEditorProperty.name = "Island Model EA";
        try {
            this.optimizationEditorProperty.value = this.optimizer;
            this.optimizationEditorProperty.editor = PropertyEditorProvider.findEditor(this.optimizationEditorProperty.value.getClass());
            if (this.optimizationEditorProperty.editor == null) {
                this.optimizationEditorProperty.editor = PropertyEditorProvider.findEditor(InterfaceOptimizer.class);
            }
            if (this.optimizationEditorProperty.editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) this.optimizationEditorProperty.editor).setClassType(InterfaceOptimizer.class);
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
        this.choicePanel.add(new JLabel("" + this.optimizationEditorProperty.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        this.choicePanel.add(this.optimizationEditorProperty.view, gbc);
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
        this.choicePanel.add(new JLabel("" + editor.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        this.choicePanel.add(editor.view, gbc);

    }

    /**
     * Method method allows mocco to set the previously selected
     * reference solution
     *
     * @param indy the reference solution
     */
    public void setReferenceSolution(AbstractEAIndividual indy) {
        this.refSolution = indy;
    }

    ActionListener weightListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            double[] w = new double[tradeOffTextFields.length];
            double sum = 0;
            for (int i = 0; i < tradeOffTextFields.length; i++) {
                w[i] = Double.parseDouble(tradeOffTextFields[i][i].getText());
                sum += w[i];
            }
            if (new Double(sum).isNaN()) {
                return;
            }
            for (int i = 0; i < tradeOffTextFields.length; i++) {
                for (int j = 0; j < tradeOffTextFields.length; j++) {
                    if (i != j) {
                        tradeOffTextFields[i][j].setText("" + (w[i] / w[j]));
                    }
                }
            }
            choicePanel.validate();
        }
    };

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            double[] w = new double[tradeOffTextFields.length];
            for (int i = 0; i < tradeOffTextFields.length; i++) {
                w[i] = Double.parseDouble(tradeOffTextFields[i][i].getText());
            }
            MOSOWeightedFitness wf = new MOSOWeightedFitness();
            // I've to set this before I change the parameters, because the problem sets the
            // output dimension based on the AbstractMultiObjectiveOptimizationProblem and
            // that one has not the faintest idea about the true output dimension            
            ((AbstractMultiObjectiveOptimizationProblem) mocco.state.currentProblem).setMOSOConverter(wf);
            w = mapObjectives2Fitness(w);
            PropertyDoubleArray da = new PropertyDoubleArray(w);
            wf.setOutputDimension(da.getNumRows());
            wf.setWeights(da);
            optimizer.setProblem(mocco.state.currentProblem);
            mocco.state.optimizer = optimizer;
            mocco.controlPanel.removeAll();
            mocco.controlPanel.validate();
            mocco.parameterPanel.removeAll();
            mocco.parameterPanel.validate();
            hasFinished = true;
        }
    };

    /**
     * This method maps a double[] vector of length of the objectives to
     * an double[] vector of length of the fitness values. This is necessary
     * since previous objectives could have turned into constraints.
     *
     * @param d The input vector
     * @return The potentially reduced output vector.
     */
    public double[] mapObjectives2Fitness(double[] d) {
        ArrayList tmpA = new ArrayList();
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) this.mocco.state.currentProblem).getProblemObjectives();
        System.out.println("Calling mapObjectives2Fitness");
        System.out.println("obj.length = " + obj.length);
        System.out.println("d.length   = " + d.length);
        for (int i = 0; i < obj.length; i++) {
            if (obj[i].getOptimizationMode().contains("Objective")) {
                tmpA.add(d[i]);
            }
        }

        double[] result = new double[tmpA.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((Double) tmpA.get(i)).doubleValue();
        }
        return result;
    }
}