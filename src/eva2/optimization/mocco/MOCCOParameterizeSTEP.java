package eva2.optimization.mocco;


import eva2.gui.PropertyDoubleArray;
import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.moso.MOSOWeightedFitness;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.optimization.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.optimization.problems.InterfaceOptimizationObjective;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.tools.AbstractObjectEditor;
import eva2.optimization.tools.GeneralOptimizationEditorProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 */
public class MOCCOParameterizeSTEP extends MOCCOPhase implements InterfaceProcessElement {

    private AbstractEAIndividual refSolution;
    private JTextField[] refSolTextField, relaxTextField, weightTextField;
    private JCheckBox[] satisfied;
    JPanel choicePanel;
    private InterfaceOptimizer optimizer;
    private GeneralOptimizationEditorProperty optimizationEditorProperty;

    public MOCCOParameterizeSTEP(MOCCOStandalone mocco) {
        this.mocco = mocco;
    }

    /**
     * This method will call the init method and will go to stall
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
        this.mocco.parameterPanel.add(this.makeHelpText("Please classify the achieved fitness values in" +
                " satisfying and unsatisfying results. For the satisfied objectives relaxation-level can be given" +
                " by which this objective is allowed to worsen. For the unsatisfied objectives weights can be given" +
                " for weigthed aggregation."), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        this.choicePanel = new JPanel();
        //this.choicePanel.setBorder(BorderFactory.createCompoundBorder(
        //    BorderFactory.createTitledBorder("Step Method:"),
        //	BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        tmpP.add(this.choicePanel, BorderLayout.CENTER);
        this.installChoice();
        this.mocco.parameterPanel.add(tmpP, BorderLayout.CENTER);
        this.mocco.parameterPanel.validate();
        this.mocco.controlPanel.validate();
    }

    private void installChoice() {
        this.choicePanel.setLayout(new GridBagLayout());
        JPanel panelSTEP = new JPanel();
        panelSTEP.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Parameterize Perferences:"),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        panelSTEP.setLayout(new GridBagLayout());
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) this.mocco.state.currentProblem).getProblemObjectives();
        this.refSolTextField = new JTextField[obj.length];
        this.relaxTextField = new JTextField[obj.length];
        this.weightTextField = new JTextField[obj.length];
        this.satisfied = new JCheckBox[obj.length];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2;
        panelSTEP.add(new JLabel("Objective:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panelSTEP.add(new JLabel(""), gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 2;
        panelSTEP.add(new JLabel("Value"), gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panelSTEP.add(new JLabel("Satis.?"), gbc);
        gbc.gridx = 4;
        gbc.gridy = 0;
        panelSTEP.add(new JLabel("Weight"), gbc);
        gbc.gridx = 5;
        gbc.gridy = 0;
        panelSTEP.add(new JLabel("Relax."), gbc);
        for (int i = 0; i < obj.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weightx = 2;
            panelSTEP.add(new JLabel("" + obj[i].getIdentName()), gbc);
            gbc.gridx = 1;
            gbc.gridy = i + 1;
            gbc.weightx = 1;
            if (obj[i].is2BMinimized()) {
                panelSTEP.add(new JLabel("min"), gbc);
            } else {
                panelSTEP.add(new JLabel("max"), gbc);
            }
            gbc.gridx = 2;
            gbc.gridy = i + 1;
            this.refSolTextField[i] = new JTextField("" + ((Double) refSolution.getData(obj[i].getIdentName())).doubleValue());
            this.refSolTextField[i].setEditable(false);
            panelSTEP.add(this.refSolTextField[i], gbc);
            gbc.gridx = 3;
            gbc.gridy = i + 1;
            this.satisfied[i] = new JCheckBox();
            this.satisfied[i].addActionListener(satisfiedChanged);
            panelSTEP.add(this.satisfied[i], gbc);
            gbc.gridx = 4;
            gbc.gridy = i + 1;
            this.weightTextField[i] = new JTextField("1.0");
            if (obj[i].getOptimizationMode().contains("Objective")) {
                this.weightTextField[i].setEditable(true);
            } else {
                this.weightTextField[i].setEditable(false);
            }
            //this.satisfied[i].addActionListener(valueChanged);
            panelSTEP.add(this.weightTextField[i], gbc);
            gbc.gridx = 5;
            gbc.gridy = i + 1;
            this.relaxTextField[i] = new JTextField("0.0");
            this.relaxTextField[i].setEditable(false);
            //this.satisfied[i].addActionListener(valueChanged);
            panelSTEP.add(this.relaxTextField[i], gbc);
        }
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 0;
        this.choicePanel.add(panelSTEP, gbc);

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

    ActionListener satisfiedChanged = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) mocco.state.currentProblem).getProblemObjectives();
            for (int i = 0; i < satisfied.length; i++) {
                if (obj[i].getOptimizationMode().contains("Objective")) {
                    if (satisfied[i].isSelected()) {
                        relaxTextField[i].setEditable(true);
                        weightTextField[i].setEditable(false);
                    } else {
                        relaxTextField[i].setEditable(false);
                        weightTextField[i].setEditable(true);
                    }
                } else {
                    relaxTextField[i].setEditable(false);
                    weightTextField[i].setEditable(false);
                }
            }
            choicePanel.validate();
        }
    };

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            // first fetch the data from the choice and set constraints
            InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) mocco.state.currentProblem).getProblemObjectives();

            double[] weights, relax;
            weights = new double[refSolTextField.length];
            relax = new double[refSolTextField.length];
            for (int i = 0; i < refSolTextField.length; i++) {
                weights[i] = new Double(weightTextField[i].getText()).doubleValue();
                relax[i] = new Double(relaxTextField[i].getText()).doubleValue();
                if ((satisfied[i].isSelected()) && (obj[i].getOptimizationMode().contains("Objective"))) {
                    weights[i] = 0;
                    if (obj[i].is2BMinimized()) {
                        // check this sounds wierd => sounds correct if objectives are used above stupid!
                        relax[i] = ((Double) refSolution.getData(obj[i].getIdentName())).doubleValue()
                                + Math.abs(relax[i]);
                    } else {
                        relax[i] = ((Double) refSolution.getData(obj[i].getIdentName())).doubleValue()
                                - Math.abs(relax[i]);
                    }
                    obj[i].SetConstraintGoal(relax[i]);
                    obj[i].SetOptimizationMode("Constraint");
                } else {
                    relax[i] = 0;
                }
            }
//            // now the parameters are prepared, I should add the new constraints
//            int dim = 0;
//            for (int i = 0; i < relax.length; i++) {
//                if (relax[i] > 0) {
//                    obj[i].SetConstraintGoal(relax[i]);
//                    obj[i].SetOptimizationMode("Constraint");
//                }
//                if ((weights[i] > 0) && (obj[i].getOptimizationMode().contains("Objective"))) dim++;
//            }
//            // and set a MOSOWeightedFitness converter
//            double[] setWeights = new double[dim];
//            dim = 0;
//            for (int i = 0; i < weights.length; i++) {
//                if (weights[i] > 0) {
//                    setWeights[dim] = weights[i];
//                    dim++;
//                }
//            }
            MOSOWeightedFitness wf = new MOSOWeightedFitness();
            ((AbstractMultiObjectiveOptimizationProblem) mocco.state.currentProblem).setMOSOConverter(wf);
            double[] setWeights = mapObjectives2Fitness(weights);
            PropertyDoubleArray da = new PropertyDoubleArray(setWeights);
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
                tmpA.add(new Double(d[i]));
            }
        }

        double[] result = new double[tmpA.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((Double) tmpA.get(i)).doubleValue();
        }
        return result;
    }
}