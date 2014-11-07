package eva2.optimization.mocco.paretofrontviewer;

import eva2.gui.plot.FunctionArea;
import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.InterfaceDPointWithContent;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.problems.InterfaceOptimizationObjective;
import eva2.tools.chart2d.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 */
public class ParetoFrontView2D extends JPanel implements InterfaceParetoFrontView, InterfaceRefPointListener {

    public MOCCOViewer moccoViewer;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JComboBox objective1, objective2, fitObjective;
    private FunctionArea functionArea;
    private ScaledBorder areaBorder;
    private InterfaceRefPointListener refPointListener;

    public ParetoFrontView2D(MOCCOViewer t) {
        this.moccoViewer = t;
        this.init();
    }

    private void init() {
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BorderLayout());
        this.setLayout(new BorderLayout());
        this.add(this.mainPanel, BorderLayout.CENTER);
        this.functionArea = new FunctionArea("?", "?");
        this.functionArea.setPreferredSize(new Dimension(450, 450));
        this.functionArea.setMinimumSize(new Dimension(450, 450));
        this.areaBorder = new ScaledBorder();
        this.areaBorder.xLabel = "?";
        this.areaBorder.yLabel = "?";
        this.functionArea.setBorder(this.areaBorder);
        this.functionArea.setBackground(Color.WHITE);
        this.mainPanel.add(this.functionArea, BorderLayout.CENTER);
        this.topPanel = new JPanel();
        this.topPanel.setLayout(new GridLayout(1, 5));
        String[] tmp = new String[2];
        tmp[0] = "Fitness";
        tmp[1] = "Objective";
        this.fitObjective = new JComboBox(tmp);
        this.fitObjective.setSelectedIndex(0);
        this.fitObjective.addActionListener(this.jcombobox2Listener);
        this.objective1 = new JComboBox(this.getAvailableObjectiveNames());
        this.objective1.setSelectedIndex(0);
        this.objective1.addActionListener(this.jcomboboxListener);
        this.topPanel.add(this.objective1);
        this.topPanel.add(new JLabel(" vs "));
        this.objective2 = new JComboBox(this.getAvailableObjectiveNames());
        try {
            this.objective2.setSelectedIndex(1);
        } catch (java.lang.IllegalArgumentException a) {
            // there seems to be no alternative
        }
        this.objective2.addActionListener(this.jcomboboxListener);
        this.topPanel.add(this.objective2);
        this.topPanel.add(new JLabel(" using:"));

