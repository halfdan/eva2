package eva2.optimization.mocco;


import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.mocco.paretofrontviewer.InterfaceRefSolutionListener;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.optimization.problems.InterfaceOptimizationObjective;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2005
 * Time: 18:42:52
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOChooseReferenceSolution extends MOCCOPhase implements InterfaceProcessElement, InterfaceRefSolutionListener {

    AbstractEAIndividual m_ReferenceSolution = null;
    JPanel               m_Selected;

    public MOCCOChooseReferenceSolution(MOCCOStandalone mocco) {
        this.m_Mocco = mocco;
    }

    /** This method will call the init method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.m_Mocco.m_JPanelControl.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to strategy parameterization.");
        tmpB.addActionListener(continue2);
        this.m_Mocco.m_JPanelControl.add(tmpB);
        // the parameter panel
        this.m_Mocco.m_JPanelParameters.removeAll();
        this.m_Mocco.m_JPanelParameters.setLayout(new BorderLayout());
        this.m_Mocco.m_JPanelParameters.add(this.makeHelpText("Please choose a reference solution " +
                "from the Pareto-optimal solutions (grey circles) given to the right."), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new BorderLayout());
        tmpP.add(new JLabel("Currently selected solution:"), BorderLayout.NORTH);
        this.m_Selected = new JPanel();
        tmpP.add(this.m_Selected, BorderLayout.CENTER);
        this.updateSelected();
        this.m_Mocco.m_View.setRefSolutionSelectable(true);
        this.m_Mocco.m_View.setUniquelySelectable(true);
        this.m_Mocco.m_View.addRefSolutionSelectionListener(this);
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.CENTER);
        this.m_Mocco.m_JPanelParameters.validate();
        this.m_Mocco.m_JPanelControl.validate();
    }

    private void updateSelected() {
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Mocco.m_State.m_CurrentProblem).getProblemObjectives();
        this.m_Selected.removeAll();
        this.m_Selected.setLayout(new BorderLayout());
        if (this.m_ReferenceSolution == null) {
            this.m_Selected.add(new JLabel("Selected Individual: none"), BorderLayout.NORTH);
        } else {
            this.m_Selected.add(new JLabel("Selected Individual: "+this.m_ReferenceSolution), BorderLayout.NORTH);
            //double[] fitness = this.m_ReferenceSolution.getFitness();
            JPanel tmpP = new JPanel();
            JTextField textA;
            tmpP.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor      = GridBagConstraints.WEST;
            gbc.fill        = GridBagConstraints.BOTH;
            gbc.gridx       = 0;
            gbc.gridy       = 0;
            gbc.weightx     = 2;
            tmpP.add(new JLabel("Objective:"), gbc);
            gbc.gridx       = 1;
            gbc.gridy       = 0;
            gbc.weightx     = 1;
            tmpP.add(new JLabel(""), gbc);
            gbc.gridx       = 2;
            gbc.gridy       = 0;
            gbc.weightx     = 2;
            tmpP.add(new JLabel("Value"), gbc);
            for (int i = 0; i < obj.length; i++) {
                gbc.gridx       = 0;
                gbc.gridy       = i+1;
                gbc.weightx     = 1;
                tmpP.add(new JLabel(""+obj[i].getIdentName()), gbc);
                gbc.gridx       = 1;
                gbc.gridy       = i+1;
                gbc.weightx     = 1;
                if (obj[i].is2BMinimized()) {
                    textA = new JTextField("min");
                }
                else {
                    textA = new JTextField("max");
                }
                textA.setEditable(false);
                tmpP.add(textA, gbc);
                gbc.gridx       = 2;
                gbc.gridy       = i+1;
                gbc.weightx     = 1;
                textA = new JTextField(""+((Double)m_ReferenceSolution.getData(obj[i].getIdentName())).doubleValue());
                textA.setEditable(false);
                tmpP.add(textA, gbc);

            }
            this.m_Selected.add(tmpP, BorderLayout.CENTER);
        }
        this.m_Selected.validate();
    }

    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (m_ReferenceSolution != null) {
                m_Mocco.m_View.setRefSolutionSelectable(false);
                m_Mocco.m_View.removeRefSolutionSelectionListeners();
                m_Mocco.m_JPanelControl.removeAll();
                m_Mocco.m_JPanelControl.validate();
                m_Mocco.m_JPanelParameters.removeAll();
                m_Finished = true;
            } else {
                JOptionPane.showMessageDialog(m_Mocco.m_JFrame,
                    "No reference solution selected. Cannot proceed!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    public AbstractEAIndividual getReferenceSolution() {
        return this.m_ReferenceSolution;
    }
    /******************************************************************************
     *     InterfaceSelectionListener
     */

    /** This method will notify the listener that an
     * Individual has been selected
     * @param indy  The selected individual
     */
    @Override
    public void individualSelected(AbstractEAIndividual indy) {
        Population pop = this.m_Mocco.m_State.m_ParetoFront.getMarkedIndividuals();
        if (pop.size() == 1) {
            this.m_ReferenceSolution = (AbstractEAIndividual)pop.get(0);
        } else {
            this.m_ReferenceSolution = null;
        }
        this.updateSelected();
    }
}