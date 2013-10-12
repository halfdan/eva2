package eva2.optimization.mocco.paretofrontviewer;


import eva2.gui.plot.FunctionArea;
import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.InterfaceDPointWithContent;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.optimization.problems.InterfaceOptimizationObjective;
import eva2.tools.chart2d.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.11.2005
 * Time: 11:16:11
 * To change this template use File | Settings | File Templates.
 */
public class ParetoFrontView2D extends JPanel implements InterfaceParetoFrontView, InterfaceRefPointListener {

    public MOCCOViewer m_MOCCOViewer;
    private JPanel m_JPMain;
    private JPanel m_JPTop;
    private JComboBox m_JCObjective1, m_JCObjective2, m_JCFitObj;
    private FunctionArea m_Area;
    private ScaledBorder m_AreaBorder;
    private InterfaceRefPointListener m_RefPointListener;

    public ParetoFrontView2D(MOCCOViewer t) {
        this.m_MOCCOViewer = t;
        this.init();
    }

    private void init() {
        this.m_JPMain = new JPanel();
        this.m_JPMain.setLayout(new BorderLayout());
        this.setLayout(new BorderLayout());
        this.add(this.m_JPMain, BorderLayout.CENTER);
        this.m_Area = new FunctionArea("?", "?");
        this.m_Area.setPreferredSize(new Dimension(450, 450));
        this.m_Area.setMinimumSize(new Dimension(450, 450));
        this.m_AreaBorder = new ScaledBorder();
        this.m_AreaBorder.x_label = "?";
        this.m_AreaBorder.y_label = "?";
        this.m_Area.setBorder(this.m_AreaBorder);
        this.m_Area.setBackground(Color.WHITE);
        this.m_JPMain.add(this.m_Area, BorderLayout.CENTER);
        this.m_JPTop = new JPanel();
        this.m_JPTop.setLayout(new GridLayout(1, 5));
        String[] tmp = new String[2];
        tmp[0] = "Fitness";
        tmp[1] = "Objective";
        this.m_JCFitObj = new JComboBox(tmp);
        this.m_JCFitObj.setSelectedIndex(0);
        this.m_JCFitObj.addActionListener(this.jcombobox2Listener);
        this.m_JCObjective1 = new JComboBox(this.getAvailableObjectiveNames());
        this.m_JCObjective1.setSelectedIndex(0);
        this.m_JCObjective1.addActionListener(this.jcomboboxListener);
        this.m_JPTop.add(this.m_JCObjective1);
        this.m_JPTop.add(new JLabel(" vs "));
        this.m_JCObjective2 = new JComboBox(this.getAvailableObjectiveNames());
        try {
            this.m_JCObjective2.setSelectedIndex(1);
        } catch (java.lang.IllegalArgumentException a) {
            // there seems to be no alternative
        }
        this.m_JCObjective2.addActionListener(this.jcomboboxListener);
        this.m_JPTop.add(this.m_JCObjective2);
        this.m_JPTop.add(new JLabel(" using:"));

        this.m_JPTop.add(this.m_JCFitObj);
        this.m_JPMain.add(this.m_JPTop, BorderLayout.NORTH);
        this.updateView();
    }

