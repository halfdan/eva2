package eva2.optimization.mocco.paretofrontviewer;


import eva2.gui.plot.FunctionArea;
import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.InterfaceDPointWithContent;
import eva2.optimization.go.MOCCOStandalone;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.problems.InterfaceOptimizationObjective;
import eva2.tools.chart2d.Chart2DDPointContentSelectable;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;
import eva2.tools.chart2d.ScaledBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *
 */
public class MOCCOViewer extends JPanel implements InterfaceRefSolutionListener, InterfaceRefPointListener {

    public MOCCOStandalone moccoStandalone;
    public MOCCOViewer instance;
    private InterfaceParetoFrontView paretoFrontView;
    private FunctionArea functionAre;
    public JPanel viewPanel, choicesPanel;
    private InterfaceRefSolutionListener refSolutionListener;
    private InterfaceRefPointListener refPointListener;
    public double[] referencePoint = null;
    public boolean selectUniqueSolution = true;
    public boolean refPointSelectable = false;
    public boolean refSolutionSelectable = false;

    public MOCCOViewer(MOCCOStandalone t) {
        this.moccoStandalone = t;
        this.instance = this;
        this.init();
    }

    public void init() {
        this.setLayout(new BorderLayout());
        // the view
        this.viewPanel = new JPanel();
        this.viewPanel.setLayout(new BorderLayout());
        this.paretoFrontView = new ParetoFrontView2D(this);
        this.viewPanel.add((JPanel) this.paretoFrontView, BorderLayout.CENTER);
        // the parameters
        this.choicesPanel = new JPanel();
        JPanel tmpP = new JPanel();
        this.choicesPanel.setLayout(new GridLayout(1, 2));
        this.choicesPanel.add(tmpP);
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        tmpP.add(new JLabel("Choose View:"), gbc);
        String[] tmpList = new String[2];
        tmpList[0] = "2D Pareto Front";
        tmpList[1] = "Scatter Plot";
        //tmpList[2] = "Parallel Axsis";
        //tmpList[3] = "Problemspec. Viewer";
        JComboBox tmpC = new JComboBox(tmpList);
        tmpC.setSelectedIndex(0);
        tmpC.addActionListener(paretoFrontViewChanged);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 3;
        tmpP.add(tmpC, gbc);
        JButton JBSaveParetoFront = new JButton("Save Pareto Front");
        JBSaveParetoFront.addActionListener(this.saveParetoFront);
        this.choicesPanel.add(JBSaveParetoFront);
        // the main panel
        this.add(this.viewPanel, BorderLayout.CENTER);
        this.add(this.choicesPanel, BorderLayout.SOUTH);
    }

