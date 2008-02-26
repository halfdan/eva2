package javaeva.server.go.mocco;

import javaeva.server.go.MOCCOStandalone;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.moso.MOSOWeightedFitness;
import javaeva.server.go.problems.AbstractMultiObjectiveOptimizationProblem;
import javaeva.server.go.problems.InterfaceMultiObjectiveDeNovoProblem;
import javaeva.server.go.problems.InterfaceOptimizationObjective;
import javaeva.server.go.strategies.GeneticAlgorithm;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.tools.GeneralGOEProperty;
import javaeva.gui.PropertyDoubleArray;
import javaeva.gui.GenericObjectEditor;
import javaeva.gui.PropertyEditorProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2005
 * Time: 18:42:04
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOParameterizeSTEP extends MOCCOPhase implements InterfaceProcessElement {

    private AbstractEAIndividual    m_RefSolution;
    private JTextField[]            m_RefSol, m_Relax, m_Weight;
    private JCheckBox[]             m_Satisfied;
    JPanel                          m_Choice;
    private InterfaceOptimizer      m_Opt;
    private GeneralGOEProperty      m_EOpt;

    public MOCCOParameterizeSTEP(MOCCOStandalone mocco) {
        this.m_Mocco = mocco;
    }

    /** This method will call the init method and will go to stall
     */
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
        this.m_Mocco.m_JPanelParameters.add(this.makeHelpText("Please classify the achieved fitness values in" +
                " satisfying and unsatisfying results. For the satisfied objectives relaxation-level can be given" +
                " by which this objective is allowed to worsen. For the unsatisfied objectives weights can be given" +
                " for weigthed aggregation."), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        this.m_Choice = new JPanel();
        //this.m_Choice.setBorder(BorderFactory.createCompoundBorder(
		//    BorderFactory.createTitledBorder("Step Method:"),
		//	BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        tmpP.add(this.m_Choice, BorderLayout.CENTER);
        this.installChoice();
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.CENTER);
        this.m_Mocco.m_JPanelParameters.validate();
        this.m_Mocco.m_JPanelControl.validate();
    }

    private void installChoice() {
        this.m_Choice.setLayout(new GridBagLayout());
        JPanel panelSTEP = new JPanel();
        panelSTEP.setBorder(BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Parameterize Perferences:"),
			BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        panelSTEP.setLayout(new GridBagLayout());
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();
        this.m_RefSol       = new JTextField[obj.length];
        this.m_Relax        = new JTextField[obj.length];
        this.m_Weight       = new JTextField[obj.length];
        this.m_Satisfied    = new JCheckBox[obj.length];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.BOTH;
        gbc.gridx       = 0;
        gbc.gridy       = 0;
        gbc.weightx     = 2;
        panelSTEP.add(new JLabel("Objective:"), gbc);
        gbc.gridx       = 1;
        gbc.gridy       = 0;
        gbc.weightx     = 1;
        panelSTEP.add(new JLabel(""), gbc);
        gbc.gridx       = 2;
        gbc.gridy       = 0;
        gbc.weightx     = 2;
        panelSTEP.add(new JLabel("Value"), gbc);
        gbc.gridx       = 3;
        gbc.gridy       = 0;
        gbc.weightx     = 1;
        panelSTEP.add(new JLabel("Satis.?"), gbc);
        gbc.gridx       = 4;
        gbc.gridy       = 0;
        panelSTEP.add(new JLabel("Weight"), gbc);
        gbc.gridx       = 5;
        gbc.gridy       = 0;
        panelSTEP.add(new JLabel("Relax."), gbc);
        for (int i = 0; i < obj.length; i++) {
            gbc.gridx       = 0;
            gbc.gridy       = i+1;
            gbc.weightx     = 2;
            panelSTEP.add(new JLabel(""+obj[i].getIdentName()), gbc);
            gbc.gridx       = 1;
            gbc.gridy       = i+1;
            gbc.weightx     = 1;
            if (obj[i].is2BMinimized()) panelSTEP.add(new JLabel("min"), gbc);
            else panelSTEP.add(new JLabel("max"), gbc);
            gbc.gridx       = 2;
            gbc.gridy       = i+1;
            this.m_RefSol[i] = new JTextField(""+((Double)m_RefSolution.getData(obj[i].getIdentName())).doubleValue());
            this.m_RefSol[i].setEditable(false);
            panelSTEP.add(this.m_RefSol[i], gbc);
            gbc.gridx       = 3;
            gbc.gridy       = i+1;
            this.m_Satisfied[i] = new JCheckBox();
            this.m_Satisfied[i].addActionListener(satisfiedChanged);
            panelSTEP.add(this.m_Satisfied[i], gbc);
            gbc.gridx       = 4;
            gbc.gridy       = i+1;
            this.m_Weight[i] = new JTextField("1.0");
            if (obj[i].getOptimizationMode().contains("Objective")) this.m_Weight[i].setEditable(true);
            else this.m_Weight[i].setEditable(false);
            //this.m_Satisfied[i].addActionListener(valueChanged);
            panelSTEP.add(this.m_Weight[i], gbc);
            gbc.gridx       = 5;
            gbc.gridy       = i+1;
            this.m_Relax[i] = new JTextField("0.0");
            this.m_Relax[i].setEditable(false);
            //this.m_Satisfied[i].addActionListener(valueChanged);
            panelSTEP.add(this.m_Relax[i], gbc);
        }
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.BOTH;
        gbc.gridx       = 0;
        gbc.gridwidth   = 2;
        gbc.gridy       = 0;
        this.m_Choice.add(panelSTEP, gbc);

        // the optimizer
        gbc.gridwidth   = 1;
        this.m_EOpt    = new GeneralGOEProperty();
        this.m_Opt   = new GeneticAlgorithm();
        this.m_Opt.SetProblem(this.m_Mocco.m_State.m_CurrentProblem);
        this.m_Mocco.m_State.m_Optimizer = this.m_Opt;
        this.m_EOpt.m_Name               = "Island Model EA";
        try {
            this.m_EOpt.m_Value      = this.m_Opt;
            this.m_EOpt.m_Editor     = PropertyEditorProvider.findEditor(this.m_EOpt.m_Value.getClass());
            if (this.m_EOpt.m_Editor == null) this.m_EOpt.m_Editor = PropertyEditorProvider.findEditor(InterfaceOptimizer.class);
            if (this.m_EOpt.m_Editor instanceof GenericObjectEditor)
                ((GenericObjectEditor) this.m_EOpt.m_Editor).setClassType(InterfaceOptimizer.class);
            this.m_EOpt.m_Editor.setValue(this.m_EOpt.m_Value);
            this.findViewFor(this.m_EOpt);
            if (this.m_EOpt.m_View != null) this.m_EOpt.m_View.repaint();
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx       = 0;
        gbc.gridy       = 2;
        gbc.weightx     = 2;
        this.m_Choice.add(new JLabel(""+this.m_EOpt.m_Name), gbc);
        gbc.gridx       = 1;
        gbc.gridy       = 2;
        gbc.weightx     = 1;
        this.m_Choice.add(this.m_EOpt.m_View, gbc);
        // Terminator
        GeneralGOEProperty editor   = new GeneralGOEProperty();
        editor.m_Name               = "Terminator";
        try {
            editor.m_Value      = this.m_Mocco.m_State.m_Terminator;
            editor.m_Editor     = PropertyEditorProvider.findEditor(editor.m_Value.getClass());
            if (editor.m_Editor == null) editor.m_Editor = PropertyEditorProvider.findEditor(InterfaceTerminator.class);
            if (editor.m_Editor instanceof GenericObjectEditor)
                ((GenericObjectEditor) editor.m_Editor).setClassType(InterfaceTerminator.class);
            editor.m_Editor.setValue(editor.m_Value);
            this.findViewFor(editor);
            if (editor.m_View != null) editor.m_View.repaint();
        } catch (Exception e) {
            System.out.println("Darn can't read the value...");
        }
        gbc.gridx       = 0;
        gbc.gridy       = 3;
        gbc.weightx     = 2;
        this.m_Choice.add(new JLabel(""+editor.m_Name), gbc);
        gbc.gridx       = 1;
        gbc.gridy       = 3;
        gbc.weightx     = 1;
        this.m_Choice.add(editor.m_View, gbc);
    }

    /** Method method allows mocco to set the previously selected
     * reference solution
     * @param indy  the reference solution
     */
    public void setReferenceSolution(AbstractEAIndividual indy) {
        this.m_RefSolution = indy;
    }

    ActionListener satisfiedChanged = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem)m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();
            for (int i = 0; i < m_Satisfied.length; i++) {
                if (obj[i].getOptimizationMode().contains("Objective")) {
                    if (m_Satisfied[i].isSelected()) {
                        m_Relax[i].setEditable(true);
                        m_Weight[i].setEditable(false);
                    } else {
                        m_Relax[i].setEditable(false);
                        m_Weight[i].setEditable(true);
                    }
                } else {
                    m_Relax[i].setEditable(false);
                    m_Weight[i].setEditable(false);
                 }
            }
            m_Choice.validate();
        }
    };

    ActionListener continue2 = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            // first fetch the data from the choice and set constraints
            InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem)m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();

            double[] weights, relax;
            weights = new double[m_RefSol.length];
            relax   = new double[m_RefSol.length];
            for (int i = 0; i < m_RefSol.length; i++) {
                weights[i] = new Double(m_Weight[i].getText()).doubleValue();
                relax[i] = new Double(m_Relax[i].getText()).doubleValue();
                if ((m_Satisfied[i].isSelected()) && (obj[i].getOptimizationMode().contains("Objective"))) {
                    weights[i] = 0;
                    if (obj[i].is2BMinimized()) {
                        // check this sounds wierd => sounds correct if objectives are used above stupid!
                        relax[i] = ((Double)m_RefSolution.getData(obj[i].getIdentName())).doubleValue()
                            + Math.abs(relax[i]);
                    } else {
                        relax[i] = ((Double)m_RefSolution.getData(obj[i].getIdentName())).doubleValue()
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
            ((AbstractMultiObjectiveOptimizationProblem)m_Mocco.m_State.m_CurrentProblem).setMOSOConverter(wf);
            double[] setWeights = mapObjectives2Fitness(weights);
            PropertyDoubleArray da = new PropertyDoubleArray(setWeights);
            wf.setWeights(da);
            m_Opt.SetProblem(m_Mocco.m_State.m_CurrentProblem);
            m_Mocco.m_State.m_Optimizer = m_Opt;
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelControl.validate();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Mocco.m_JPanelParameters.validate();
            m_Finished = true;
        }
    };

    /** This method maps a double[] vector of length of the objectives to
     * an double[] vector of length of the fitness values. This is necessary
     * since previous objectives could have turned into constraints.
     * @param d     The input vector
     * @return The potentially reduced output vector.
     */
    public double[] mapObjectives2Fitness(double[] d) {
        ArrayList tmpA = new ArrayList();
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();
        System.out.println("Calling mapObjectives2Fitness");
        System.out.println("obj.length = " + obj.length);
        System.out.println("d.length   = " + d.length);
        for (int i = 0; i < obj.length; i++) {
            if (obj[i].getOptimizationMode().contains("Objective")) tmpA.add(new Double(d[i]));
        }

        double[] result = new double[tmpA.size()];
        for (int i = 0; i < result.length; i++) result[i] = ((Double)tmpA.get(i)).doubleValue();
        return result;
    }
}