    ActionListener jcomboboxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateView();
        }
    };

    ActionListener jcombobox2Listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateObjectiveComboBoxes();
        }
    };

    public void updateObjectiveComboBoxes() {
        m_JCObjective1.removeActionListener(jcomboboxListener);
        m_JCObjective2.removeActionListener(jcomboboxListener);
        m_JCObjective1.removeAllItems();
        String[] tmpS = getAvailableObjectiveNames();
        for (int i = 0; i < tmpS.length; i++) {
            m_JCObjective1.addItem(tmpS[i]);
        }
        m_JCObjective1.setSelectedIndex(0);
        m_JCObjective2.removeAllItems();
        tmpS = getAvailableObjectiveNames();
        for (int i = 0; i < tmpS.length; i++) {
            m_JCObjective2.addItem(tmpS[i]);
        }
        m_JCObjective2.setSelectedIndex(1);
        m_JCObjective1.addActionListener(jcomboboxListener);
        m_JCObjective2.addActionListener(jcomboboxListener);
        updateView();
    }

    private String[] getAvailableObjectiveNames() {
        String[] result = null;
        if (this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem == null) {
            result = new String[1];
            result[0] = "none";
            return result;
        }
        if (this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) {
            InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem) this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            result = new String[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                result[i] = tmp[i].getIdentName();
            }
            if (this.m_JCFitObj.getSelectedIndex() == 0) {
                // in this case constraints have to be omitted
                ArrayList tmpList = new ArrayList();
                for (int i = 0; i < tmp.length; i++) {
                    if (!tmp[i].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                        tmpList.add(result[i]);
                    }
                }
                result = new String[tmpList.size()];
                for (int i = 0; i < tmpList.size(); i++) {
                    result[i] = (String) tmpList.get(i);
                }
            }
        }
        return result;
    }

    /**
     * This method notifies the Pareto front view that
     * the data has changed most likely due to changes in
     * the problem definition
     */
    @Override
    public void updateView() {
        // i assume that all the populations are evaluated
        // all using the same problem
        this.m_Area.removeAll();
        // first set the names of the objectives
        String[] tmpS = this.getAvailableObjectiveNames();
        if (tmpS.length != this.m_JCObjective1.getItemCount()) {
            updateObjectiveComboBoxes();
        }
        boolean equal = true;
        for (int i = 0; i < tmpS.length; i++) {
            if (!tmpS[i].equalsIgnoreCase((String) this.m_JCObjective1.getItemAt(i))) {
                equal = false;
            }
        }
        if (!equal) {
            updateObjectiveComboBoxes();
        }
        if (this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) {
            //InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem)this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            String[] objectives = this.getAvailableObjectiveNames();
            this.m_AreaBorder.x_label = "" + objectives[this.m_JCObjective1.getSelectedIndex()];
            this.m_AreaBorder.y_label = "" + objectives[this.m_JCObjective2.getSelectedIndex()];
        }
        if ((this.m_MOCCOViewer.m_MOCCO.m_State.m_ParetoFront != null) && (this.m_MOCCOViewer.m_MOCCO.m_State.m_ParetoFront.size() > 0)) {
            this.plot2DParetoFront();
        }
        this.validate();
    }

    /**
     * This method will plot a simple fitness plot, using the iterations a x-axis
     */
    public void plot2DParetoFront() {
        double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
        this.m_Area.clearAll();
        this.m_Area.setBackground(Color.WHITE);
        GraphPointSet mySet;
        DPoint myPoint;
        double[] fitness;
        int indexX, indexY;
        indexX = this.m_JCObjective1.getSelectedIndex();
        indexY = this.m_JCObjective2.getSelectedIndex();
        // first plot the populations
        if (false) {
            for (int i = 0; i < this.m_MOCCOViewer.m_MOCCO.m_State.m_PopulationHistory.length; i++) {
                //System.out.println("Population " + i+" show " +this.m_MOCCOViewer.m_MOCCO.m_State.m_Show[i]);
                if (this.m_MOCCOViewer.m_MOCCO.m_State.m_Show[i]) {
                    mySet = new GraphPointSet(i + 10, this.m_Area);
                    mySet.setConnectedMode(false);
                    mySet.setColor(this.m_MOCCOViewer.m_MOCCO.m_State.m_Color[i]);
                    //System.out.println(((ArrayList)this.m_Boss.m_FitnessCache.get(i)).size()+"/"+((ArrayList)this.m_Boss.m_ObjectiveCache.get(i)).size());
                    if (this.m_JCFitObj.getSelectedIndex() == 0) {
                        for (int j = 0; j < ((ArrayList) this.m_MOCCOViewer.m_MOCCO.m_State.m_FitnessCache.get(i)).size(); j++) {
                            fitness = (double[]) ((ArrayList) this.m_MOCCOViewer.m_MOCCO.m_State.m_FitnessCache.get(i)).get(j);
                            myPoint = new DPoint(fitness[indexX], fitness[indexY]);
                            if (((Double) ((ArrayList) this.m_MOCCOViewer.m_MOCCO.m_State.m_ConstraintCache.get(i)).get(j)).doubleValue() == 0) {
                                myPoint.setIcon(new Chart2DDPointIconCross());
                            } else {
                                myPoint.setIcon(new Chart2DDPointIconPoint());
                            }
                            mySet.addDPoint(myPoint);
                            if (fitness[indexX] < xmin) {
                                xmin = fitness[indexX];
                            }
                            if (fitness[indexX] > xmax) {
                                xmax = fitness[indexX];
                            }
                            if (fitness[indexY] < ymin) {
                                ymin = fitness[indexY];
                            }
                            if (fitness[indexY] > ymax) {
                                ymax = fitness[indexY];
                            }
                        }
                    } else {
                        for (int j = 0; j < ((ArrayList) this.m_MOCCOViewer.m_MOCCO.m_State.m_ObjectiveCache.get(i)).size(); j++) {
                            fitness = (double[]) ((ArrayList) this.m_MOCCOViewer.m_MOCCO.m_State.m_ObjectiveCache.get(i)).get(j);
                            myPoint = new DPoint(fitness[indexX], fitness[indexY]);
                            if (((Double) ((ArrayList) this.m_MOCCOViewer.m_MOCCO.m_State.m_ConstraintCache.get(i)).get(j)).doubleValue() == 0) {
                                myPoint.setIcon(new Chart2DDPointIconCross());
                            } else {
                                myPoint.setIcon(new Chart2DDPointIconPoint());
                            }
                            mySet.addDPoint(myPoint);
                            if (fitness[indexX] < xmin) {
                                xmin = fitness[indexX];
                            }
                            if (fitness[indexX] > xmax) {
                                xmax = fitness[indexX];
                            }
                            if (fitness[indexY] < ymin) {
                                ymin = fitness[indexY];
                            }
                            if (fitness[indexY] > ymax) {
                                ymax = fitness[indexY];
                            }
                        }
                    }
                }
            }
        }
        // now mark the ParetoFront
        Population pf = this.m_MOCCOViewer.m_MOCCO.m_State.m_ParetoFront;
        if (pf.size() > 0) {
            DPoint point;
            DPointIcon icon;
            mySet = new GraphPointSet(1, this.m_Area);
            mySet.setConnectedMode(false);
            mySet.setColor(Color.BLACK);
            for (int i = 0; i < pf.size(); i++) {
                if (this.m_JCFitObj.getSelectedIndex() == 0) {
                    fitness = ((AbstractEAIndividual) pf.get(i)).getFitness();
                } else {
                    InterfaceOptimizationObjective[] tmpObj = ((InterfaceMultiObjectiveDeNovoProblem) this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
                    fitness = new double[tmpObj.length];
                    for (int k = 0; k < tmpObj.length; k++) {
                        fitness[k] = ((Double) ((AbstractEAIndividual) pf.get(i)).getData(tmpObj[k].getIdentName())).doubleValue();
                    }
                }
                point = new DPoint(fitness[indexX], fitness[indexY]);
                icon = new Chart2DDPointContentSelectable();
                if (this.m_MOCCOViewer.m_RefSolutionSelectable) {
                    ((Chart2DDPointContentSelectable) icon).addSelectionListener(this.m_MOCCOViewer);
                }
                ((InterfaceDPointWithContent) icon).setProblem(this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem);
                ((InterfaceDPointWithContent) icon).setEAIndividual((AbstractEAIndividual) pf.get(i));
                point.setIcon(icon);
                mySet.addDPoint(point);
                if (fitness[indexX] < xmin) {
                    xmin = fitness[indexX];
                }
                if (fitness[indexX] > xmax) {
                    xmax = fitness[indexX];
                }
                if (fitness[indexY] < ymin) {
                    ymin = fitness[indexY];
                }
                if (fitness[indexY] > ymax) {
                    ymax = fitness[indexY];
                }
            }
        }

        mySet = new GraphPointSet(0, this.m_Area);
        mySet.setConnectedMode(false);
        double xrange = (xmax - xmin), yrange = (ymax - ymin);
        if ((new Double(xrange)).isNaN()) {
            mySet.addDPoint(0, 0);
            mySet.addDPoint(1, 1);
        } else {
            mySet.addDPoint(xmin - 0.1 * xrange, ymin - 0.1 * yrange);
            mySet.addDPoint(xmin - 0.1 * xrange, ymin - 0.1 * yrange);
            mySet.addDPoint(xmax + 0.1 * xrange, ymax + 0.1 * yrange);
            mySet.addDPoint(xmax + 0.1 * xrange, ymax + 0.1 * yrange);
        }

        // now lets prepare the contraints or goals if any...
        if ((this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) &&
                (this.m_JCFitObj.getSelectedIndex() == 1)) {
            InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem) this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            for (int i = 0; i < tmp.length; i++) {
                if ((!(tmp[i].getOptimizationMode().equalsIgnoreCase("Objective"))) &&
                        ((this.m_JCObjective1.getSelectedIndex() == i) ||
                                (this.m_JCObjective2.getSelectedIndex() == i))) {
                    double tmpD = tmp[i].getConstraintGoal();
                    if (!(new Double(tmpD)).isNaN()) {
                        // draw a line indicating the constraint/goals
                        mySet = new GraphPointSet(500 + i, this.m_Area);
                        mySet.setConnectedMode(true);
                        if (this.m_JCObjective1.getSelectedIndex() == i) {
                            if ((tmpD > xmin - 0.1 * xrange) && (tmpD < xmax + 0.1 * xrange)) {
                                mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                                mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                                mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
                                mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
                            }
                        } else {
                            if ((tmpD > ymin - 0.1 * yrange) && (tmpD < ymax + 0.1 * yrange)) {
                                mySet.addDPoint(xmin - 0.1 * xrange, tmpD);
                                mySet.addDPoint(xmin - 0.1 * xrange, tmpD);
                                mySet.addDPoint(xmax + 0.1 * xrange, tmpD);
                                mySet.addDPoint(xmax + 0.1 * xrange, tmpD);
                            }
                        }
                    }
                }
            }
        }

        // lets prepare to reference point selection
        if (this.m_MOCCOViewer.m_RefPointSelectable) {
            this.m_Area.addRefPointSelectionListener(this);
        } else {
            this.m_Area.removeRefPointSelectionListeners();
            this.removeRefPointSelectionListeners();
        }

        // lets show a reference point if given...
        if ((this.m_MOCCOViewer.m_ReferencePoint != null) && (this.m_JCFitObj.getSelectedIndex() == 0)) {
            mySet = new GraphPointSet(1001, this.m_Area);
            mySet.setConnectedMode(true);
            mySet.setColor(Color.RED);
            double tmpD = this.m_MOCCOViewer.m_ReferencePoint[this.m_JCObjective1.getSelectedIndex()];
            if ((tmpD > xmin - 0.1 * xrange) && (tmpD < xmax + 0.1 * xrange)) {
                mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
                mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
            }
            mySet = new GraphPointSet(1002, this.m_Area);
            mySet.setConnectedMode(true);
            mySet.setColor(Color.RED);
            tmpD = this.m_MOCCOViewer.m_ReferencePoint[this.m_JCObjective2.getSelectedIndex()];
            if ((tmpD > ymin - 0.1 * yrange) && (tmpD < ymax + 0.1 * yrange)) {
                mySet.addDPoint(xmin - 0.1 * xrange, tmpD);
                mySet.addDPoint(xmin - 0.1 * xrange, tmpD);
                mySet.addDPoint(xmax + 0.1 * xrange, tmpD);
                mySet.addDPoint(xmax + 0.1 * xrange, tmpD);
            }
        }
    }

    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /**
     * This method will notify the listener that a point has been selected
     * It has to be noted though that at this point the reference point is not
     * a full vector since it is only 2d
     *
     * @param point The selected point, most likely 2d
     */
    @Override
    public void refPointGiven(double[] point) {
        if (this.m_JCFitObj.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this.m_MOCCOViewer.m_MOCCO.m_JFrame,
                    "Reference point needs to be selected in fitness space!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            double[] tmpD = this.m_MOCCOViewer.m_ReferencePoint;
            tmpD[this.m_JCObjective1.getSelectedIndex()] = point[0];
            tmpD[this.m_JCObjective2.getSelectedIndex()] = point[1];
            this.m_MOCCOViewer.refPointGiven(tmpD);
        }
    }

    /**
     * This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     *
     * @param a The selection listener
     */
    public void addRefPointSelectionListener(InterfaceRefPointListener a) {
        this.m_RefPointListener = a;
    }

    /**
     * This method returns the selection listner to the PointIcon
     *
     * @return InterfaceSelectionListener
     */
    public InterfaceRefPointListener getRefPointSelectionListener() {
        return this.m_RefPointListener;
    }

    /**
     * This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefPointSelectionListeners() {
        this.m_RefPointListener = null;
    }
}