    ActionListener paretoFrontViewChanged = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            JComboBox tmpC = (JComboBox) event.getSource();
            int index = tmpC.getSelectedIndex();
            switch (index) {
                case 0: {
                    viewPanel.removeAll();
                    paretoFrontView = new ParetoFrontView2D(instance);
                    viewPanel.add((JPanel) paretoFrontView, BorderLayout.CENTER);
                    //problemChanged(false);
                    break;
                }
                case 1: {
                    viewPanel.removeAll();
                    paretoFrontView = new ParetoFrontViewScatterPlot(instance);
                    viewPanel.add((JPanel) paretoFrontView, BorderLayout.CENTER);
                    //problemChanged(false);
                    break;
                }
                case 2: {
                    viewPanel.removeAll();
                    paretoFrontView = new ParetoFrontViewParallelAxis(instance);
                    viewPanel.add((JPanel) paretoFrontView, BorderLayout.CENTER);
                    //problemChanged(false);
                    break;
                }
                case 3: {
                    viewPanel.removeAll();
                    paretoFrontView = ((InterfaceMultiObjectiveDeNovoProblem) moccoStandalone.state.currentProblem).getParetoFrontViewer4MOCCO(instance);
                    viewPanel.add((JComponent) paretoFrontView, BorderLayout.CENTER);
                    //problemChanged(false);
                    break;
                }
            }
            viewPanel.updateUI();
        }
    };

    ActionListener saveParetoFront = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_'HH.mm.ss");
            String startDate = formatter.format(new Date());
            BufferedWriter out = null;
            Population pop;

            String name = "MOCCO_" + startDate + "_PF_Iteration_" + moccoStandalone.iteration + ".dat", tmp;
            pop = moccoStandalone.state.paretoFront;
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) moccoStandalone.state.currentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) {
                tmp += obj[j].getIdentName() + "\t";
            }
            try {
                out.write(tmp + "\n");
            } catch (java.io.IOException e) {
            }
            for (int i = 0; i < pop.size(); i++) {
                if (!((AbstractEAIndividual) pop.get(i)).violatesConstraint()) {
                    // write
                    tmp = "";
                    double[] fit = ((AbstractEAIndividual) pop.get(i)).getFitness();
                    for (int j = 0; j < fit.length; j++) {
                        tmp += fit[j] + "\t";
                    }
                    try {
                        out.write(tmp + "\n");
                    } catch (java.io.IOException e) {
                    }
                }
            }
            try {
                out.close();
            } catch (java.io.IOException e) {
            }

            name = "MOCCO_" + startDate + "_All_Iteration_" + moccoStandalone.iteration + ".dat";
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            pop = new Population();
            for (int i = 0; i < moccoStandalone.state.populationHistory.length; i++) {
                pop.addPopulation(moccoStandalone.state.populationHistory[i]);
            }
            obj = ((InterfaceMultiObjectiveDeNovoProblem) moccoStandalone.state.currentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) {
                tmp += obj[j].getIdentName() + "\t";
            }
            try {
                out.write(tmp + "\n");
            } catch (java.io.IOException e) {
            }
            for (int i = 0; i < pop.size(); i++) {
                if (!((AbstractEAIndividual) pop.get(i)).violatesConstraint()) {
                    // write
                    tmp = "";
                    double[] fit = ((AbstractEAIndividual) pop.get(i)).getFitness();
                    for (int j = 0; j < fit.length; j++) {
                        tmp += fit[j] + "\t";
                    }
                    try {
                        out.write(tmp + "\n");
                    } catch (java.io.IOException e) {
                    }
                }
            }
            try {
                out.close();
            } catch (java.io.IOException e) {
            }

            name = "MOCCO_" + startDate + "_Infeasible_Iteration_" + moccoStandalone.iteration + ".dat";
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            pop = new Population();
            for (int i = 0; i < moccoStandalone.state.populationHistory.length; i++) {
                pop.addPopulation(moccoStandalone.state.populationHistory[i]);
            }
            obj = ((InterfaceMultiObjectiveDeNovoProblem) moccoStandalone.state.currentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) {
                tmp += obj[j].getIdentName() + "\t";
            }
            try {
                out.write(tmp + "\n");
            } catch (java.io.IOException e) {
            }
            for (int i = 0; i < pop.size(); i++) {
                if (((AbstractEAIndividual) pop.get(i)).violatesConstraint()) {
                    // write
                    tmp = "";
                    double[] fit = ((AbstractEAIndividual) pop.get(i)).getFitness();
                    for (int j = 0; j < fit.length; j++) {
                        tmp += fit[j] + "\t";
                    }
                    try {
                        out.write(tmp + "\n");
                    } catch (java.io.IOException e) {
                    }
                }
            }
            try {
                out.close();
            } catch (java.io.IOException e) {
            }

            name = "MOCCO_" + startDate + "_RefSolutions_Iteration_" + moccoStandalone.iteration + ".dat";
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            pop = moccoStandalone.state.paretoFront.getMarkedIndividuals();
            obj = ((InterfaceMultiObjectiveDeNovoProblem) moccoStandalone.state.currentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) {
                tmp += obj[j].getIdentName() + "\t";
            }
            try {
                out.write(tmp + "\n");
            } catch (java.io.IOException e) {
            }
            for (int i = 0; i < pop.size(); i++) {
                // write
                tmp = "";
                double[] fit = ((AbstractEAIndividual) pop.get(i)).getFitness();
                for (int j = 0; j < fit.length; j++) {
                    tmp += fit[j] + "\t";
                }
                try {
                    out.write(tmp + "\n");
                } catch (java.io.IOException e) {
                }
            }
            try {
                out.close();
            } catch (java.io.IOException e) {
            }

            name = "MOCCO_" + startDate + "_RefPoint_Iteration_" + moccoStandalone.iteration + ".dat";
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
                return;
            }
            pop = moccoStandalone.state.paretoFront.getMarkedIndividuals();
            obj = ((InterfaceMultiObjectiveDeNovoProblem) moccoStandalone.state.currentProblem).getProblemObjectives();
            tmp = "";
            for (int j = 0; j < obj.length; j++) {
                tmp += obj[j].getIdentName() + "\t";
            }
            try {
                out.write(tmp + "\n");
            } catch (java.io.IOException e) {
            }
            tmp = "";
            if (referencePoint != null) {
                for (int j = 0; j < referencePoint.length; j++) {
                    tmp += referencePoint[j] + "\t";
                }
                try {
                    out.write(tmp + "\n");
                } catch (java.io.IOException e) {
                }
            }
            try {
                out.close();
            } catch (java.io.IOException e) {
            }
        }
    };

    public void problemChanged(boolean t) {
        this.moccoStandalone.state.makeFitnessCache(t);
        //this.updateHistory();
        if (this.moccoStandalone.state.currentProblem.isMultiObjective()) {
            if (this.functionAre != null) {
                this.viewPanel.removeAll();
                this.viewPanel.add((JPanel) paretoFrontView, BorderLayout.CENTER);
            }
            this.paretoFrontView.updateView();
            this.functionAre = null;
        } else {
            // here I simply add a single-objective view..
            if (this.functionAre == null) {
                this.viewPanel.removeAll();
                this.viewPanel.setLayout(new BorderLayout());
                this.functionAre = new FunctionArea("?", "?");
                this.functionAre.setPreferredSize(new Dimension(450, 450));
                this.functionAre.setMinimumSize(new Dimension(450, 450));
                ScaledBorder areaBorder = new ScaledBorder();
                areaBorder.xLabel = "Optimzation Step";
                areaBorder.yLabel = "Fitness";
                this.functionAre.setBorder(areaBorder);
                this.functionAre.setBackground(Color.WHITE);
                this.viewPanel.add(this.functionAre, BorderLayout.CENTER);
            }
            this.plot1DFitnessPlot();
        }

    }

    /**
     * This method will plot a eva2.problems.simple fitness plot, using the iterations a x-axis
     */
    public void plot1DFitnessPlot() {
        double xmin = 0, ymin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY, fitness;
        Population[] pops = this.moccoStandalone.state.populationHistory;
        if ((pops == null) || (pops.length < 1)) {
            return;
        }
        GraphPointSet mySet;
        DPoint myPoint;
        DPointIcon icon;
        this.functionAre.removeAll();
        mySet = new GraphPointSet(1, this.functionAre);
        mySet.setConnectedMode(true);
        mySet.setColor(Color.BLACK);
        for (int i = 0; i < pops.length; i++) {
            fitness = pops[i].getBestEAIndividual().getFitness()[0];
            myPoint = new DPoint(i + 1, fitness);
            icon = new Chart2DDPointContentSelectable();
            ((InterfaceDPointWithContent) icon).setProblem(this.moccoStandalone.state.currentProblem);
            ((InterfaceDPointWithContent) icon).setEAIndividual(pops[i].getBestEAIndividual());
            myPoint.setIcon(icon);
            mySet.addDPoint(myPoint);
            if (fitness < ymin) {
                ymin = fitness;
            }
            if (fitness > ymax) {
                ymax = fitness;
            }
        }
        mySet = new GraphPointSet(2, this.functionAre);
        mySet.setConnectedMode(false);
        double yrange = ymax - ymin;
        if (yrange < 0.00001) {
            yrange = 0.00001;
        }
        mySet.addDPoint(0, ymin - 0.1 * yrange);
        mySet.addDPoint(pops.length + 2, ymax + 0.1 * yrange);
        this.viewPanel.validate();
    }


    /******************************************************************************
     *     InterfaceSelectionListener
     */

    /**
     * This method will notify the listener that an
     * Individual has been selected
     *
     * @param indy The selected individual
     */
    @Override
    public void individualSelected(AbstractEAIndividual indy) {
        if (indy.isMarked()) {
            indy.unmark();
        } else {
            if (this.selectUniqueSolution) {
                this.moccoStandalone.state.paretoFront.unmarkAllIndividuals();
            }
            indy.mark();
        }
        this.paretoFrontView.updateView();
        if (this.refSolutionListener != null) {
            this.refSolutionListener.individualSelected(indy);
        }
    }

    /**
     * This method allows to toggle unique selection mode
     *
     * @param t
     */
    public void setUniquelySelectable(boolean t) {
        this.selectUniqueSolution = t;
    }

    /**
     * This method allows to toggle selection mode
     *
     * @param t
     */
    public void setRefSolutionSelectable(boolean t) {
        this.refSolutionSelectable = t;
        this.paretoFrontView.updateView();
    }

    /**
     * This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     *
     * @param a The selection listener
     */
    public void addRefSolutionSelectionListener(InterfaceRefSolutionListener a) {
        this.refSolutionListener = a;
    }

    /**
     * This method returns the selection listner to the PointIcon
     *
     * @return InterfaceSelectionListener
     */
    public InterfaceRefSolutionListener getRefSolutionSelectionListener() {
        return this.refSolutionListener;
    }

    /**
     * This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefSolutionSelectionListeners() {
        this.refSolutionListener = null;
    }

    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /**
     * This method will notify the listener that a point has been selected
     *
     * @param point The selected point, most likely 2d
     */
    @Override
    public void refPointGiven(double[] point) {
        this.referencePoint = point;
        if (this.refPointListener != null) {
            this.refPointListener.refPointGiven(point);
        }
        this.paretoFrontView.updateView();
    }

    /**
     * This method allows to toggle unique selection mode
     *
     * @param t
     */
    public void setRefPointSelectable(boolean t) {
        this.refPointSelectable = t;
        if (this.refPointSelectable) {
            int dim = ((AbstractEAIndividual) this.moccoStandalone.state.paretoFront.get(0)).getFitness().length;
            this.referencePoint = new double[dim];
            for (int i = 0; i < dim; i++) {
                this.referencePoint[i] = 0;
            }
        } else {
            //this.referencePoint = null;
        }
        this.paretoFrontView.updateView();
    }

    /**
     * This method removes the reference point
     */
    public void removeReferencePoint() {
        this.referencePoint = null;
    }

    /**
     * This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     *
     * @param a The selection listener
     */
    public void addRefPointSelectionListener(InterfaceRefPointListener a) {
        this.refPointListener = a;
    }

    /**
     * This method returns the selection listner to the PointIcon
     *
     * @return InterfaceSelectionListener
     */
    public InterfaceRefPointListener getRefPointSelectionListener() {
        return this.refPointListener;
    }

    /**
     * This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefPointSelectionListeners() {
        this.refPointListener = null;
        this.paretoFrontView.updateView();
    }
}