        this.topPanel.add(this.fitObjective);
        this.mainPanel.add(this.topPanel, BorderLayout.NORTH);
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
        objective1.removeActionListener(jcomboboxListener);
        objective2.removeActionListener(jcomboboxListener);
        objective1.removeAllItems();
        String[] tmpS = getAvailableObjectiveNames();
        for (int i = 0; i < tmpS.length; i++) {
            objective1.addItem(tmpS[i]);
        }
        objective1.setSelectedIndex(0);
        objective2.removeAllItems();
        tmpS = getAvailableObjectiveNames();
        for (int i = 0; i < tmpS.length; i++) {
            objective2.addItem(tmpS[i]);
        }
        objective2.setSelectedIndex(1);
        objective1.addActionListener(jcomboboxListener);
        objective2.addActionListener(jcomboboxListener);
        updateView();
    }

    private String[] getAvailableObjectiveNames() {
        String[] result = null;
        if (this.moccoViewer.moccoStandalone.state.currentProblem == null) {
            result = new String[1];
            result[0] = "none";
            return result;
        }
        if (this.moccoViewer.moccoStandalone.state.currentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) {
            InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem) this.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
            result = new String[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                result[i] = tmp[i].getIdentName();
            }
            if (this.fitObjective.getSelectedIndex() == 0) {
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
        this.functionArea.removeAll();
        // first set the names of the objectives
        String[] tmpS = this.getAvailableObjectiveNames();
        if (tmpS.length != this.objective1.getItemCount()) {
            updateObjectiveComboBoxes();
        }
        boolean equal = true;
        for (int i = 0; i < tmpS.length; i++) {
            if (!tmpS[i].equalsIgnoreCase((String) this.objective1.getItemAt(i))) {
                equal = false;
            }
        }
        if (!equal) {
            updateObjectiveComboBoxes();
        }
        if (this.moccoViewer.moccoStandalone.state.currentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) {
            //InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem)this.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
            String[] objectives = this.getAvailableObjectiveNames();
            this.areaBorder.xLabel = "" + objectives[this.objective1.getSelectedIndex()];
            this.areaBorder.yLabel = "" + objectives[this.objective2.getSelectedIndex()];
        }
        if ((this.moccoViewer.moccoStandalone.state.paretoFront != null) && (this.moccoViewer.moccoStandalone.state.paretoFront.size() > 0)) {
            this.plot2DParetoFront();
        }
        this.validate();
    }

    /**
     * This method will plot a eva2.problems.simple fitness plot, using the iterations a x-axis
     */
    public void plot2DParetoFront() {
        double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
        this.functionArea.clearAll();
        this.functionArea.setBackground(Color.WHITE);
        GraphPointSet mySet;
        DPoint myPoint;
        double[] fitness;
        int indexX, indexY;
        indexX = this.objective1.getSelectedIndex();
        indexY = this.objective2.getSelectedIndex();
        // first plot the populations
        if (false) {
            for (int i = 0; i < this.moccoViewer.moccoStandalone.state.populationHistory.length; i++) {
                //System.out.println("Population " + i+" show " +this.moccoViewer.moccoStandalone.state.show[i]);
                if (this.moccoViewer.moccoStandalone.state.show[i]) {
                    mySet = new GraphPointSet(i + 10, this.functionArea);
                    mySet.setConnectedMode(false);
                    mySet.setColor(this.moccoViewer.moccoStandalone.state.colors[i]);
                    if (this.fitObjective.getSelectedIndex() == 0) {
                        for (int j = 0; j < ((ArrayList) this.moccoViewer.moccoStandalone.state.fitnessCache.get(i)).size(); j++) {
                            fitness = (double[]) ((ArrayList) this.moccoViewer.moccoStandalone.state.fitnessCache.get(i)).get(j);
                            myPoint = new DPoint(fitness[indexX], fitness[indexY]);
                            if ((Double) ((ArrayList) this.moccoViewer.moccoStandalone.state.constraintCache.get(i)).get(j) == 0) {
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
                        for (int j = 0; j < ((ArrayList) this.moccoViewer.moccoStandalone.state.objectiveCache.get(i)).size(); j++) {
                            fitness = (double[]) ((ArrayList) this.moccoViewer.moccoStandalone.state.objectiveCache.get(i)).get(j);
                            myPoint = new DPoint(fitness[indexX], fitness[indexY]);
                            if ((Double) ((ArrayList) this.moccoViewer.moccoStandalone.state.constraintCache.get(i)).get(j) == 0) {
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
        Population pf = this.moccoViewer.moccoStandalone.state.paretoFront;
        if (pf.size() > 0) {
            DPoint point;
            DPointIcon icon;
            mySet = new GraphPointSet(1, this.functionArea);
            mySet.setConnectedMode(false);
            mySet.setColor(Color.BLACK);
            for (int i = 0; i < pf.size(); i++) {
                if (this.fitObjective.getSelectedIndex() == 0) {
                    fitness = pf.get(i).getFitness();
                } else {
                    InterfaceOptimizationObjective[] tmpObj = ((InterfaceMultiObjectiveDeNovoProblem) this.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
                    fitness = new double[tmpObj.length];
                    for (int k = 0; k < tmpObj.length; k++) {
                        fitness[k] = (Double) pf.get(i).getData(tmpObj[k].getIdentName());
                    }
                }
                point = new DPoint(fitness[indexX], fitness[indexY]);
                icon = new Chart2DDPointContentSelectable();
                if (this.moccoViewer.refSolutionSelectable) {
                    ((Chart2DDPointContentSelectable) icon).addSelectionListener(this.moccoViewer);
                }
                ((InterfaceDPointWithContent) icon).setProblem(this.moccoViewer.moccoStandalone.state.currentProblem);
                ((InterfaceDPointWithContent) icon).setEAIndividual(pf.get(i));
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

        mySet = new GraphPointSet(0, this.functionArea);
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
        if ((this.moccoViewer.moccoStandalone.state.currentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) &&
                (this.fitObjective.getSelectedIndex() == 1)) {
            InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem) this.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
            for (int i = 0; i < tmp.length; i++) {
                if ((!(tmp[i].getOptimizationMode().equalsIgnoreCase("Objective"))) &&
                        ((this.objective1.getSelectedIndex() == i) ||
                                (this.objective2.getSelectedIndex() == i))) {
                    double tmpD = tmp[i].getConstraintGoal();
                    if (!(new Double(tmpD)).isNaN()) {
                        // draw a line indicating the constraint/goals
                        mySet = new GraphPointSet(500 + i, this.functionArea);
                        mySet.setConnectedMode(true);
                        if (this.objective1.getSelectedIndex() == i) {
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
        if (this.moccoViewer.refPointSelectable) {
            this.functionArea.addRefPointSelectionListener(this);
        } else {
            this.functionArea.removeRefPointSelectionListeners();
            this.removeRefPointSelectionListeners();
        }

        // lets show a reference point if given...
        if ((this.moccoViewer.referencePoint != null) && (this.fitObjective.getSelectedIndex() == 0)) {
            mySet = new GraphPointSet(1001, this.functionArea);
            mySet.setConnectedMode(true);
            mySet.setColor(Color.RED);
            double tmpD = this.moccoViewer.referencePoint[this.objective1.getSelectedIndex()];
            if ((tmpD > xmin - 0.1 * xrange) && (tmpD < xmax + 0.1 * xrange)) {
                mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
                mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
            }
            mySet = new GraphPointSet(1002, this.functionArea);
            mySet.setConnectedMode(true);
            mySet.setColor(Color.RED);
            tmpD = this.moccoViewer.referencePoint[this.objective2.getSelectedIndex()];
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
        if (this.fitObjective.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this.moccoViewer.moccoStandalone.getMainFrame(),
                    "Reference point needs to be selected in fitness space!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            double[] tmpD = this.moccoViewer.referencePoint;
            tmpD[this.objective1.getSelectedIndex()] = point[0];
            tmpD[this.objective2.getSelectedIndex()] = point[1];
            this.moccoViewer.refPointGiven(tmpD);
        }
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
    }
}
