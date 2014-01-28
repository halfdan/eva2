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
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2005
 * Time: 18:42:04
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOParameterizeGDF extends MOCCOPhase implements InterfaceProcessElement {

    private AbstractEAIndividual m_RefSolution;
    private JTextField[][] m_TradeOff;
    JPanel m_Choice;
    private InterfaceOptimizer m_Opt;
    private GeneralOptimizationEditorProperty m_EOpt;

    public MOCCOParameterizeGDF(MOCCOStandalone mocco) {
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
        this.m_Mocco.m_JPanelParameters.add(this.makeHelpText("Please give weights (white) for the individual objectives," +
                " or choose trade-off (gray) between individual objectives. Please note that these values are interdependent.")
                , BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        this.m_Choice = new JPanel();
        //this.m_Choice.setBorder(BorderFactory.createCompoundBorder(
        //    BorderFactory.createTitledBorder("Geoffrion-Dyer-Feinberg Method:"),
        //	BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        tmpP.add(this.m_Choice, BorderLayout.CENTER);
        this.installChoice();
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.CENTER);
        this.m_Mocco.m_JPanelParameters.validate();
        this.m_Mocco.m_JPanelControl.validate();
    }

    private void installChoice() {
        this.m_Choice.setLayout(new GridBagLayout());
        JPanel panelGDF = new JPanel();
        panelGDF.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Parameterize Perferences:"),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        panelGDF.setLayout(new GridBagLayout());
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) this.m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();
        this.m_TradeOff = new JTextField[obj.length][obj.length];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        for (int i = 0; i < obj.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 2;
            panelGDF.add(new JLabel("" + obj[i].getIdentName()), gbc);
            for (int j = 0; j < obj.length; j++) {
                this.m_TradeOff[i][j] = new JTextField("");
                if (i == j) {
                    this.m_TradeOff[i][j].setBackground(Color.WHITE);
                    if (obj[i].getOptimizationMode().contains("Objective")) {
                        this.m_TradeOff[i][j].setEditable(true);
                    } else {
                        this.m_TradeOff[i][j].setEditable(false);
                    }
                    this.m_TradeOff[i][j].setText("1.0");
                    this.m_TradeOff[i][j].addActionListener(weightListener);
                } else {
                    this.m_TradeOff[i][j].setBackground(Color.LIGHT_GRAY);
                    this.m_TradeOff[i][j].setEditable(false);
                    this.m_TradeOff[i][j].setText("0.5");
                    //this.m_TradeOff[i][j].addActionListener(tradeoffListener);
                }
                gbc.gridx = j + 1;
                gbc.gridy = i;
                gbc.weightx = 1;
                panelGDF.add(this.m_TradeOff[i][j], gbc);
            }
        }
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 0;
        this.m_Choice.add(panelGDF, gbc);

        // the optimizer
        gbc.gridwidth = 1;
        this.m_EOpt = new GeneralOptimizationEditorProperty();
        this.m_Opt = new GeneticAlgorithm();
        this.m_Opt.setProblem(this.m_Mocco.m_State.m_CurrentProblem);
        this.m_Mocco.m_State.m_Optimizer = this.m_Opt;
        this.m_EOpt.name = "Island Model EA";
        try {
            this.m_EOpt.value = this.m_Opt;
            this.m_EOpt.editor = PropertyEditorProvider.findEditor(this.m_EOpt.value.getClass());
            if (this.m_EOpt.editor == null) {
                this.m_EOpt.editor = PropertyEditorProvider.findEditor(InterfaceOptimizer.class);
            }
            if (this.m_EOpt.editor instanceof GenericObjectEditor) {
                ((GenericObjectEditor) this.m_EOpt.editor).setClassType(InterfaceOptimizer.class);
            }
            this.m_EOpt.editor.setValue(this.m_EOpt.value);
            AbstractObjectEditor.findViewFor(this.m_EOpt);
            if (this.m_EOpt.view != null) {
                this.m_EOpt.view.repaint();
            }
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 2;
        this.m_Choice.add(new JLabel("" + this.m_EOpt.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        this.m_Choice.add(this.m_EOpt.view, gbc);
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
        this.m_Choice.add(new JLabel("" + editor.name), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        this.m_Choice.add(editor.view, gbc);

    }

    /**
     * Method method allows mocco to set the previously selected
     * reference solution
     *
     * @param indy the reference solution
     */
    public void setReferenceSolution(AbstractEAIndividual indy) {
        this.m_RefSolution = indy;
    }

    ActionListener weightListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            double[] w = new double[m_TradeOff.length];
            double sum = 0;
            for (int i = 0; i < m_TradeOff.length; i++) {
                w[i] = new Double(m_TradeOff[i][i].getText()).doubleValue();
                sum += w[i];
            }
            if (new Double(sum).isNaN()) {
                return;
            }
            for (int i = 0; i < m_TradeOff.length; i++) {
                for (int j = 0; j < m_TradeOff.length; j++) {
                    if (i != j) {
                        m_TradeOff[i][j].setText("" + (w[i] / w[j]));
                    }
                }
            }
            m_Choice.validate();
        }
    };

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            double[] w = new double[m_TradeOff.length];
            for (int i = 0; i < m_TradeOff.length; i++) {
                w[i] = new Double(m_TradeOff[i][i].getText()).doubleValue();
            }
            MOSOWeightedFitness wf = new MOSOWeightedFitness();
            // I've to set this before I change the parameters, because the problem sets the
            // output dimension based on the AbstractMultiObjectiveOptimizationProblem and
            // that one has not the faintest idea about the true output dimension            
            ((AbstractMultiObjectiveOptimizationProblem) m_Mocco.m_State.m_CurrentProblem).setMOSOConverter(wf);
            w = mapObjectives2Fitness(w);
            PropertyDoubleArray da = new PropertyDoubleArray(w);
            wf.setOutputDimension(da.getNumRows());
            wf.setWeights(da);
            m_Opt.setProblem(m_Mocco.m_State.m_CurrentProblem);
            m_Mocco.m_State.m_Optimizer = m_Opt;
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelControl.validate();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Mocco.m_JPanelParameters.validate();
            m_Finished = true;
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
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) this.m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();
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