package eva2.server.go.mocco;


import javax.swing.*;

import eva2.gui.GenericObjectEditor;
import eva2.gui.PropertyDoubleArray;
import eva2.gui.PropertyEditorProvider;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.MOCCOStandalone;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.moso.MOSOWeightedFitness;
import eva2.server.go.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.server.go.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.server.go.problems.InterfaceOptimizationObjective;
import eva2.server.go.strategies.GeneticAlgorithm;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.tools.GeneralGOEProperty;

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
public class MOCCOParameterizeGDF extends MOCCOPhase implements InterfaceProcessElement {

    private AbstractEAIndividual    m_RefSolution;
    private JTextField[][]          m_TradeOff;
    JPanel                          m_Choice;
    private InterfaceOptimizer      m_Opt;
    private GeneralGOEProperty      m_EOpt;

    public MOCCOParameterizeGDF(MOCCOStandalone mocco) {
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
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();
        this.m_TradeOff       = new JTextField[obj.length][obj.length];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.BOTH;

        for (int i = 0; i < obj.length; i++) {
            gbc.gridx       = 0;
            gbc.gridy       = i;
            gbc.weightx     = 2;
            panelGDF.add(new JLabel(""+obj[i].getIdentName()), gbc);
            for (int j = 0; j < obj.length; j++) {
                this.m_TradeOff[i][j] = new JTextField("");
                if (i == j) {
                    this.m_TradeOff[i][j].setBackground(Color.WHITE);
                    if (obj[i].getOptimizationMode().contains("Objective")) this.m_TradeOff[i][j].setEditable(true);
                    else this.m_TradeOff[i][j].setEditable(false);
                    this.m_TradeOff[i][j].setText("1.0");
                    this.m_TradeOff[i][j].addActionListener(weightListener);
                } else {
                    this.m_TradeOff[i][j].setBackground(Color.LIGHT_GRAY);
                    this.m_TradeOff[i][j].setEditable(false);
                    this.m_TradeOff[i][j].setText("0.5");
                    //this.m_TradeOff[i][j].addActionListener(tradeoffListener);
                }
                gbc.gridx       = j+1;
                gbc.gridy       = i;
                gbc.weightx     = 1;
                panelGDF.add(this.m_TradeOff[i][j], gbc);
            }
        }
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.BOTH;
        gbc.gridx       = 0;
        gbc.gridwidth   = 2;
        gbc.gridy       = 0;
        this.m_Choice.add(panelGDF, gbc);

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

    ActionListener weightListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            double[]    w = new double [m_TradeOff.length];
            double      sum = 0;
            for (int i = 0; i < m_TradeOff.length; i++) {
                w[i] = new Double(m_TradeOff[i][i].getText()).doubleValue();
                sum += w[i];
            }
            if (new Double(sum).isNaN()) return;
            for (int i = 0; i < m_TradeOff.length; i++) {
                for (int j = 0; j < m_TradeOff.length; j++) {
                    if (i != j) {
                        m_TradeOff[i][j].setText(""+(w[i]/w[j]));
                    }
                }
            }
            m_Choice.validate();
        }
    };

    ActionListener continue2 = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            double[]    w = new double [m_TradeOff.length];
            for (int i = 0; i < m_TradeOff.length; i++) {
                w[i] = new Double(m_TradeOff[i][i].getText()).doubleValue();
            }
            MOSOWeightedFitness wf = new MOSOWeightedFitness();
            // I've to set this before I change the parameters, because the problem sets the
            // output dimension based on the AbstractMultiObjectiveOptimizationProblem and
            // that one has not the faintest idea about the true output dimension            
            ((AbstractMultiObjectiveOptimizationProblem)m_Mocco.m_State.m_CurrentProblem).setMOSOConverter(wf);
            w = mapObjectives2Fitness(w);
            PropertyDoubleArray da = new PropertyDoubleArray(w);
            wf.setOutputDimension(da.getDoubleArray().length);
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