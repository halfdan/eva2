package eva2.server.go.mocco.paretofrontviewer;


import javax.swing.*;

import eva2.gui.FunctionArea;
import eva2.gui.GraphPointSet;
import eva2.gui.InterfaceDPointWithContent;
import eva2.server.go.MOCCOStandalone;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.server.go.problems.InterfaceOptimizationObjective;
import eva2.tools.chart2d.Chart2DDPointContentSelectable;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;
import eva2.tools.chart2d.ScaledBorder;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.11.2005
 * Time: 11:02:47
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOViewer extends JPanel implements InterfaceRefSolutionListener, InterfaceRefPointListener {

    public MOCCOStandalone  m_MOCCO;
    public MOCCOViewer      m_self;
    private InterfaceParetoFrontView    m_View;
    private FunctionArea    m_1DView;
    public JSplitPane       m_JSplit;
    public JPanel           m_ViewPanel, m_Choices;
    private InterfaceRefSolutionListener        m_RefSolutionListener;
    private InterfaceRefPointListener           m_RefPointListener;
    public double[]         m_ReferencePoint = null;
    public boolean          m_SelectUniqueSolution      = true;
    public boolean          m_RefPointSelectable        = false;
    public boolean          m_RefSolutionSelectable     = false;

    public MOCCOViewer(MOCCOStandalone t) {
        this.m_MOCCO    = t;
        this.m_self     = this;
        this.init();
    }

    public void init() {
        this.setLayout(new BorderLayout());
        // the view
        this.m_ViewPanel    = new JPanel();
        this.m_ViewPanel.setLayout(new BorderLayout());
        this.m_View         = new ParetoFrontView2D(this);
        this.m_ViewPanel.add((JPanel)this.m_View, BorderLayout.CENTER);
        // the parameters
        this.m_Choices = new JPanel();
        JPanel tmpP = new JPanel();
        this.m_Choices.setLayout(new GridLayout(1,2));
        this.m_Choices.add(tmpP);
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.BOTH;
        gbc.gridx       = 0;
        gbc.gridy       = 0;
        gbc.weightx     = 1;
        tmpP.add(new JLabel("Choose View:"), gbc);
        String[] tmpList = new String[2];
        tmpList[0] = "2D Pareto Front";
        tmpList[1] = "Scatter Plot";
        //tmpList[2] = "Parallel Axsis";
        //tmpList[3] = "Problemspec. Viewer";
        JComboBox tmpC = new JComboBox(tmpList);
        tmpC.setSelectedIndex(0);
        tmpC.addActionListener(paretoFrontViewChanged);
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.BOTH;
        gbc.gridx       = 1;
        gbc.gridy       = 0;
        gbc.weightx     = 3;
        tmpP.add(tmpC, gbc);
        JButton JBSaveParetoFront = new JButton("Save Pareto Front");
        JBSaveParetoFront.addActionListener(this.saveParetoFront);
        this.m_Choices.add(JBSaveParetoFront);
        // the main panel
        this.add(this.m_ViewPanel, BorderLayout.CENTER);
        this.add(this.m_Choices, BorderLayout.SOUTH);

        // the old version
//        this.setLayout(new BorderLayout());
//        this.m_Main         = new JPanel();
//        this.add(this.m_Main, BorderLayout.CENTER);
//        this.m_ViewPanel    = new JPanel();
//        this.m_ViewPanel.setLayout(new BorderLayout());
//        this.m_View         = new ParetoFrontView2D(this);
//        this.m_ViewPanel.add((JPanel)this.m_View, BorderLayout.CENTER);
//        this.m_Parameters   = new JPanel();
//        this.m_Parameters.setLayout(new BorderLayout());
//        this.m_Choices      = new JPanel();
//        this.m_Choices.setBorder(BorderFactory.createCompoundBorder(
//		    BorderFactory.createTitledBorder("Parameterize Visualization:"),
//			BorderFactory.createEmptyBorder(0, 5, 5, 5)));
//        this.m_History      = new JPanel();
//        JButton JBSaveParetoFront = new JButton("Save Pareto Front");
//        JBSaveParetoFront.addActionListener(this.saveParetoFront);
//        this.m_Parameters.add(this.m_Choices, BorderLayout.NORTH);
//        this.m_Parameters.add(this.m_History, BorderLayout.CENTER);
//        this.m_Parameters.add(JBSaveParetoFront, BorderLayout.SOUTH);
//        this.m_JSplit       = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.m_ViewPanel, this.m_Parameters);
//        this.m_JSplit.setOneTouchExpandable(true);
//        this.m_Main.setLayout(new BorderLayout());
//        this.m_Main.add(this.m_JSplit, BorderLayout.CENTER);
//
//        this.initChoices();
//        this.updateHistory();
    }

//    private void initChoices() {
//        JPanel tmpP = new JPanel();
//        this.m_Choices.setLayout(new BorderLayout());
//        this.m_ChoicesParameters = new JPanel();
//        this.m_Choices.add(tmpP, BorderLayout.NORTH);
//        this.m_Choices.add(this.m_ChoicesParameters, BorderLayout.CENTER);
//        tmpP.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.anchor      = GridBagConstraints.WEST;
//        gbc.fill        = GridBagConstraints.BOTH;
//        gbc.gridx       = 0;
//        gbc.gridy       = 0;
//        gbc.weightx     = 1;
//        tmpP.add(new JLabel("Choose View:"), gbc);
//        String[] tmpList = new String[2];
//        tmpList[0] = "2D Pareto Front";
//        tmpList[1] = "Scatter Plot";
//        //tmpList[2] = "Parallel Axsis";
//        //tmpList[3] = "Problemspec. Viewer";
//        JComboBox tmpC = new JComboBox(tmpList);
//        tmpC.setSelectedIndex(0);
//        tmpC.addActionListener(paretoFrontViewChanged);
//        gbc.anchor      = GridBagConstraints.WEST;
//        gbc.fill        = GridBagConstraints.BOTH;
//        gbc.gridx       = 1;
//        gbc.gridy       = 0;
//        gbc.weightx     = 3;
//        tmpP.add(tmpC, gbc);
//    }

    ActionListener paretoFrontViewChanged = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            JComboBox tmpC = (JComboBox)event.getSource();
            int index = tmpC.getSelectedIndex();
            switch (index) {
                case 0: {
                    m_ViewPanel.removeAll();
                    m_View = new ParetoFrontView2D(m_self);
                    m_ViewPanel.add((JPanel)m_View, BorderLayout.CENTER);
                    //problemChanged(false);
                    break;
                }
                case 1: {
                    m_ViewPanel.removeAll();
                    m_View = new ParetoFrontViewScatterPlot(m_self);
                    m_ViewPanel.add((JPanel)m_View, BorderLayout.CENTER);
                    //problemChanged(false);
                    break;
                }
                case 2: {
                    m_ViewPanel.removeAll();
                    m_View = new ParetoFrontViewParallelAxsis(m_self);
                    m_ViewPanel.add((JPanel)m_View, BorderLayout.CENTER);
                    //problemChanged(false);
                    break;
                }
                case 3: {
                    m_ViewPanel.removeAll();
                    m_View = ((InterfaceMultiObjectiveDeNovoProblem)m_MOCCO.m_State.m_CurrentProblem).getParetoFrontViewer4MOCCO(m_self);
                    m_ViewPanel.add((JComponent)m_View, BorderLayout.CENTER);
                    //problemChanged(false);
                    break;
                }
            }
            m_ViewPanel.updateUI();
        }
    };

    ActionListener saveParetoFront = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_'HH.mm.ss");
            String m_StartDate = formatter.format(new Date());
            BufferedWriter  out = null;
            Population      pop;

            String name = "MOCCO_"+m_StartDate+"_PF_Iteration_"+m_MOCCO.m_Iteration+".dat", tmp;
            pop = m_MOCCO.m_State.m_ParetoFront;
            try {
                out = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem)m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) tmp += obj[j].getIdentName() +"\t";
            try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
            for (int i = 0; i < pop.size(); i++) {
                if (!((AbstractEAIndividual)pop.get(i)).violatesConstraint()) {
                    // write
                    tmp ="";
                    double[] fit = ((AbstractEAIndividual)pop.get(i)).getFitness();
                    for (int j = 0; j < fit.length; j++) {
                        tmp += fit[j] +"\t";
                    }
                    try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
                }
            }
            try { out.close(); } catch(java.io.IOException e) {}

            name = "MOCCO_"+m_StartDate+"_All_Iteration_"+m_MOCCO.m_Iteration+".dat";
            try {
                out = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            pop = new Population();
            for (int i = 0; i < m_MOCCO.m_State.m_PopulationHistory.length; i++) pop.addPopulation(m_MOCCO.m_State.m_PopulationHistory[i]);
            obj = ((InterfaceMultiObjectiveDeNovoProblem)m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) tmp += obj[j].getIdentName() +"\t";
            try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
            for (int i = 0; i < pop.size(); i++) {
                if (!((AbstractEAIndividual)pop.get(i)).violatesConstraint()) {
                    // write
                    tmp ="";
                    double[] fit = ((AbstractEAIndividual)pop.get(i)).getFitness();
                    for (int j = 0; j < fit.length; j++) {
                        tmp += fit[j] +"\t";
                    }
                    try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
                }
            }
            try { out.close(); } catch(java.io.IOException e) {}

            name = "MOCCO_"+m_StartDate+"_Infeasible_Iteration_"+m_MOCCO.m_Iteration+".dat";
            try {
                out = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            pop = new Population();
            for (int i = 0; i < m_MOCCO.m_State.m_PopulationHistory.length; i++) pop.addPopulation(m_MOCCO.m_State.m_PopulationHistory[i]);
            obj = ((InterfaceMultiObjectiveDeNovoProblem)m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) tmp += obj[j].getIdentName() +"\t";
            try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
            for (int i = 0; i < pop.size(); i++) {
                if (((AbstractEAIndividual)pop.get(i)).violatesConstraint()) {
                    // write
                    tmp ="";
                    double[] fit = ((AbstractEAIndividual)pop.get(i)).getFitness();
                    for (int j = 0; j < fit.length; j++) {
                        tmp += fit[j] +"\t";
                    }
                    try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
                }
            }
            try { out.close(); } catch(java.io.IOException e) {}

            name = "MOCCO_"+m_StartDate+"_RefSolutions_Iteration_"+m_MOCCO.m_Iteration+".dat";
            try {
                out = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            pop = m_MOCCO.m_State.m_ParetoFront.getMarkedIndividuals();
            obj = ((InterfaceMultiObjectiveDeNovoProblem)m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) tmp += obj[j].getIdentName() +"\t";
            try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
            for (int i = 0; i < pop.size(); i++) {
                // write
                tmp ="";
                double[] fit = ((AbstractEAIndividual)pop.get(i)).getFitness();
                for (int j = 0; j < fit.length; j++) {
                    tmp += fit[j] +"\t";
                }
                try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
            }
            try { out.close(); } catch(java.io.IOException e) {}
            
            name = "MOCCO_"+m_StartDate+"_RefPoint_Iteration_"+m_MOCCO.m_Iteration+".dat";
            try {
                out = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            pop = m_MOCCO.m_State.m_ParetoFront.getMarkedIndividuals();
            obj = ((InterfaceMultiObjectiveDeNovoProblem)m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) tmp += obj[j].getIdentName() +"\t";
            try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
            tmp = "";
            if (m_ReferencePoint != null) {
                for (int j = 0; j < m_ReferencePoint.length; j++) tmp += m_ReferencePoint[j] +"\t";
                try { out.write(tmp+"\n"); } catch (java.io.IOException e) { }
            }
            try { out.close(); } catch(java.io.IOException e) {}
        }
    };

    public void problemChanged(boolean t) {
        this.m_MOCCO.m_State.makeFitnessCache(t);
        //this.updateHistory();
        if (this.m_MOCCO.m_State.m_CurrentProblem.isMultiObjective()) {
            if (this.m_1DView != null) {
                this.m_ViewPanel.removeAll();
                this.m_ViewPanel.add((JPanel)m_View, BorderLayout.CENTER);
            }
            this.m_View.updateView();
            this.m_1DView = null;
        } else {
            // here I simply add a single-objective view..
            if (this.m_1DView == null) {
                this.m_ViewPanel.removeAll();
                this.m_ViewPanel.setLayout(new BorderLayout());
                this.m_1DView     = new FunctionArea("?","?");
                this.m_1DView.setPreferredSize(new Dimension(450, 450));
                this.m_1DView.setMinimumSize(new Dimension(450, 450));
                ScaledBorder areaBorder = new ScaledBorder();
                areaBorder.x_label = "Optimzation Step";
                areaBorder.y_label = "Fitness";
                this.m_1DView.setBorder(areaBorder);
                this.m_1DView.setBackground(Color.WHITE);
                this.m_ViewPanel.add(this.m_1DView, BorderLayout.CENTER);
            }
            this.plot1DFitnessPlot();
        }

    }

    /** This method will plot a simple fitness plot, using the iterations a x-axis
     *
     */
    public void plot1DFitnessPlot() {
        double xmin = 0, ymin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY, fitness;
        Population[] pops = this.m_MOCCO.m_State.m_PopulationHistory;
        if ((pops == null) || (pops.length < 1)) return;
        GraphPointSet           mySet;
        DPoint                  myPoint;
        DPointIcon              icon;
        this.m_1DView.removeAll();
        mySet = new GraphPointSet(1, this.m_1DView);
        mySet.setConnectedMode(true);
        mySet.setColor(Color.BLACK);
        for (int i = 0; i < pops.length; i++) {
            fitness = pops[i].getBestEAIndividual().getFitness()[0];
            myPoint = new DPoint(i+1, fitness);
            icon = new Chart2DDPointContentSelectable();
            //if (this.m_MOCCOViewer.m_RefSolutionSelectable) ((Chart2DDPointContentSelectable)icon).addSelectionListener(this.m_MOCCOViewer);
            ((InterfaceDPointWithContent)icon).setProblem(this.m_MOCCO.m_State.m_CurrentProblem);
            ((InterfaceDPointWithContent)icon).setEAIndividual(pops[i].getBestEAIndividual());
            myPoint.setIcon(icon);
            mySet.addDPoint(myPoint);
            if (fitness < ymin) ymin = fitness;
            if (fitness > ymax) ymax = fitness;
        }
        mySet = new GraphPointSet(2, this.m_1DView);
        mySet.setConnectedMode(false);
        double yrange = ymax - ymin;
        if (yrange < 0.00001) yrange = 0.00001;
        mySet.addDPoint(0, ymin - 0.1 * yrange);
        mySet.addDPoint(pops.length+2, ymax + 0.1 * yrange);
        this.m_ViewPanel.validate();
    }


    /******************************************************************************
     *     InterfaceSelectionListener
     */

    /** This method will notify the listener that an
     * Individual has been selected
     * @param indy  The selected individual
     */
    public void individualSelected(AbstractEAIndividual indy) {
        if (indy.isMarked()) indy.unmark();
        else {
            if (this.m_SelectUniqueSolution) this.m_MOCCO.m_State.m_ParetoFront.unmarkAllIndividuals();
            indy.mark();
        }
        this.m_View.updateView();
        if (this.m_RefSolutionListener != null) this.m_RefSolutionListener.individualSelected(indy);
    }

    /** This method allows to toggel unique selection mode
     * @param t
     */
    public void setUniquelySelectable(boolean t) {
        this.m_SelectUniqueSolution = t;
    }

    /** This method allows to toggel selection mode
     * @param t
     */
    public void setRefSolutionSelectable(boolean t) {
        this.m_RefSolutionSelectable = t;
        this.m_View.updateView();
    }

    /** This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     * @param a The selection listener
     */
    public void addRefSolutionSelectionListener(InterfaceRefSolutionListener a) {
        this.m_RefSolutionListener = a;
    }

    /** This method returns the selection listner to the PointIcon
     * @return InterfaceSelectionListener
     */
    public InterfaceRefSolutionListener getRefSolutionSelectionListener() {
        return this.m_RefSolutionListener;
    }

    /** This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefSolutionSelectionListeners() {
        this.m_RefSolutionListener = null;
    }

    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /** This method will notify the listener that a point has been selected
     * @param point  The selected point, most likely 2d
     */
    public void refPointGiven(double[] point) {
        this.m_ReferencePoint = point;
        if (this.m_RefPointListener != null) this.m_RefPointListener.refPointGiven(point);
        this.m_View.updateView();
    }

    /** This method allows to toggel unique selection mode
     * @param t
     */
    public void setRefPointSelectable(boolean t) {
        this.m_RefPointSelectable = t;
        if (this.m_RefPointSelectable) {
            int dim = ((AbstractEAIndividual)this.m_MOCCO.m_State.m_ParetoFront.get(0)).getFitness().length;
            this.m_ReferencePoint = new double[dim];
            for (int i = 0; i < dim; i++) this.m_ReferencePoint[i] = 0;
        } else {
            //this.m_ReferencePoint = null;
        }
        this.m_View.updateView();
    }

    /** This method removes the reference point
     *
     */
    public void removeReferencePoint() {
        this.m_ReferencePoint = null;
    }

    /** This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     * @param a The selection listener
     */
    public void addRefPointSelectionListener(InterfaceRefPointListener a) {
        this.m_RefPointListener = a;
    }

    /** This method returns the selection listner to the PointIcon
     * @return InterfaceSelectionListener
     */
    public InterfaceRefPointListener getRefPointSelectionListener() {
        return this.m_RefPointListener;
    }

    /** This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefPointSelectionListeners() {
        this.m_RefPointListener = null;
        this.m_View.updateView();
    }

//  private void updateHistory() {
//      this.m_History.removeAll();
//      this.m_History.setLayout(new GridBagLayout());
//      this.m_JLIternations    = new JLabel[this.m_MOCCO.m_State.m_PopulationHistory.length];
//      this.m_JLIterSize       = new JLabel[this.m_MOCCO.m_State.m_PopulationHistory.length];
//      this.m_JCUse            = new JCheckBox[this.m_MOCCO.m_State.m_PopulationHistory.length];
//      this.m_JCShow           = new JCheckBox[this.m_MOCCO.m_State.m_PopulationHistory.length];
//      this.m_JColor           = new JComboBox[this.m_MOCCO.m_State.m_PopulationHistory.length];
//      GridBagConstraints gbc = new GridBagConstraints();
//      gbc.anchor      = GridBagConstraints.WEST;
//      gbc.fill        = GridBagConstraints.BOTH;
//      gbc.gridx       = 0;
//      gbc.gridy       = 0;
//      gbc.weightx     = 1;
//      this.m_History.add(new JLabel("Iteration"), gbc);
//      gbc.gridx       = 1;
//      gbc.gridy       = 0;
//      this.m_History.add(new JLabel("Size"), gbc);
//      gbc.gridx       = 2;
//      gbc.gridy       = 0;
//      this.m_History.add(new JLabel("Use"), gbc);
//      gbc.gridx       = 3;
//      gbc.gridy       = 0;
//      this.m_History.add(new JLabel("Show"), gbc);
//      gbc.gridx       = 4;
//      gbc.gridy       = 0;
//      this.m_History.add(new JLabel("Color"), gbc);
//      for (int i = 0; i < this.m_MOCCO.m_State.m_PopulationHistory.length; i++) {
//          gbc.gridx       = 0;
//          gbc.gridy       = i+1;
//          this.m_History.add(new JLabel((i+0)+"."), gbc);
//          gbc.gridx       = 1;
//          gbc.gridy       = i+1;
//          this.m_History.add(new JLabel(this.m_MOCCO.m_State.m_PopulationHistory[i].size()+""), gbc);
//          gbc.gridx       = 2;
//          gbc.gridy       = i+1;
//          this.m_JCUse[i] = new JCheckBox();
//          this.m_JCUse[i].setSelected(this.m_MOCCO.m_State.m_Use[i]);
//          this.m_JCUse[i].addActionListener(useModeChanged);
//          this.m_History.add(this.m_JCUse[i], gbc);
//          gbc.gridx       = 3;
//          gbc.gridy       = i+1;
//          this.m_JCShow[i] = new JCheckBox();
//          this.m_JCShow[i].setSelected(this.m_MOCCO.m_State.m_Show[i]);
//          this.m_JCShow[i].addActionListener(showModeChanged);
//          this.m_History.add(this.m_JCShow[i], gbc);
//          gbc.gridx       = 4;
//          gbc.gridy       = i+1;
//          this.m_JColor[i] = this.getComboBox(i);
//          this.m_JColor[i].addActionListener(colorModeChanged);
//          this.m_History.add(this.m_JColor[i], gbc);
//      }
//  }
//
//  ActionListener useModeChanged = new ActionListener() {
//      public void actionPerformed(ActionEvent event) {
//          for (int i = 0; i < m_JCUse.length; i++) {
//              m_MOCCO.m_State.m_Use[i] = m_JCUse[i].isSelected();
//          }
//      }
//  };
//  ActionListener showModeChanged = new ActionListener() {
//      public void actionPerformed(ActionEvent event) {
//          for (int i = 0; i < m_JCShow.length; i++) {
//              m_MOCCO.m_State.m_Show[i] = m_JCShow[i].isSelected();
//          }
//          m_View.updateView();
//      }
//  };
//  ActionListener colorModeChanged = new ActionListener() {
//      public void actionPerformed(ActionEvent event) {
//          for (int i = 0; i < m_JColor.length; i++) {
//              m_MOCCO.m_State.m_Color[i] = getColor(m_JColor[i].getSelectedIndex());
//          }
//          m_View.updateView();
//      }
//  };
//
//  private JComboBox getComboBox(int index) {
//      String[] colors = {"RED", "BLUE", "GREEN", "CYAN", "MAGENTA", "ORANGE"};
//      JComboBox result;
//      result = new JComboBox(colors);
//      int color = 0;
//      if (this.m_MOCCO.m_State.m_Color[index] == Color.RED)     color = 0;
//      if (this.m_MOCCO.m_State.m_Color[index] == Color.BLUE)    color = 1;
//      if (this.m_MOCCO.m_State.m_Color[index] == Color.GREEN)   color = 2;
//      if (this.m_MOCCO.m_State.m_Color[index] == Color.CYAN)    color = 3;
//      if (this.m_MOCCO.m_State.m_Color[index] == Color.MAGENTA) color = 4;
//      if (this.m_MOCCO.m_State.m_Color[index] == Color.ORANGE)  color = 5;
//      result.setSelectedIndex(color);
//      return result;
//  }
//  public Color getColor(int i) {
//      switch (i)  {
//          case 0 : {
//              return Color.RED;
//          }
//          case 1 : {
//              return Color.BLUE;
//          }
//          case 2 : {
//              return Color.GREEN;
//          }
//          case 3 : {
//              return Color.CYAN;
//          }
//          case 4 : {
//              return Color.MAGENTA;
//          }
//          case 5 : {
//              return Color.ORANGE;
//          }
//      }
//      return Color.BLACK;
//  }

}
